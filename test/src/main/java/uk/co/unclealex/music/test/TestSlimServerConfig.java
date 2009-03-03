package uk.co.unclealex.music.test;

import java.util.List;

import uk.co.unclealex.music.base.SlimServerConfig;

public class TestSlimServerConfig implements SlimServerConfig {
	
	private List<String> i_definiteArticles;

	public List<String> getDefiniteArticles() {
		return i_definiteArticles;
	}

	public void setDefiniteArticles(List<String> definiteArticles) {
		i_definiteArticles = definiteArticles;
	}
	

}