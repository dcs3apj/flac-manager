/**
 * 
 */
package uk.co.unclealex.flacconverter.main;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import uk.co.unclealex.flacconverter.Constants;
import uk.co.unclealex.flacconverter.FileBasedDAO;
import uk.co.unclealex.flacconverter.FileCodec;
import uk.co.unclealex.flacconverter.FlacDAO;
import uk.co.unclealex.flacconverter.IOUtils;
import uk.co.unclealex.flacconverter.Mp3FileCodec;
import uk.co.unclealex.flacconverter.OggFileCodec;
import uk.co.unclealex.flacconverter.Track;

/**
 * @author alex
 *
 */
public class Convert implements Runnable {

	private static FileCodec[] CODECS = new FileCodec[] { new OggFileCodec(), new Mp3FileCodec() };
	private SortedSet<Track> i_flacTracks;
	private FileCodec i_codec;
	private Logger i_log;
	private FileBasedDAO i_fileBasedDAO;
	private File i_baseDir;
	private Map<String, SortedSet<String>> i_ownedArtists;
	
	private Convert(SortedSet<Track> flacTracks, FileCodec codec, Map<String, SortedSet<String>> ownedArtists) {
		i_flacTracks = flacTracks;
		i_codec = codec;
		String extension = codec.getExtension();
		i_log = Logger.getLogger(extension);
		i_baseDir = new File(Constants.BASE_DIR, extension);
		i_fileBasedDAO = new FileBasedDAO(new File(i_baseDir, Constants.RAW_DIR), i_codec);
		i_ownedArtists = ownedArtists;
	}
	
	public void run() {
		Logger log = getLog();
		log.info("Scanning existing " + getCodec().getExtension() + " files.");
		SortedSet<Track> tracks = new TreeSet<Track>();
		
		for (Track track : getFileBasedDAO().findAllTracks(log)) {
			if (track.getException() == null) {
				tracks.add(track);
			}
			else {
				log.warn("Could not scan " + track.getFile().getAbsolutePath(), track.getException());
			}
		}

		SortedSet<Track> newTracks = new TreeSet<Track>();
		SortedSet<Track> deletedTracks = new TreeSet<Track>();
		newTracks.addAll(getFlacTracks());
		newTracks.removeAll(tracks);
		deletedTracks.addAll(tracks);
		deletedTracks.removeAll(getFlacTracks());
		
		int countNewTracks = newTracks.size();
		int countDeletedTracks = deletedTracks.size();
		log.info("Found " + countNewTracks + " new " + pluralise("track", "tracks", countNewTracks) + ".");
		for (Track track : newTracks) {
			log.debug(track.toString());
		}
		log.info("Found " + countDeletedTracks + " " + pluralise("track", "tracks", countDeletedTracks) + " to delete.");
		for (Track track : deletedTracks) {
			log.debug(track.toString());
		}

		SortedSet<Track> reencodedTracks = new TreeSet<Track>();
		
		Map<String, Track> flacTracksByKey = Track.makeMap(getFlacTracks());
		Map<String, Track> tracksByKey = Track.makeMap(tracks);
		
		for (Map.Entry<String, Track> entry : flacTracksByKey.entrySet()) {
			Track track = tracksByKey.get(entry.getKey());
			if (track != null) {
				Track flacTrack = entry.getValue();
				if (flacTrack.getLastModified() > track.getLastModified()) {
					reencodedTracks.add(flacTrack);
				}
			}
		}
		int countReencodedTracks = reencodedTracks.size();
		log.info("Found " + countReencodedTracks + " reencoded " + pluralise("track", "tracks", countReencodedTracks) + ".");
		for (Track track : reencodedTracks) {
			log.debug(track.toString());
			newTracks.add(track);
		}
		
		int trackIdx = 1;
		for (Track track : newTracks) {
			log.info("Converting " + progress(trackIdx++, countNewTracks + countReencodedTracks, track));
			try {
				String output = encode(track);
				log.debug(output);
			}
			catch (IOException e) {
				log.warn("Could not encode " + track.getFile(), e);
			}
		}
		
		trackIdx = 1;
		for (Track track : deletedTracks) {
			log.info("Deleting " + progress(trackIdx++, countDeletedTracks, track));
			IOUtils.deleteFile(track.getFile(), log);
		}
		
		log.info("Pruning empty directories.");
		IOUtils.pruneDirectories(getFileBasedDAO().getBaseDirectory(), getLog());

		log.info("Removing personal directories.");
		for (File personalDirectory : getBaseDir().listFiles(s_personalFileFilter)) {
			for (File artistLink : personalDirectory.listFiles()) {
				log.debug("Deleting " + artistLink.getAbsolutePath());
				IOUtils.deleteFile(artistLink, log);
			}
			log.debug("Deleting " + personalDirectory.getAbsolutePath());
			IOUtils.deleteFile(personalDirectory, log);
		}
		
		log.info("Recreating personal directories.");
		for (Map.Entry<String, SortedSet<String>> entry : getOwnedArtists().entrySet()) {
			String owner = entry.getKey();
			File ownerDir = new File(getBaseDir(), owner);
			for (String artist : entry.getValue()) {
				File targetDir = getCodec().getArtistDirectory(getFileBasedDAO().getBaseDirectory(), artist);
				File sourceDir = getCodec().getArtistDirectory(ownerDir, artist); 
				sourceDir.getParentFile().mkdirs();
				try {
					IOUtils.runCommand(new String[] { "ln", "-s", targetDir.getAbsolutePath(), sourceDir.getAbsolutePath()}, getLog());
				} catch (IOException e) {
					log.warn("Could not link artist " + artist + " from " + sourceDir.getAbsolutePath());
				}
			}
		}
		log.info("Finished");
	}

