package com.bwyap.network.interpreter;

import com.bwyap.network.message.MessagePacket;
import com.bwyap.network.message.MessageType;
import com.bwyap.network.server.ClientConnection;
import com.bwyap.network.server.Server;
import com.bwyap.network.server.ServerLogger;
import com.bwyap.utility.interpreter.Interpreter;
import com.bwyap.utility.resource.ResourceLoader;


/**
 * An interpreter which handles input specific to a server.
 * Commands are given to this interpreter in a string form. 
 * Receiving input from the user and giving output to the user should not be handled in this class.
 * @author bwyap
 *
 */
public abstract class ServerInterpreter extends Interpreter {
	
	protected Server server;
	protected ServerLogger logger;
	
	protected static String commandListLocation = "/com/bwyap/network/resource/commands/server_commands";
	
	
	public ServerInterpreter(Server server) {
		this.server = server;
		commands = ResourceLoader.loadCommands(commandListLocation);
	}
	
	
	@Override
	public void executeInput(String[] args) {
		switch (args[0]) {
		case "list":
			list(args); break;
		case "say":
			say(args); break;
		case "whisper":
			whisper(args); break;
		case "kickall":
			kickall(args); break;
		case "kick":
			kick(args); break;
		case "help":
			help(args); break;
		case "exit":
			exit(); break;
		}
	}
	

	
	/*
	 * ==============
	 *    COMMANDS
	 * ==============
	 */
	
	private void list(String[] args) {
		boolean showExtraInfo = false;
		
		if (args.length == 2) {
			if (args[1].equals("i")) showExtraInfo = true;
			else {
				println("Invalid argument.");
				println("USAGE: " + commands.get(args[0]).getUsage());
				return;
			}
		}
		
		println("CLIENTS: " + server.getClients().size() + " connected");
		for (ClientConnection c : server.getClients()) {
			print(c.clientIdentifier());
			if (showExtraInfo) {
				print(" | " + c.getState() + " @ " + c.getInetAddress().toString() + ":" + c.getPort());
			}
			println();
		}
	}
	
	
	/**
	 * Sends a message to all connected clients.
	 * @param args
	 */
	private void say(String[] args) {
		int count = 0;
		for (ClientConnection c : server.getClients()) {
			MessagePacket m = new MessagePacket(server.getID(), c.getServerID(), MessageType.MESSAGE_BROADCAST, args);
			c.sendToClient(m);
			count++;
		}
		println("Message sent to " + count + (count == 1 ? " client." : " clients."));
	}
	
	
	/**
	 * Sends a message to the specified client.
	 * The second argument must contain the identifier of the client.
	 * @param args
	 */
	private void whisper(String[] args) {
		int hit = 0;
		ClientConnection lastHit = null;
		
		for (ClientConnection c : server.getClients()) {
			if (c.clientIdentifier().equals(args[1])) {
				hit++;
				lastHit = c;
			}
			else if (c.clientIdentifier().substring(0, 
						c.clientIdentifier().length() > args[1].length() ? 
						args[1].length() : c.clientIdentifier().length()).equals(args[1])) {
				hit++;
				lastHit = c;
			}
		}
		
		if (hit == 1) {
			MessagePacket m = new MessagePacket(server.getID(), lastHit.getID(), MessageType.MESSAGE_CHAT, args);
			lastHit.sendToClient(m);
			if (Server.DEV_DEBUG) println("Message sent to " + args[1]);
		}
		else if (hit > 1) {
			println("Please be more specific: " + hit + "clients found with prefix <" + args[1] + ">");
		}
		else println("Client <" + args[1] + "> does not exist.");
	}
	
	
	/**
	 * Kicks all clients from the server.
	 * @param args
	 */
	private void kickall(String[] args) {
		String reason = "";
		int count = 0;
		
		if (args.length == 1) reason = "none";
		for (int i = 1; i < args.length; i++) {
			reason += args[i] + " ";
		}
		
		synchronized (server.getClients()) {
			for (ClientConnection c : server.getClients()) {
				c.kick(reason);
				count++;
			}
		}
		
		if (count > 0) {
			println(count + " clients kicked.");
			println("REASON: " + reason);
		}
		else println("No connected clients.");
	}
	

	/**
	 * Kicks a client from the server.
	 * @param args
	 */
	private void kick(String[] args) {
		String reason = "";
		
		if (args.length == 2) reason = "none";
		for (int i = 2; i < args.length; i++) {
			reason += args[i] + " ";
		}

		for (ClientConnection c : server.getClients()) {
			if (c.clientIdentifier().equals(args[1])) {
				c.kick(reason);
				println("Kicked " + c.clientIdentifier());
				println("REASON: " + reason);
				return;
			}
		}
		println("Client <" + args[1] + "> does not exist.");
	}
	
	
	/**
	 * Exit the server.
	 */
	private void exit() {
		server.shutdown();
	}
	
}
