package uk.co.unclealex.music.web.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import uk.co.unclealex.music.base.initialise.Initialiser;
import uk.co.unclealex.music.commands.Command;
import uk.co.unclealex.music.encoder.initialise.Importer;

@Service
public class Import implements Command {

	private Importer i_importer;
	private Initialiser i_initialiser;
	
	@Override
	public void execute(String[] args) throws IOException {
		getInitialiser().clear();
		getInitialiser().initialise();
		getImporter().importTracks();
	}
	
	public Importer getImporter() {
		return i_importer;
	}

	@Required
	public void setImporter(Importer importer) {
		i_importer = importer;
	}

	public Initialiser getInitialiser() {
		return i_initialiser;
	}

	public void setInitialiser(Initialiser initialiser) {
		i_initialiser = initialiser;
	}
}