	private String encode(Track track) throws IOException {
		File target = getCodec().getFile(getFileBasedDAO().getBaseDirectory(), track);
		target.getParentFile().mkdirs();
		InputStream in = IOUtils.runCommand(getCodec().generateEncodeCommand(track, target), getLog());
		String output = IOUtils.toString(in);
		in.close();
		return output;
	}
	
	private String pluralise(String singular, String plural, int scalar) {
		return scalar==1?singular:plural;
	}
	
	private String format(String format, Object ... args) {
		Formatter formatter = new Formatter();
		formatter.format(format, args);
		formatter.flush();
		return formatter.toString();
	}
	private String progress(int current, int total, Track track) {
		return format(
				"%d of %d (%3.2f%%) %s", current, total, 100 * current / (double) total,
				track.toString()).toString();
	}
	/**
	 * @return the codec
	 */
	public FileCodec getCodec() {
		return i_codec;
	}

	/**
	 * @return the baseDir
	 */
	public File getBaseDir() {
		return i_baseDir;
	}

	/**
	 * @return the formatManager
	 */
	public FileBasedDAO getFileBasedDAO() {
		return i_fileBasedDAO;
	}

	/**
	 * @return the log
	 */
	public Logger getLog() {
		return i_log;
	}

	/**
	 * @return the flacTracks
	 */
	public SortedSet<Track> getFlacTracks() {
		return i_flacTracks;
	}
	
	/**
	 * @return the ownedArtists
	 */
	public Map<String, SortedSet<String>> getOwnedArtists() {
		return i_ownedArtists;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger log = Logger.getLogger("flac");
		try {
			List<FileCodec> codecs = findCodecs(args); 

			log.info("Scanning flac tracks");
			SortedSet<Track> flacTracks = new TreeSet<Track>();
			for (Track track : new FlacDAO().findAllTracks(log)) {
				flacTracks.add(track);
			}
			
			for (FileCodec codec : codecs) {
				Runnable runnable = new Convert(flacTracks, codec, analyseOwnership(log));
				new Thread(runnable).start();
			}
		} catch (RuntimeException e) {
			e.printStackTrace(System.err);
			log.fatal("An error has caused the flac converter to stop running.", e);
			System.exit(1);
		}
	}

	private static final FilenameFilter s_personalFileFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.startsWith("owner.");
		}
	};

	private static Map<String, SortedSet<String>> analyseOwnership(Logger log) {
		log.info("Analysing ownership");
		TreeMap<String, SortedSet<String>> ownedArtists = new TreeMap<String, SortedSet<String>>();
		SortedSet<String> unownedArtists = new TreeSet<String>();
		Map<File,String> artists = new FlacDAO().getAllArtists();
		
		for (Map.Entry<File, String> entry : artists.entrySet()) {
			File dir = entry.getKey();
			String artist = entry.getValue();
			File[] ownerFiles = dir.listFiles(s_personalFileFilter);
			if (ownerFiles.length == 0) {
				unownedArtists.add(artist);
			}
			else {
				for (File ownerFile : ownerFiles) {
					String owner = ownerFile.getName();
					if (ownedArtists.get(owner) == null) {
						ownedArtists.put(owner, new TreeSet<String>());
					}
					ownedArtists.get(owner).add(artist);					
				}
			}
		}
		
		for (Map.Entry<String, SortedSet<String>> entry : ownedArtists.entrySet()) {
			log.info("Found " + entry.getKey());
			for (String artist : entry.getValue()) {
				log.debug(artist);
			}
		}
		
		if (!unownedArtists.isEmpty()) {
			StringBuffer message = new StringBuffer("The following artists are owned by no-one:\n");
			for (String artist : unownedArtists) {
				message.append(artist).append('\n');
			}
			log.warn(message.toString());
		}
		return ownedArtists;
	}

	/**
	 * @param args
	 * @return
	 */
	private static List<FileCodec> findCodecs(String[] extensions) {
		if (extensions.length == 0) {
			return Arrays.asList(CODECS);
		}
		List<FileCodec> codecs = new ArrayList<FileCodec>();
		for (String extension : extensions) {
			for (FileCodec codec : CODECS) {
				if (extension.equalsIgnoreCase(codec.getExtension())) {
					codecs.add(codec);
				}
			}
		}
		return codecs;
	}

}
