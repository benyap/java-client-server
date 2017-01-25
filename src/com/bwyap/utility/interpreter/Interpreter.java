package com.bwyap.utility.interpreter;

import java.util.HashMap;

/**
 * An interpreter class which handles input from and output to a user through a command line.
 * 
 * @author bwyap
 *
 */
public abstract class Interpreter implements Runnable, InterpreterInterface {
	
	protected volatile boolean running = false;
	protected HashMap<String, Command> commands;
	
	
	@Override
	public void run() {
		running = true;
		
		while(running) {
			String command = getInput();
			interpretInput(command);
		}
	}
	
	
	@Override
	public boolean isRunning() {
		return running;
	}
	
	
	@Override
	public void stop() {
		running = false;
	}
	
	
	@Override
	public boolean validate(String[] arguments) {		
		if (arguments.length > 0) {
			Command c = commands.get(arguments[0]);
			if (c != null) {
				if (arguments.length <= c.maxArgs() && arguments.length >= c.minArgs()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	/**
	 * Gets the input command from whichever interface is used to interact with the interpreter
	 * @return
	 */
	public abstract String getInput();
	
	
	/**
	 * Processes the input command
	 * @param command
	 */
	public void interpretInput(String command) {
		if (command == null) return;
		
		String[] arguments = command.split(" ");

		if (validate(arguments)) {
			executeInput(arguments);
		}
		else {
			Command c = commands.get(arguments[0]);
			if (c != null) println("USAGE: " + c.getUsage());
			else println("Unrecognised command.");
		}
	}
	
	
	/**
	 * Executes the input command
	 * @param args
	 */
	public abstract void executeInput(String[] args);
	
	
	/**
	 * Print a message on a new line to display to the user.
	 * @param s
	 */
	public abstract void println(String s);
	
	
	public abstract void println();
	

	/**
	 * Print a message to display to the user.
	 * @param s
	 */
	public abstract void print(String s);
	
	
	/**
	 * Gets the map of commands that are valid for this interpreter.
	 * @return
	 */
	public HashMap<String, Command> getCommands() {
		return commands;
	}

	
	/**
	 * Prints help
	 * @param args
	 */
	public void help(String[] args) {
		if (args.length == 2) {
			if (commands.containsKey(args[1])) {
				println("USAGE: " + commands.get(args[1]).getUsage());
				println(commands.get(args[1]).getDescription());
			}
		}
		else {
			for (String c : commands.keySet()) {
				String spacing = "\t";
				if (c.length() < 9) spacing += "\t";
				println(c + spacing + commands.get(c).getDescription());
			}
		}
	}
}
