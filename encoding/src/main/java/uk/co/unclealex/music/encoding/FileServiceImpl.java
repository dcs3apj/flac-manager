package uk.co.unclealex.music.encoding;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.FilenameUtils;

import uk.co.unclealex.music.Constants;

public class FileServiceImpl implements FileService {

	private File i_flacDirectory;
	private File i_encodedDirectory;
	private SortedSet<Encoding> i_encodings;
	
	@Override
	public String relativiseFile(File file) {
		Transformer<Encoding, File> transformer = new Transformer<Encoding, File>() {
			@Override
			public File transform(Encoding encoding) {
				return createEncodedRoot(encoding);
			}
		};
		List<File> fileRoots = CollectionUtils.collect(getEncodings(), transformer, new ArrayList<File>());
		fileRoots.add(getFlacDirectory());
		String relativePath = null;
		Predicate<File> isParentOfPredicate = new ParentFilePredicate(file);
		for (Iterator<File> iter = fileRoots.iterator(); relativePath == null && iter.hasNext(); ) {
			File rootFile = iter.next();
			if (isParentOfPredicate.evaluate(rootFile)) {
				URI parentUri = rootFile.toURI();
				URI childUri = file.toURI();
				URI relativeUri = parentUri.relativize(childUri);
				relativePath = relativeUri.getPath();
			}
		}
		return relativePath;
	}

	protected File createEncodedRoot(Encoding encoding) {
		return new File(getEncodedDirectory(), encoding.getExtension());
	}

	@Override
	public File translateEncodedFileToFlacFile(File encodedFile) {
		String filename = encodedFile.getName();
		String newFilename = renameEncodedToFlac(FilenameUtils.getBaseName(filename)) + "." + Constants.FLAC;
		return new File(translateEncodedDirectoryToFlacDirectory(encodedFile.getParentFile()), newFilename);
	}

	@Override
	public File translateFlacFileToEncodedFile(File flacFile, Encoding encoding) {
		String filename = flacFile.getName();
		String newFilename = renameFlacToEncoded(FilenameUtils.getBaseName(filename)) + "." + encoding.getExtension();
		return new File(translateFlacDirectoryToEncodedDirectory(flacFile.getParentFile(), encoding), newFilename);
	}

	@Override
	public File translateEncodedDirectoryToFlacDirectory(File encodedDirectory) {
		File flacDirectory = getFlacDirectory();
		if (getEncodedDirectory().equals(encodedDirectory.getParentFile())) {
			return flacDirectory;
		}
		else {
			String relativeDirectoryPath = relativiseFile(encodedDirectory);
			return new File(flacDirectory, renameEncodedToFlac(relativeDirectoryPath));
		}
	}

	@Override
	public File translateFlacDirectoryToEncodedDirectory(File flacDirectory, Encoding encoding) {
		File encodedRoot = createEncodedRoot(encoding);
		if (getFlacDirectory().equals(flacDirectory)) {
			return encodedRoot;
		}
		else {
			String relativeDirectoryPath = relativiseFile(flacDirectory);
			return new File(encodedRoot, renameFlacToEncoded(relativeDirectoryPath));
		}
	}

	protected String renameEncodedToFlac(String relativePath) {
		return relativePath.replace(' ', '_');
	}

	protected String renameFlacToEncoded(String relativePath) {
		return relativePath.replace('_', ' ');
	}

	@Override
	public SortedSet<File> listFiles(File f, FileFilter fileFilter) {
		TreeSet<File> acceptedFiles = new TreeSet<File>();
		listFiles(f, fileFilter, acceptedFiles);
		return acceptedFiles;
	}
	
	protected void listFiles(File f, FileFilter fileFilter, SortedSet<File> acceptedFiles) {
		if (f.isDirectory()) {
			File[] children = f.listFiles();
			Arrays.sort(children);
			for (File child : children) {
				listFiles(child, fileFilter, acceptedFiles);
			}
		}
		else {
			if (fileFilter.accept(f)) {
				acceptedFiles.add(f);
			}
		}
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

	public SortedSet<Encoding> getEncodings() {
		return i_encodings;
	}

	public void setEncodings(SortedSet<Encoding> encodings) {
		i_encodings = encodings;
	}

}
