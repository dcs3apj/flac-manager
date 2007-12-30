package uk.co.unclealex.music.core.service;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import uk.co.unclealex.music.core.dao.EncodedTrackDao;
import uk.co.unclealex.music.core.dao.OwnerDao;
import uk.co.unclealex.music.core.model.EncodedAlbumBean;
import uk.co.unclealex.music.core.model.EncodedArtistBean;
import uk.co.unclealex.music.core.model.EncodedTrackBean;
import uk.co.unclealex.music.core.model.EncoderBean;
import uk.co.unclealex.music.core.model.OwnerBean;

@Service
public class OwnerServiceImpl implements OwnerService {

	private static Log log = LogFactory.getLog(OwnerServiceImpl.class);
	
	private EncodedTrackDao i_encodedTrackDao;
	private OwnerDao i_ownerDao;
	
	@Override
	public SortedSet<EncodedTrackBean> getOwnedEncodedTracks(final OwnerBean ownerBean, final EncoderBean encoderBean) {
		final EncodedTrackDao encodedTrackDao = getEncodedTrackDao();
		OwnerSearcher ownerSearcher = new OwnerSearcher() {
			@Override
			public SortedSet<? extends EncodedTrackBean> findAll() {
				return encodedTrackDao.findByEncoderBean(encoderBean);
			}
			@Override
			public SortedSet<? extends EncodedTrackBean> findByArtist(
					EncodedArtistBean encodedArtistBean) {
				return encodedTrackDao.findByArtistAndEncoderBean(encodedArtistBean, encoderBean);
			}
			@Override
			public SortedSet<? extends EncodedTrackBean> findByAlbum(
					EncodedAlbumBean encodedAlbumBean) {
				return encodedTrackDao.findByAlbumAndEncoderBean(encodedAlbumBean, encoderBean);
			}
		};
		return getOwnedEncodedTracks(ownerBean, ownerSearcher);
	}
	
	@Override
	public SortedSet<EncodedTrackBean> getOwnedEncodedTracks(final OwnerBean ownerBean) {
		final EncodedTrackDao encodedTrackDao = getEncodedTrackDao();
		OwnerSearcher ownerSearcher = new OwnerSearcher() {
			@Override
			public SortedSet<? extends EncodedTrackBean> findAll() {
				return encodedTrackDao.getAll();
			}
			@Override
			public SortedSet<? extends EncodedTrackBean> findByArtist(
					EncodedArtistBean encodedArtistBean) {
				return encodedTrackDao.findByArtist(encodedArtistBean);
			}
			@Override
			public SortedSet<? extends EncodedTrackBean> findByAlbum(
					EncodedAlbumBean encodedAlbumBean) {
				return encodedAlbumBean.getEncodedTrackBeans();
			}
		};
		return getOwnedEncodedTracks(ownerBean, ownerSearcher);
	}

	protected SortedSet<EncodedTrackBean> getOwnedEncodedTracks(OwnerBean ownerBean, OwnerSearcher ownerSearcher) {
		SortedSet<EncodedTrackBean> encodedTrackBeans = new TreeSet<EncodedTrackBean>();
		if (ownerBean.isOwnsAll()) {
			encodedTrackBeans.addAll(ownerSearcher.findAll());
		}
		else {
			for (EncodedArtistBean encodedArtistBean : ownerBean.getEncodedArtistBeans()) {
				encodedTrackBeans.addAll(ownerSearcher.findByArtist(encodedArtistBean));
			}			
			for (EncodedAlbumBean encodedAlbumBean : ownerBean.getEncodedAlbumBeans()) {
				encodedTrackBeans.addAll(ownerSearcher.findByAlbum(encodedAlbumBean));
			}
		}
		return encodedTrackBeans;
	}

	protected interface OwnerSearcher {
		public SortedSet<? extends EncodedTrackBean> findByArtist(EncodedArtistBean encodedArtistBean);
		public SortedSet<? extends EncodedTrackBean> findByAlbum(EncodedAlbumBean encodedAlbumBean);
		public SortedSet<? extends EncodedTrackBean> findAll();
	}
	
	@Override
	public void updateOwnership(String ownerName,
			Collection<EncodedArtistBean> encodedArtistBeans,
			Collection<EncodedAlbumBean> encodedAlbumBeans) {
		log.info("Updating ownership.");
		if (encodedArtistBeans == null) {
			encodedArtistBeans = new TreeSet<EncodedArtistBean>();
		}
		if (encodedAlbumBeans == null) {
			encodedAlbumBeans = new TreeSet<EncodedAlbumBean>();
		}
		// Filter out any albums owned by any of the given artists
		for (EncodedArtistBean encodedArtistBean : encodedArtistBeans) {
			encodedAlbumBeans.removeAll(encodedArtistBean.getEncodedAlbumBeans());
		}
		OwnerBean ownerBean = getOwnerDao().findByName(ownerName);
		Collection<EncodedArtistBean> ownedEncodedArtistBeans = ownerBean.getEncodedArtistBeans();
		ownedEncodedArtistBeans.retainAll(encodedArtistBeans);
		ownedEncodedArtistBeans.addAll(encodedArtistBeans);
		Collection<EncodedAlbumBean> ownedEncodedAlbumBeans = ownerBean.getEncodedAlbumBeans();
		ownedEncodedAlbumBeans.retainAll(encodedAlbumBeans);
		ownedEncodedAlbumBeans.addAll(encodedAlbumBeans);
		getOwnerDao().store(ownerBean);
	}
	
	public EncodedTrackDao getEncodedTrackDao() {
		return i_encodedTrackDao;
	}

	@Required
	public void setEncodedTrackDao(EncodedTrackDao encodedTrackDao) {
		i_encodedTrackDao = encodedTrackDao;
	}

	public OwnerDao getOwnerDao() {
		return i_ownerDao;
	}

	@Required
	public void setOwnerDao(OwnerDao ownerDao) {
		i_ownerDao = ownerDao;
	}
}
