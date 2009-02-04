package uk.co.unclealex.music.core.service.filesystem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import uk.co.unclealex.music.core.dao.AlbumCoverDao;
import uk.co.unclealex.music.core.dao.KeyedReadOnlyDao;
import uk.co.unclealex.music.core.model.AlbumCoverBean;
import uk.co.unclealex.music.core.model.FlacAlbumBean;
import uk.co.unclealex.music.core.service.FlacService;

@Transactional
public class CoversRepositoryAdaptor extends AbstractKeyedRepositoryAdaptor<AlbumCoverBean> implements RepositoryAdaptor<AlbumCoverBean> {

	private AlbumCoverDao i_albumCoverDao;
	private FlacService i_flacService;

	@Override
	public Set<FileCommandBean> transform(AlbumCoverBean albumCoverBean) {
		FlacAlbumBean flacAlbumBean = getFlacService().findFlacAlbumByPath(albumCoverBean.getFlacAlbumPath());
		if (flacAlbumBean != null) {
			String path = flacAlbumBean.getFlacArtistBean().getName() + "/" + flacAlbumBean.getTitle() + "." + albumCoverBean.getExtension();
			return Collections.singleton(
				new FileCommandBean(albumCoverBean.getId(), path, albumCoverBean.getDateSelected(), albumCoverBean.getCoverLength(), null));
		}
		else {
			return new HashSet<FileCommandBean>();
		}
	}

	@Override
	public Set<AlbumCoverBean> getAllElements() {
		return getAlbumCoverDao().getSelected();
	}
	
	@Override
	public KeyedReadOnlyDao<AlbumCoverBean> getDao() {
		return getAlbumCoverDao();
	}
	
	public AlbumCoverDao getAlbumCoverDao() {
		return i_albumCoverDao;
	}

	@Required
	public void setAlbumCoverDao(AlbumCoverDao albumCoverDao) {
		i_albumCoverDao = albumCoverDao;
	}

	public FlacService getFlacService() {
		return i_flacService;
	}

	@Required
	public void setFlacService(FlacService flacService) {
		i_flacService = flacService;
	}
}
