package uk.co.unclealex.music.encoding;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import uk.co.unclealex.music.ProcessService;

public class SingleEncodingServiceImpl implements SingleEncodingService {

	private static final Logger log = Logger.getLogger(SingleEncodingServiceImpl.class);
	
	private ProcessService i_processService;
	private ArtworkUpdatingService i_artworkUpdatingService;
	private DateFormat i_dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	@Override
	public void encode(Encoding encoding, File flacFile, File encodingScript, File encodedDestination) throws EncodingException {
		log.info("Encoding " + flacFile + " to " + encodedDestination);
		if (encodedDestination.exists()) {
			DateFormat dateFormat = getDateFormat();
			log.info(
					"Encoding as " + flacFile + " (" + dateFormat.format(new Date(flacFile.lastModified())) + 
					") is newer than " + encodedDestination + " (" + dateFormat.format(new Date(encodedDestination.lastModified())) + ").");
		}
		else {
			encodedDestination.getParentFile().mkdirs();
			log.info("Encoding as " + encodedDestination + " does not exist.");
		}
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(
					encodingScript.getCanonicalPath(), flacFile.getCanonicalPath(), encodedDestination.getCanonicalPath());
			getProcessService().run(processBuilder, true);
			getArtworkUpdatingService().updateEncodedArtwork(encoding, flacFile, encodedDestination);
		}
		catch (IOException e) {
			encodedDestination.delete();
			throw new EncodingException("File " + encodedDestination + " could not be created.", e);
		}
	}

	public ProcessService getProcessService() {
		return i_processService;
	}

	public void setProcessService(ProcessService processService) {
		i_processService = processService;
	}

	public ArtworkUpdatingService getArtworkUpdatingService() {
		return i_artworkUpdatingService;
	}

	public void setArtworkUpdatingService(ArtworkUpdatingService artworkUpdatingService) {
		i_artworkUpdatingService = artworkUpdatingService;
	}

	public DateFormat getDateFormat() {
		return i_dateFormat;
	}

}