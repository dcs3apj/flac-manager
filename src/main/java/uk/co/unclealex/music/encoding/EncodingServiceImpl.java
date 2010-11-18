package uk.co.unclealex.music.encoding;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.unclealex.music.Constants;
import uk.co.unclealex.music.DeviceService;
import uk.co.unclealex.music.Encoding;
import uk.co.unclealex.music.FileService;
import uk.co.unclealex.music.ParentFilePredicate;

public class EncodingServiceImpl implements EncodingService {

	private static final Logger log = LoggerFactory.getLogger(EncodingServiceImpl.class);
	
	private File i_flacDirectory;
	private File i_encodedDirectory;
	private SortedSet<Encoding> i_encodings;
	
	private int i_maximumThreads;
	
	private SingleEncodingService i_singleEncodingService;
	private ArtworkUpdatingService i_artworkUpdatingService;
	private FileFixingService i_fileFixingService;
	private DeviceService i_deviceService;
	private FileService i_fileService;
	
	@Override
	public void encodeAll() {
		File flacDirectory = getFlacDirectory();
		log.info("Fixing flac file names in " + flacDirectory);
		fixFlacNames(flacDirectory);
		log.info("Scanning for files in " + flacDirectory);
		final SortedMap<File, SortedSet<File>> flacFilesByDirectory = new TreeMap<File, SortedSet<File>>();
		final SortedMap<String, SortedSet<File>> directoriesByOwner = new TreeMap<String, SortedSet<File>>();
		final SortedMap<File, SortedSet<File>> nonFlacFilesByDirectory = new TreeMap<File, SortedSet<File>>();
		SortedSet<File> allFlacFiles = new TreeSet<File>();
		buildFileLists(flacDirectory, flacFilesByDirectory, directoriesByOwner, nonFlacFilesByDirectory, allFlacFiles);
		log.info("Checking artwork.");
		SortedSet<File> flacFilesWithoutArtwork = updateArtwork(flacFilesByDirectory, nonFlacFilesByDirectory);
		log.info("Encoding.");
		int changeCount = doEncodeFiles(allFlacFiles, getEncodings());
		log.info("Purging orphan encoded files.");
		changeCount += purgeFiles(allFlacFiles);
		if (changeCount == 0) {
			log.info("No changes were detected so device filesystems will not be changed.");
		}
		else {
			log.info("Creating device filesystems.");
			createDeviceFilesystems(directoriesByOwner);
		}
		writeMissingArtworkFiles(flacDirectory, flacFilesWithoutArtwork);
		log.info("Done.");
	}

