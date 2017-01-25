package com.bwyap.network.driver.interpreter;

import com.bwyap.network.driver.ClientServerDriver;
import com.bwyap.network.resource.Resource;
import com.bwyap.utility.interpreter.Interpreter;
import com.bwyap.utility.resource.ResourceLoader;

/**
 * An interpreter which handles input from the user. 
 * This interpreter should be able to handle both client commands and server commands.
 * Receiving input from the user and giving output to the user should not be handled in this class.
 * @author bwyap
 *
 */

public abstract class DriverClientServerInterpreter extends Interpreter {
	
	protected static String commandListLocation = "/com/bwyap/network/resource/commands/launcher_commands";
	
	private ClientServerDriver domination;
	
	
	public DriverClientServerInterpreter(ClientServerDriver domination) {
		this.domination = domination;
		commands = ResourceLoader.loadCommands(commandListLocation);
		commands.putAll(domination.getClient().getInterpreter().getCommands());
	}
	
	
	@Override 
	public boolean validate(String[] args) {
		if (!super.validate(args)) {
			return domination.getClient().getInterpreter().validate(args);
		}
		return true;
	}
	

	@Override
	public void executeInput(String[] args) {
		switch (args[0]) {
		case "startserver":
			startServer(args); break;
		case "server":
			serverCommand(args); break;
		case "help":
			//Redundant as the client's interpreter also has this command
			help(args); break;
		case "exit":
			exit(); break;
		default:
			domination.getClient().getInterpreter().executeInput(args);
		}
	}
	
	
	/*
	 * ==============
	 *    COMMANDS
	 * ==============
	 */
	
	
	/**
	 * Attempt to execute a server command.
	 * @param args
	 */
	public void serverCommand(String[] args) {
		if (isServerRunning()) {
			String command = "";
			
			for (int i = 1; i < args.length; i++) {
				command += args[i] + " ";
			}

			domination.getServer().getInterpreter().interpretInput(command);
		}
		else println("No server running.");
	}
	
	
	/**
	 * Host a server. 
	 * Only one server can be hosted at a time.
	 * @param args
	 */
	public boolean startServer(String[] args) {
		if (!isServerRunning()) {
			if (createServer(args)) {
				println("Domination server created on port " + domination.getServer().getPort());
				return true;
			}
		}
		else println("Server already running!");
		return false;
	}
	
	
	private boolean createServer(String[] args) {
		if (args.length == 2) {
			try {
				int port = Integer.parseInt(args[1]);
				domination.createServer(port);
			}
			catch (NumberFormatException e) {
				println("Invalid port entered.");
				return false;
			}
		}
		//Use default port
		else {
			domination.createServer(Resource.Settings.getDefaultPort());
		}
		return true;
	}
	
	
	/**
	 * Returns true if the server is running.
	 * @return
	 */
	public boolean isServerRunning() {
		if (domination.getServer() == null) return false;
		return domination.getServer().isRunning();
	}
	
	
	/**
	 * Exit the program.
	 */
	public void exit() {
		domination.shutdown();
	}
	
}
