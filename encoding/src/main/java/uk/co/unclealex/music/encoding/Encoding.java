package uk.co.unclealex.music.encoding;

public class Encoding implements Comparable<Encoding> {

	private String i_extension;
	private String i_encodingScriptResourceName;
	
	@Override
	public int hashCode() {
		return getExtension().hashCode();
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public int compareTo(Encoding o) {
		return getExtension().compareTo(o.getExtension());
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Encoding && compareTo((Encoding) obj) == 0;
	}
	
	public String getExtension() {
		return i_extension;
	}

	public void setExtension(String extension) {
		i_extension = extension;
	}

	public String getEncodingScriptResourceName() {
		return i_encodingScriptResourceName;
	}

	public void setEncodingScriptResourceName(String encodingScriptResourceName) {
		i_encodingScriptResourceName = encodingScriptResourceName;
	}
}
