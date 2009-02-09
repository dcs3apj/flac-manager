package uk.co.unclealex.music.web.converter;

import uk.co.unclealex.music.core.dao.FlacArtistDao;
import uk.co.unclealex.music.core.dao.KeyedReadOnlyDao;
import uk.co.unclealex.music.core.model.FlacArtistBean;

import com.opensymphony.xwork2.conversion.annotations.Conversion;

@Conversion
public class FlacArtistConverter extends KeyedConverter<FlacArtistBean> {

	private FlacArtistDao i_flacArtistDao;
	
	@Override
	protected KeyedReadOnlyDao<FlacArtistBean> getDao() {
		return getFlacArtistDao();
	}

	public FlacArtistDao getFlacArtistDao() {
		return i_flacArtistDao;
	}

	public void setFlacArtistDao(FlacArtistDao flacArtistDao) {
		i_flacArtistDao = flacArtistDao;
	}

}