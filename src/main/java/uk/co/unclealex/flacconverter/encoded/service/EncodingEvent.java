package uk.co.unclealex.flacconverter.encoded.service;

import uk.co.unclealex.flacconverter.encoded.model.EncoderBean;
import uk.co.unclealex.flacconverter.flac.model.FlacTrackBean;

public class EncodingEvent {

	private FlacTrackBean i_flacTrackBean;
	private EncoderBean i_encoderBean;
	
	public EncodingEvent(
			FlacTrackBean flacTrackBean, EncoderBean encoderBean) {
		super();
		i_flacTrackBean = flacTrackBean;
		i_encoderBean = encoderBean;
	}
	
	public FlacTrackBean getFlacTrackBean() {
		return i_flacTrackBean;
	}
	
	public EncoderBean getEncoderBean() {
		return i_encoderBean;
	}
	
}
