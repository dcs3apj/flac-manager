package uk.co.unclealex.music.core.service.titleformat;

import uk.co.unclealex.music.core.model.EncodedTrackBean;

public interface TitleFormatService {

	public String getTitle(EncodedTrackBean encodedTrackBean);

	public void setTitleFormat(String titleFormat);
}