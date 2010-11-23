package uk.co.unclealex.music.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class SpringCommand<S> {

	protected static final char HELP_OPTION = 'h';
	private static final Logger log = LoggerFactory.getLogger(SpringCommand.class);
	
	public void run(String[] args) {
		int exitValue = 0;
		Options options = createOptions();
		try {
			CommandLineParser parser = new GnuParser();
			List<String> argList = new ArrayList<String>(Arrays.asList(args));
			argList.remove(0);
			CommandLine commandLine = parser.parse(options, argList.toArray(new String[0]));
			if (commandLine.hasOption('h')) {
				throw new ParseException(null);
			}
			checkCommandLine(commandLine);
			ClassPathXmlApplicationContext ctxt = null;
			try {
				ctxt = new ClassPathXmlApplicationContext("classpath*:application-context-music.xml");
				Class<? extends S> serviceClass = getServiceClass();
				run(ctxt.getBean(serviceClass), commandLine);
			}
			catch (Throwable t) {
				log.error("The command errored.", t);
				exitValue = 1;
			}
			finally {
				if (ctxt != null) {
					ctxt.close();
				}
			}
		}
		catch (ParseException e) {
			exitValue = 2;
			HelpFormatter formatter = new HelpFormatter();
			String message = e.getMessage();
			if (!StringUtils.isEmpty(message)) {
				System.out.println(message);
			}
			formatter.printHelp(args[0], options, true);
		}
		catch (Throwable t) {
			log.error("The command errored.", t);
			exitValue = 1;
		}
		finally {
			System.exit(exitValue);
		}
	}
	
	protected abstract Class<? extends S> getServiceClass();

	protected void checkCommandLine(CommandLine commandLine) throws ParseException {
	}

	protected Options createOptions() {
		Options options = new Options();
		@SuppressWarnings("static-access")
		Option helpOption = OptionBuilder.withDescription("Print this message.").create(HELP_OPTION);
		options.addOption(helpOption);
		for (Option option : addOptions()) {
			options.addOption(option);
		}
		return options;
	}

	protected Option[] addOptions() {
		return new Option[0];
	}
	
	protected abstract void run(S service, CommandLine commandLine) throws Exception;
}
