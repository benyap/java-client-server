package com.bwyap.utility.interpreter;


/**
 * An Command object holds information about the syntax of how to use a particular command.
 * It stores the name of the command, the min and max number of arguments
 * as well as how its usage and description.
 * There are getter methods to easily access this information,
 * which are initialised upon object creation.
 * @author bwyap
 *
 */
public class Command {
	
	public static final int CMDLENGTH = 5;
	
	private String command;
	private int minArguments;
	private int maxArguments;
	private String usage;
	private String description;
	
	
	
	public Command(String cmd, int minArgs, int maxArgs, String usage, String description) {
		this.command = cmd;
		this.minArguments = minArgs;
		this.maxArguments = maxArgs;
		this.usage = usage;
		this.description = description;
	}
	
	
	
	public String getCmd() {
		return command;
	}
	
	
	
	public int minArgs() {
		return minArguments;
	}
	
	
	
	public int maxArgs() {
		return maxArguments;
	}
	
	
	
	public String getUsage() {
		return usage;
	}
	
	
	
	public String getDescription() {
		return description;
	}
	
	
	
	
	//TODO
	//
	
	private final int TABSIZE = 7;
	
	public String toString() {
		String s = "";
		
		if (usage.length() < TABSIZE*1) {
			s += "\t\t\t\t\t";
		}
		else if (usage.length() < TABSIZE*2) {
			s += "\t\t\t\t";
		}
		else if (usage.length() <= TABSIZE*3 + 1) {
			s += "\t\t\t";
		}
		else if (usage.length() <= TABSIZE*4 + 2) {
			s += "\t\t";
		}
		else if (usage.length() <= TABSIZE*5 + 0) {
			s += "\t";
		}
		
		return " " + usage + s + " " + description;
	}
	
	
	
}
