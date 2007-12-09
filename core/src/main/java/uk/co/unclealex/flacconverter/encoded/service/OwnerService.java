package uk.co.unclealex.flacconverter.encoded.service;

import java.util.SortedSet;

import org.springframework.transaction.annotation.Transactional;

import uk.co.unclealex.flacconverter.encoded.model.EncodedTrackBean;
import uk.co.unclealex.flacconverter.encoded.model.EncoderBean;
import uk.co.unclealex.flacconverter.encoded.model.OwnerBean;

@Transactional
public interface OwnerService {

	public SortedSet<EncodedTrackBean> getOwnedEncodedTracks(OwnerBean ownerBean, EncoderBean encoderBean);
}
