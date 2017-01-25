package com.bwyap.network.interpreter;

import com.bwyap.network.client.Client;
import com.bwyap.network.client.ClientLogger;
import com.bwyap.network.message.MessagePacket;
import com.bwyap.network.message.MessageType;
import com.bwyap.utility.interpreter.Interpreter;
import com.bwyap.utility.resource.ResourceLoader;

/**
 * An interpreter which handles input specific to a client.
 * Commands are given to this interpreter in a string form. 
 * Receiving input from the user and giving output to the user should not be handled in this class.
 * @author bwyap
 *
 */
public abstract class ClientInterpreter extends Interpreter {

	protected Client client;
	protected ClientLogger logger;

	protected static String commandListLocation = "/com/bwyap/network/resource/commands/client_commands";
	
	
	public ClientInterpreter(Client client) {
		this.client = client;
		commands = ResourceLoader.loadCommands(commandListLocation);
	}
	

	@Override
	public void executeInput(String[] args) {
		switch (args[0]) {
		case "connectip":
			connectip(args);
		case "connect":
			connect(); break;
		case "disconnect":
			disconnect(); break;
		case "say":
			say(args); break;
		case "help":
			help(args); break;
		case "address":
			address(args); break;
		case "exit":
			exit(); break;
		case "name":
			name(args); break;
		}
	}
	
	
	/*
	 * ==============
	 *    COMMANDS
	 * ==============
	 */
	
	/**
	 * Connects the client to the given server address and port.
	 * @param args
	 */
	public void connectip(String[] args) {
		if (!client.hasConnection()) {
			client.setServerAddress(args[1]);
			client.setServerPort(Integer.parseInt(args[2]));
		}
	}
	
	
	/**
	 * Connects the client to the server.
	 */
	public void connect() {
		if (!client.hasConnection()) {
			new Thread(client, "client").start(); 
		}
		else {
			println("Already connected to a server. You must disconnect before connecting to another server.");
		}	
	}
	
	
	/**
	 * Disconnects the client from the server.
	 * Does nothing if the client is not connected.
	 */
	public void disconnect() {
		if (client.hasConnection())	client.disconnectFromServer();
		else println("No connection to terminate.");
	}
	
	
	/**
	 * Sends a message to the server.
	 * @param args
	 */
	public void say(String[] args) {
		if (client.hasConnection()) {
			MessagePacket m = new MessagePacket(client.getID(), client.getServerID(), MessageType.MESSAGE_CHAT, args);
			client.sendToServer(m);
		}
		else println("No connection: unable to send message.");
	}
	
	
	/**
	 * Displays or sets the IP and port that the client should connect to.
	 * @param args
	 */
	public void address(String[] args) {
		if (args.length == 1) {
			println(client.getServerAddress() + ":" + client.getServerPort());
			return;
		}
		else if (args.length == 3) {
			if (client.hasConnection()) {
				println("Cannot change address while there is a connection.");
				return;
			}
			try {
				int port = Integer.parseInt(args[2]);
				client.setServerPort(port);
				client.setServerAddress(args[1]);
				println("Address set to " + client.getServerAddress() + ":" + client.getServerPort());
			}
			catch (Exception e) {
				println("The port must be a valid number.");
			}
			
			return;
		}
		println("Invalid arguments.");
	}
	
	
	/**
	 * Displays or sets the current username of the client.
	 * @param args
	 */
	public void name(String[] args) {
		if (args.length == 1) {
			println(client.getClientName());
			return;
		}
		else if (args.length == 2) {
			if (client.hasConnection()) {
				println("Cannot change name while there is a connection.");
				return;
			}
			client.setClientName(args[1]);
			println("Name changed to <" + client.getClientName() + ">");
			return;
		}
	}
	
	
	/**
	 * Exits the program.
	 */
	public void exit() {
		client.shutdown();
	}
	
}
