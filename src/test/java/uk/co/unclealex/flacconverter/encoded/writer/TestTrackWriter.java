package uk.co.unclealex.flacconverter.encoded.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.co.unclealex.flacconverter.encoded.model.EncodedTrackBean;

public class TestTrackWriter extends AbstractTrackWriter<ByteArrayOutputStream> {

	private Map<String, Integer> i_fileNamesAndSizes = new LinkedHashMap<String, Integer>();

	@Override
	public ByteArrayOutputStream createStream(EncodedTrackBean encodedTrackBean,
			String title) throws IOException {
		return new ByteArrayOutputStream();
	}

	@Override
	public void closeStream(EncodedTrackBean encodedTrackBean, String title,
			ByteArrayOutputStream out) throws IOException {
		getFileNamesAndSizes().put(title, out.size());
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void create() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public Map<String, Integer> getFileNamesAndSizes() {
		return i_fileNamesAndSizes;
	}

	public void setFileNamesAndSizes(Map<String, Integer> fileNamesAndSizes) {
		i_fileNamesAndSizes = fileNamesAndSizes;
	}
	
}