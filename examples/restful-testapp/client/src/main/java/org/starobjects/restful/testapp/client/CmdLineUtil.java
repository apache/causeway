package org.starobjects.restful.testapp.client;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class CmdLineUtil {
	private CmdLineUtil(){}

	public static enum Optionality {
		REQUIRED,
		OPTIONAL
	}

	public static Option addOption(final Options options, String shortOpt,
			String longOpt, boolean hasArg, String description, Optionality optionality) {
		Option opt = addOption(options, shortOpt, longOpt, hasArg, description);
		if (optionality == Optionality.REQUIRED) {
			opt.setRequired(true);
		}
	    return opt;
	}

	public static Option addOption(final Options options, String shortOpt,
			String longOpt, boolean hasArg, String description) {
		Option opt = new Option(shortOpt, longOpt, hasArg, description);
		options.addOption(opt);
		return opt;
	}

	public static CommandLine parse(String programName, Options options, String[] args) {
		final CommandLineParser parser = new BasicParser();
	    CommandLine cmdLine;
		try {
			cmdLine = parser.parse(options, args);
		} catch (ParseException e) {
			printUsage(programName, options);
			return null;
		}
		return cmdLine;
	}

	public static void printUsage(String programName, Options options) {
		System.err.println("usage:");
		System.err.println(" " + programName + " ");
		for(Object optObj: options.getOptions()) {
			Option option = (Option) optObj;
			System.err.println("       -" + option.getOpt() + "  " + (option.isRequired()?"[required] ":"") + option.getDescription());
		}
	}


}
