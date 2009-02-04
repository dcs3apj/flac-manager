package uk.co.unclealex.music.web.actions;

import javax.jcr.RepositoryException;

public class SelectAlbumCoverAction extends AbstractAlbumCoverAction {

	@Override
	public void doExecute() throws RepositoryException {
		getAlbumCoverService().selectAlbumCover(getAlbumCoverBean());
	}

	@Override
	public void fail(Exception e) {
		addActionError("The cover could not be selected: " + e.getMessage());
	}
}
