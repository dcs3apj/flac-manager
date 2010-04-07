package uk.co.unclealex.music.encoding.command;
import org.springframework.context.ApplicationContext;

import uk.co.unclealex.music.encoding.EncodingService;

public class EncodeCommand extends SpringCommand {

	public static void main(String[] args) {
		new EncodeCommand().run();
	}

	@Override
	public void run(ApplicationContext ctxt) {
		EncodingService encodingService = ctxt.getBean(EncodingService.class);
		encodingService.encodeAll();
	}
}