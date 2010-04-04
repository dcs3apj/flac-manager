package uk.co.unclealex.music.encoder.action;

import uk.co.unclealex.music.base.model.EncodedTrackBean;

public class TrackRemovedAction extends TrackAction {
	
	public TrackRemovedAction(String artistCode, String albumCode, int trackNumber, String trackCode, String extension) {
		super(artistCode, albumCode, trackNumber, trackCode, extension);
	}

	public TrackRemovedAction(EncodedTrackBean encodedTrackBean) {
		super(encodedTrackBean);
	}

	@Override
	public void accept(EncodingActionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <E> E accept(ValueEncodingActionVisitor<E> visitor) {
		return visitor.visit(this);
	}
}