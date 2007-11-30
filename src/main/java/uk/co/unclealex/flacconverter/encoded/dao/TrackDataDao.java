package uk.co.unclealex.flacconverter.encoded.dao;

import java.util.SortedSet;

import uk.co.unclealex.flacconverter.encoded.model.EncodedTrackBean;
import uk.co.unclealex.flacconverter.encoded.model.TrackDataBean;

public interface TrackDataDao extends EncodingDao<TrackDataBean> {

	public TrackDataBean findByEncodedTrackBeanAndSequence(
			EncodedTrackBean encodedTrackBean, int sequence);

	public SortedSet<Integer> getIdsForEncodedTrackBean(
			EncodedTrackBean encodedTrackBean);

	public void removeById(int id);
}