	protected void fixFlacNames(File flacDirectory) {
		FilenameFilter flacFilenameFilter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return Constants.FLAC.equalsIgnoreCase(FilenameUtils.getExtension(name));
			}
		};
		@SuppressWarnings("unchecked")
		Collection<File> allFlacFiles = 
			FileUtils.listFiles(flacDirectory, FileFilterUtils.asFileFilter(flacFilenameFilter), TrueFileFilter.INSTANCE);
		getFileFixingService().fixFlacFilenames(allFlacFiles);
	}

	protected void writeMissingArtworkFiles(File flacDirectory, SortedSet<File> flacFilesWithoutArtwork) {
		SortedSet<File> flacDirectoriesWithoutArtwork = 
			CollectionUtils.collect(
				flacFilesWithoutArtwork,
				new Transformer<File, File>() {
					@Override
					public File transform(File flacFile) {
						return flacFile.getParentFile();
					}
				},
				new TreeSet<File>());
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File(flacDirectory, Constants.MISSING_ARTWORK));
			for (File flacDirectoryWithoutArtwork : flacDirectoriesWithoutArtwork) {
				writer.println(getFileService().relativiseFile(flacDirectoryWithoutArtwork));
			}
		}
		catch (FileNotFoundException e) {
			log.warn("Could not create the missing artwork file.", e);
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}

	protected void buildFileLists(File flacDirectory, final SortedMap<File, SortedSet<File>> flacFilesByDirectory,
			final SortedMap<String, SortedSet<File>> directoriesByOwner,
			final SortedMap<File, SortedSet<File>> nonFlacFilesByDirectory, SortedSet<File> allFlacFiles) {
		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				String filename = f.getName();
				String baseName = FilenameUtils.getBaseName(filename);
				String extension = FilenameUtils.getExtension(filename);
				File directory = f.getParentFile();
				if (Constants.OWNER.equals(baseName)) {
					String ownerName = extension;
					SortedSet<File> ownedDirectories = directoriesByOwner.get(ownerName);
					if (ownedDirectories == null) {
						ownedDirectories = new TreeSet<File>();
						directoriesByOwner.put(ownerName, ownedDirectories);
					}
					if (CollectionUtils.find(ownedDirectories, new ParentFilePredicate(directory)) == null) {
						ownedDirectories.add(directory);
					}
				}
				else {
					SortedMap<File, SortedSet<File>> filesByDirectory =
						Constants.FLAC.equals(extension)?flacFilesByDirectory:nonFlacFilesByDirectory;
					SortedSet<File> files = filesByDirectory.get(directory);
					if (files == null) {
						files = new TreeSet<File>();
						filesByDirectory.put(directory, files);
					}
					files.add(f);
				}
				return false;
			}
		};
		getFileService().listFiles(flacDirectory, fileFilter);
		for (SortedSet<File> flacFiles : flacFilesByDirectory.values()) {
			allFlacFiles.addAll(flacFiles);
		}
	}

	protected SortedSet<File> updateArtwork(
			SortedMap<File, SortedSet<File>> flacFilesByDirectory, SortedMap<File, SortedSet<File>> nonFlacFilesByDirectory) {
		ArtworkUpdatingService artworkUpdatingService = getArtworkUpdatingService();
		SortedSet<File> filesWithoutArtwork = new TreeSet<File>();
		for (Entry<File, SortedSet<File>> entry : flacFilesByDirectory.entrySet()) {
			File directory = entry.getKey();
			SortedSet<File> flacFiles = entry.getValue();
			SortedSet<File> possibleImageFiles = nonFlacFilesByDirectory.get(directory);
			if (!artworkUpdatingService.updateArtwork(flacFiles, possibleImageFiles)) {
				filesWithoutArtwork.addAll(flacFiles);
			}
			if (possibleImageFiles != null) {
				for (File possibleImageFile : possibleImageFiles) {
					possibleImageFile.delete();
				}
			}
		}
		return filesWithoutArtwork;
	}

	protected int doEncodeFiles(SortedSet<File> allFlacFiles, SortedSet<Encoding> allEncodings) {
		SortedMap<Encoding, File> encodingScriptFilesByEncoding = new TreeMap<Encoding, File>();
		Class<? extends EncodingServiceImpl> clazz = getClass();
		for (Encoding encoding : allEncodings) {
			try {
				File encodingScriptFile = File.createTempFile("music-encoding-", ".sh");
				InputStream in = clazz.getResourceAsStream("flac2" + encoding.getExtension());
				OutputStream out = new FileOutputStream(encodingScriptFile);
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
				encodingScriptFile.deleteOnExit();
				encodingScriptFile.setExecutable(true);
				encodingScriptFilesByEncoding.put(encoding, encodingScriptFile);
			}
			catch (IOException e) {
				log.warn("Cannot create a script file for encoding " + encoding, e);
			}
		}
		int changeCount = encodeFiles(allFlacFiles, encodingScriptFilesByEncoding);
		for (File encodingScriptFile : encodingScriptFilesByEncoding.values()) {
			encodingScriptFile.delete();
		}
		return changeCount;
	}
	
	protected int encodeFiles(SortedSet<File> allFlacFiles, SortedMap<Encoding, File> encodingScriptFilesByEncoding) {
		int maximumThreads = getMaximumThreads();
		final BlockingQueue<EncodingCommand> encodingCommands = new LinkedBlockingQueue<EncodingCommand>();
		EncodingWorker[] workers = new EncodingWorker[maximumThreads];
		final SingleEncodingService singleEncodingService = getSingleEncodingService();
		for (int idx = 0; idx < maximumThreads; idx++) {
			workers[idx] = new EncodingWorker(encodingCommands) {
				@Override
				protected void process(EncodingCommand encodingCommand) throws EncodingException {
					singleEncodingService.encode(
							encodingCommand.getEncoding(), 
							encodingCommand.getFlacFile(),
							encodingCommand.getEncodingScriptFile(), encodingCommand.getDestinationFile());
				}
			};
			workers[idx].start();
		}
		FileService fileService = getFileService();
		for (File flacFile : allFlacFiles) {
			for (Entry<Encoding, File> entry : encodingScriptFilesByEncoding.entrySet()) {
				Encoding encoding = entry.getKey();
				File encodingScriptFile = entry.getValue();
				File destinationFile = fileService.translateFlacFileToEncodedFile(flacFile, encoding);
				if (!destinationFile.exists() || flacFile.lastModified() > destinationFile.lastModified()) {
					encodingCommands.offer(new EncodingCommand(encoding, flacFile, destinationFile, encodingScriptFile));
				}
			}
		}
		for (EncodingWorker worker : workers) {
			encodingCommands.offer(worker.getEndOfWorkCommand());
		}
		int totalCount = 0;
		for (EncodingWorker worker : workers) {
			try {
				worker.join();
			}
			catch (InterruptedException e) {
				// Do nothing
			}
			totalCount += worker.getCount();
		}
		return totalCount;
	}

	protected int purgeFiles(SortedSet<File> allFlacFiles) {
		File encodedDirectory = getEncodedDirectory();
		final FileService fileService = getFileService();
		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File encodedFile) {
				File flacFile = fileService.translateEncodedFileToFlacFile(encodedFile);
				if (!flacFile.exists()) {
					purge(encodedFile);
					return true;
				}
				return false;
			}
		};
		return fileService.listFiles(encodedDirectory, fileFilter).size();
	}

	protected void purge(File encodedFile) {
		do {
			log.info("Purging " + encodedFile);
			encodedFile.delete();
			encodedFile = encodedFile.getParentFile();
		} while (encodedFile != null && encodedFile.list().length == 0);
	}

	protected void createDeviceFilesystems(SortedMap<String, SortedSet<File>> directoriesByOwner) {
		getDeviceService().createDeviceFileSystems(directoriesByOwner);
	}

	@Override
	public void refreshArtwork() {
		final FileService fileService = getFileService();
		final ArtworkUpdatingService artworkUpdatingService = getArtworkUpdatingService();
		final SortedSet<Encoding> encodings = getEncodings();
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File flacFile) {
				if (Constants.FLAC.equals(FilenameUtils.getExtension(flacFile.getName()))) {
					for (Encoding encoding : encodings) {
						File encodedFile = fileService.translateFlacFileToEncodedFile(flacFile, encoding);
						artworkUpdatingService.updateEncodedArtwork(encoding, flacFile, encodedFile);
					}
				}
				return false;
			}
		};
		fileService.listFiles(getFlacDirectory(), filter);
	}
	
	public File getFlacDirectory() {
		return i_flacDirectory;
	}

	public void setFlacDirectory(File flacDirectory) {
		i_flacDirectory = flacDirectory;
	}

	public File getEncodedDirectory() {
		return i_encodedDirectory;
	}

	public void setEncodedDirectory(File encodedDirectory) {
		i_encodedDirectory = encodedDirectory;
	}

	public SingleEncodingService getSingleEncodingService() {
		return i_singleEncodingService;
	}

	public void setSingleEncodingService(SingleEncodingService singleEncodingService) {
		i_singleEncodingService = singleEncodingService;
	}

	public ArtworkUpdatingService getArtworkUpdatingService() {
		return i_artworkUpdatingService;
	}

	public void setArtworkUpdatingService(ArtworkUpdatingService artworkUpdatingService) {
		i_artworkUpdatingService = artworkUpdatingService;
	}

	public DeviceService getDeviceService() {
		return i_deviceService;
	}

	public void setDeviceService(DeviceService deviceService) {
		i_deviceService = deviceService;
	}

	public int getMaximumThreads() {
		return i_maximumThreads;
	}

	public void setMaximumThreads(int maximumThreads) {
		i_maximumThreads = maximumThreads;
	}

	public FileService getFileService() {
		return i_fileService;
	}

	public void setFileService(FileService fileService) {
		i_fileService = fileService;
	}

	public SortedSet<Encoding> getEncodings() {
		return i_encodings;
	}

	public void setEncodings(SortedSet<Encoding> encodings) {
		i_encodings = encodings;
	}

	public FileFixingService getFileFixingService() {
		return i_fileFixingService;
	}

	public void setFileFixingService(FileFixingService fileFixingService) {
		i_fileFixingService = fileFixingService;
	}

}
