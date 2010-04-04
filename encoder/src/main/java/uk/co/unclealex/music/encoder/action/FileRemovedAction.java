package uk.co.unclealex.music.encoder.action;

public class FileRemovedAction extends FileAction {

	public FileRemovedAction(String path) {
		super(path);
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