package uk.co.unclealex.music.encoder.initialise;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.co.unclealex.music.base.dao.EncodedTrackDao;
import uk.co.unclealex.music.base.dao.EncoderDao;
import uk.co.unclealex.music.base.dao.FlacTrackDao;
import uk.co.unclealex.music.base.initialise.TrackImporter;
import uk.co.unclealex.music.base.io.DataExtractor;
import uk.co.unclealex.music.base.io.InputStreamCopier;
import uk.co.unclealex.music.base.model.EncodedTrackBean;
import uk.co.unclealex.music.base.model.EncoderBean;
import uk.co.unclealex.music.base.model.FlacTrackBean;
import uk.co.unclealex.music.encoder.service.EncoderService;

@Service
@Transactional
public class ImporterImpl implements Importer {

	private static final Logger log = Logger.getLogger(ImporterImpl.class);
	
	private EncoderDao i_encoderDao;
	private EncodedTrackDao i_encodedTrackDao;
	private EncoderService i_encoderService;
	private FlacTrackDao i_flacTrackDao;
	private TrackImporter i_trackImporter;
	private DataExtractor i_encodedTrackDataExtractor;
	private InputStreamCopier i_inputStreamCopier;
	
	public void importTracks() throws IOException {
		TrackImporter trackImporter = getTrackImporter();
		log.info("Importing tracks.");
		File baseDir = new File("/home/converted");
		EncodedTrackDao encodedTrackDao = getEncodedTrackDao();
		for (EncoderBean encoderBean : getEncoderDao().getAll()) {
			final String extension = encoderBean.getExtension();
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches("[0-9]+\\." + extension);
				}
			};
			for (File file : baseDir.listFiles(filter)) {
				int flacTrackId = Integer.parseInt(FilenameUtils.getBaseName(file.getName()));
				FlacTrackBean flacTrackBean = getFlacTrackDao().findById(flacTrackId);
				if (flacTrackBean == null) {
					log.info("Ignoring " + file + " as it does not correspond to a flac track.");
				}
				else {
					String url = flacTrackBean.getUrl();
					EncodedTrackBean encodedTrackBean =
						encodedTrackDao.findByUrlAndEncoderBean(url, encoderBean);
					if (encodedTrackBean != null) {
						log.info("Ignoring " + file + " as it has already been converted.");
					}
					else {
						InputStream in = new FileInputStream(file);
						trackImporter.importTrack(
							in, (int) file.length(), encoderBean, flacTrackBean.getTitle(), flacTrackBean.getUrl(), 
							flacTrackBean.getTrackNumber(), file.lastModified(), null);
						in.close();						
					}
				}
			}
		}
		getEncoderService().updateMissingAlbumInformation();
	}
	
	@Override
	public void exportTracks() throws IOException {
		FlacTrackDao flacTrackDao = getFlacTrackDao();
		DataExtractor encodedTrackDataExtractor = getEncodedTrackDataExtractor();
		for (EncodedTrackBean encodedTrackBean : getEncodedTrackDao().getAll()) {
			String flacUrl = encodedTrackBean.getFlacUrl();
			String extension = encodedTrackBean.getEncoderBean().getExtension();
			FlacTrackBean flacTrackBean = flacTrackDao.findByUrl(flacUrl);
			if (flacTrackBean == null) {
				log.info("Ignoring " + extension + " version of " + flacUrl + " as it is not a valid flac track.");
			}
			else {
				String filename = flacTrackBean.getId() + "." + encodedTrackBean.getEncoderBean().getExtension();
				File file = new File(new File("/home/converted"), filename);
				if (file.exists()) {
					log.info("Ignoring " + encodedTrackBean + " as it has already been exported.");
				}
				else {
					log.info("Exporting " + encodedTrackBean + " to " + file);
					OutputStream fileOutputStream = new FileOutputStream(file);
					getInputStreamCopier().copy(encodedTrackDataExtractor, encodedTrackBean.getId(), fileOutputStream);
					fileOutputStream.close();
				}
			}
		}
	}
	
	public FlacTrackDao getFlacTrackDao() {
		return i_flacTrackDao;
	}

	@Required
	public void setFlacTrackDao(FlacTrackDao flacTrackDao) {
		i_flacTrackDao = flacTrackDao;
	}

	public TrackImporter getTrackImporter() {
		return i_trackImporter;
	}

	@Required
	public void setTrackImporter(TrackImporter trackImporter) {
		i_trackImporter = trackImporter;
	}

	public EncoderDao getEncoderDao() {
		return i_encoderDao;
	}

	@Required
	public void setEncoderDao(EncoderDao encoderDao) {
		i_encoderDao = encoderDao;
	}

	public EncodedTrackDao getEncodedTrackDao() {
		return i_encodedTrackDao;
	}

	@Required
	public void setEncodedTrackDao(EncodedTrackDao encodedTrackDao) {
		i_encodedTrackDao = encodedTrackDao;
	}

	public EncoderService getEncoderService() {
		return i_encoderService;
	}

	@Required
	public void setEncoderService(EncoderService encoderService) {
		i_encoderService = encoderService;
	}

	public DataExtractor getEncodedTrackDataExtractor() {
		return i_encodedTrackDataExtractor;
	}

	@Required
	public void setEncodedTrackDataExtractor(DataExtractor encodedTrackDataExtractor) {
		i_encodedTrackDataExtractor = encodedTrackDataExtractor;
	}

	public InputStreamCopier getInputStreamCopier() {
		return i_inputStreamCopier;
	}

	@Required
	public void setInputStreamCopier(InputStreamCopier inputStreamCopier) {
		i_inputStreamCopier = inputStreamCopier;
	}
}
