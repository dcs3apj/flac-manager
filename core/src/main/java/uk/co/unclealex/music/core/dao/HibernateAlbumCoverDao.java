package uk.co.unclealex.music.core.dao;

import java.io.IOException;
import java.util.SortedSet;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.co.unclealex.music.base.dao.AlbumCoverDao;
import uk.co.unclealex.music.base.dao.Streamer;
import uk.co.unclealex.music.base.io.KnownLengthInputStreamCallback;
import uk.co.unclealex.music.base.model.AlbumCoverBean;

@Repository
@Transactional
public class HibernateAlbumCoverDao extends
		HibernateKeyedDao<AlbumCoverBean> implements AlbumCoverDao {

	private Streamer i_streamer;
	
	@Autowired
	public HibernateAlbumCoverDao(@Qualifier("musicSessionFactory") SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public AlbumCoverBean createExampleBean() {
		return new AlbumCoverBean();
	}

	@Override
	public SortedSet<AlbumCoverBean> getCoversForAlbumPath(String albumPath) {
		return asSortedSet(createWithAlbumPathQuery(albumPath));
	}

	protected Query createWithAlbumPathQuery(String albumPath) {
		Query query = getSession().createQuery(
				"from albumCoverBean where flacAlbumPath = :albumPath").
			setString("albumPath", albumPath);
		return query;
	}
	
	@Override
	public SortedSet<AlbumCoverBean> getSelected() {
		Query query = getSession().createQuery(
			"from albumCoverBean where dateSelected is not null");
		return asSortedSet(query);
	}
		
	@Override
	public boolean albumPathHasCovers(String albumPath) {
		Query query = createWithAlbumPathQuery(albumPath);
		return query.iterate().hasNext();
	}

	@Override
	public AlbumCoverBean findSelectedCoverForAlbumPath(String albumPath) {
		Query query = getSession().createQuery(
				"from albumCoverBean where flacAlbumPath = :albumPath and dateSelected is not null").
			setString("albumPath", albumPath);
		return uniqueResult(query);
	}

	@Override
	public void streamCover(int id, KnownLengthInputStreamCallback callback) throws IOException {
		streamField("cover", id, callback);
	}
	
	@Override
	public void streamThumbnail(int id, KnownLengthInputStreamCallback callback) throws IOException {
		streamField("thumbnail", id, callback);
	}
	
	protected void streamField(String fieldName, int id, KnownLengthInputStreamCallback callback)
			throws IOException {
		getStreamer().stream(getSession(), "trackData", "albumCoverBean", id, callback);
	}
	
	public Streamer getStreamer() {
		return i_streamer;
	}

	public void setStreamer(Streamer streamer) {
		i_streamer = streamer;
	}

}
