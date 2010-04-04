package uk.co.unclealex.music.encoder.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import uk.co.unclealex.hibernate.model.DataBean;
import uk.co.unclealex.hibernate.service.DataService;
import uk.co.unclealex.music.base.RequiresPackages;
import uk.co.unclealex.music.base.dao.EncodedTrackDao;
import uk.co.unclealex.music.base.dao.FlacTrackDao;
import uk.co.unclealex.music.base.model.EncodedTrackBean;
import uk.co.unclealex.music.base.model.EncoderBean;
import uk.co.unclealex.music.base.model.FlacAlbumBean;
import uk.co.unclealex.music.base.model.FlacTrackBean;
import uk.co.unclealex.music.encoder.action.EncodingAction;
import uk.co.unclealex.music.encoder.action.TrackEncodedAction;
import uk.co.unclealex.music.encoder.exception.EventException;

@Transactional
public class EncoderEncodingEventListener extends AbstractEncodingEventListener implements RequiresPackages {

	private static Logger log = Logger.getLogger(EncoderEncodingEventListener.class);

	private FlacTrackDao i_flacTrackDao;
	private DataService i_dataService;
	private EncodedTrackDao i_encodedTrackDao;
	
	private Map<String, File> i_commandCache = new HashMap<String, File>();
	
	@Override
	public void trackAdded(FlacTrackBean flacTrackBean, EncodedTrackBean encodedTrackBean, final List<EncodingAction> encodingActions) 
	throws EventException {
		Map<String, File> commandCache = getCommandCache();
		EncoderBean encoderBean = encodedTrackBean.getEncoderBean();
		String extension = encoderBean.getExtension();
		File commandFile = commandCache.get(extension);
		if (commandFile == null) {
			commandFile = createCommandFile(encoderBean);
			commandCache.put(extension, commandFile);
		}
		DataBean dataBean = createDataBean(flacTrackBean, encodedTrackBean);
		String[] command;
		try {
			command = new String[] { 
				commandFile.getCanonicalPath(), flacTrackBean.getFile().getCanonicalPath(), getDataService().findFile(dataBean).getCanonicalPath() };
		}
		catch (IOException e) {
			throw new EventException(
					"Could not create a command for track " + encodedTrackBean.getFlacUrl() + " and encoder " + extension, e);
		}
		String commandString = StringUtils.join(command, ' ');
		if (log.isDebugEnabled()) {
			log.debug("Running " + commandString);
		}
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process;
		try {
			process = builder.start();
		}
		catch (IOException e) {
			throw new EventException("Could not start command " + commandString, e);
		}
		int returnValue;
		try {
			returnValue = process.waitFor();
			if (returnValue != 0) {
				StringWriter error = new StringWriter();
				try {
					IOUtils.copy(process.getErrorStream(), error);
					error.write("\n");
					IOUtils.copy(process.getInputStream(), error);
				}
				catch (IOException e) {
					log.warn("Could not create a correct error message.", e);
				}
				throw new EventException(
						"The process " + commandString + " failed with exit code " + returnValue + "\n" + error);
			}
			if (log.isDebugEnabled()) {
				log.debug("Finished " + commandString);
			}
		}
		catch (InterruptedException e) {
			throw new EventException(
					"The process " + commandString + " was interrupted.", e);
		}
		encodingActions.add(new TrackEncodedAction(encodedTrackBean));
	}

	protected DataBean createDataBean(FlacTrackBean flacTrackBean, EncodedTrackBean encodedTrackBean) throws EventException {
		FlacAlbumBean flacAlbumBean = flacTrackBean.getFlacAlbumBean();
		String filename = 
			String.format(
					"%s-%s-%02d-%s", 
					flacAlbumBean.getFlacArtistBean().getCode(), 
					flacAlbumBean.getCode(), 
					flacTrackBean.getTrackNumber(), 
					flacTrackBean.getCode());
		DataBean dataBean;
		String extension = encodedTrackBean.getEncoderBean().getExtension();
		try {
			dataBean = getDataService().createDataBean(filename, extension);
		}
		catch (IOException e) {
			throw new EventException(
					"Cannot create a data bean for file " + flacTrackBean.getUrl() + " and encoder " + extension, e);
		}
		encodedTrackBean.setTrackDataBean(dataBean);
		getEncodedTrackDao().store(encodedTrackBean);
		return dataBean;
	}

	public File createCommandFile(EncoderBean encoderBean) throws EventException {
		try {
			File commandFile = File.createTempFile(encoderBean.getExtension(), ".sh");
			commandFile.deleteOnExit();
			commandFile.setExecutable(true);
			FileWriter writer = new FileWriter(commandFile);
			IOUtils.copy(new StringReader(encoderBean.getCommand()), writer);
			writer.close();
			return commandFile;
		}
		catch (IOException e) {
			throw new EventException("Cannot create a command file for encoder " + encoderBean.getExtension(), e);
		}
	}

	@Override
	public void encodingFinished() {
		for (File commandFile : getCommandCache().values()) {
			commandFile.delete();
		}
		getCommandCache().clear();
	}
	
	@Override
	public String[] getRequiredPackageNames() {
		return new String[] { "vorbis-tools", "lame", "id3v2" };
	}
	
	public Map<String, File> getCommandCache() {
		return i_commandCache;
	}

	public FlacTrackDao getFlacTrackDao() {
		return i_flacTrackDao;
	}

	public void setFlacTrackDao(FlacTrackDao flacTrackDao) {
		i_flacTrackDao = flacTrackDao;
	}

	public DataService getDataService() {
		return i_dataService;
	}

	public void setDataService(DataService dataService) {
		i_dataService = dataService;
	}

	public EncodedTrackDao getEncodedTrackDao() {
		return i_encodedTrackDao;
	}

	public void setEncodedTrackDao(EncodedTrackDao encodedTrackDao) {
		i_encodedTrackDao = encodedTrackDao;
	}
}