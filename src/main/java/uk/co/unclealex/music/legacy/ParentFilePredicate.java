package uk.co.unclealex.music.legacy;

import java.io.File;

import com.google.common.base.Predicate;

public class ParentFilePredicate implements Predicate<File> {

	private File i_childFile;
	
	public ParentFilePredicate(File childFile) {
		super();
		i_childFile = childFile;
	}

	@Override
	public boolean apply(File f) {
		File childFile = getChildFile();
		return !f.equals(childFile) && doEvaluate(childFile, f);
	}

	public boolean doEvaluate(File child, File possibleParent) {
		if (child == null) {
			return false;
		}
		else if (child.equals(possibleParent)) {
			return true;
		}
		else {
			return doEvaluate(child.getParentFile(), possibleParent);
		}
	}
	
	public File getChildFile() {
		return i_childFile;
	}

}