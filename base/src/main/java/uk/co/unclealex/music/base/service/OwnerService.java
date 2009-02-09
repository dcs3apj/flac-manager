package uk.co.unclealex.music.base.service;

import java.util.Collection;
import java.util.SortedSet;

import org.springframework.transaction.annotation.Transactional;

import uk.co.unclealex.music.base.model.EncodedAlbumBean;
import uk.co.unclealex.music.base.model.EncodedArtistBean;
import uk.co.unclealex.music.base.model.EncodedTrackBean;
import uk.co.unclealex.music.base.model.EncoderBean;
import uk.co.unclealex.music.base.model.OwnerBean;

@Transactional
public interface OwnerService {

	public SortedSet<EncodedTrackBean> getOwnedEncodedTracks(OwnerBean ownerBean, EncoderBean encoderBean);
	
	public SortedSet<EncodedTrackBean> getOwnedEncodedTracks(final OwnerBean ownerBean);

	public SortedSet<OwnerBean> getOwners(EncodedAlbumBean encodedAlbumBean);
	
	public void updateOwnership(
			String ownerName,
			Collection<EncodedArtistBean> encodedArtistBeans, Collection<EncodedAlbumBean> encodedAlbumBeans);

	public void clearOwnership();

}