package uk.co.unclealex.music.web.converter;

import uk.co.unclealex.music.base.dao.AlbumCoverDao;
import uk.co.unclealex.music.base.dao.KeyedReadOnlyDao;
import uk.co.unclealex.music.base.model.AlbumCoverBean;

import com.opensymphony.xwork2.conversion.annotations.Conversion;

@Conversion
public class AlbumCoverConverter extends KeyedConverter<AlbumCoverBean> {

	private AlbumCoverDao i_albumCoverDao;
	
	@Override
	protected KeyedReadOnlyDao<AlbumCoverBean> getDao() {
		return getAlbumCoverDao();
	}

	public AlbumCoverDao getAlbumCoverDao() {
		return i_albumCoverDao;
	}

	public void setAlbumCoverDao(AlbumCoverDao albumCoverDao) {
		i_albumCoverDao = albumCoverDao;
	}

}
