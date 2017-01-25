package com.bwyap.network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.bwyap.network.ConnectionState;
import com.bwyap.network.authenticator.ServerAuthenticator;
import com.bwyap.network.interpreter.ConsoleServerInterpreter;
import com.bwyap.network.interpreter.ServerInterpreter;
import com.bwyap.utility.interpreter.InterpreterInterface;

/**
 * An abstract class representing a server bound to a specified port. 
 * If no port is specified, the default port (8080) is used.
 * When the server is run, 
 * it will create a {@code ServerSocket} that listens to the specified port for incoming connections on a new thread.
 * Every new connection, once authenticated, will be added to a list of clients as a {@code ClientConnection} object.
 * Each connection starts a new thread to listen to any incoming messages and TODO to send messages.
 * <p>
 * A separate thread is also started to poll all connected clients.
 * The polling method for clients should be defined in a concrete implementation.
 * 
 * @author bwyap
 *
 */
public abstract class Server implements Runnable {
	
	public static final String VERSION = "0.8a";
	public static final boolean DEV_DEBUG = false;
	
	//TODO change this to use properties file
	protected static final int DEFAULT_PORT = 8080;
	
	protected final int MAX_CLIENTS;
	protected final UUID SERVERID;
	
	protected int port;
	
	protected ServerSocket serverSocket;
	protected ServerAuthenticator authenticator;
	protected ServerLogger logger;
	protected List<ClientConnection> clients = 
			Collections.synchronizedList(new ArrayList<ClientConnection>());
		
	protected InterpreterInterface interpreter;
	
	protected volatile boolean listening = false;
	protected volatile boolean polling = false;
	
	protected Thread listenThread;
	protected Thread pollThread;
	protected Thread interpreterThread;
	
	
	/**
	 * Create a Server on the default port
	 */
	public Server(String name, final int maxConnections, ServerAuthenticator authenticator) {
		if (DEV_DEBUG) System.out.println("SERVER version " + VERSION);
		this.SERVERID = UUID.randomUUID();
		this.port = DEFAULT_PORT;
		this.authenticator = authenticator;
		this.authenticator.setUUID(SERVERID);
		this.MAX_CLIENTS = maxConnections;
		this.logger =  new ServerLogger(name, System.out);
		logger.pushInfo("Server [" + name + "] created on port " + this.port + " (default).");
		logger.pushInfo("ID: " + this.SERVERID);
	}
	
	
	/**
	 * Create a server on the specified port.
	 * @param port
	 */
	public Server(String name, int port, final int maxConnections, ServerAuthenticator authenticator) {
		System.out.println("SERVER version " + VERSION);
		this.SERVERID = UUID.randomUUID();
		this.port = port;
		this.authenticator = authenticator;
		this.MAX_CLIENTS = maxConnections;
		this.logger =  new ServerLogger(name, System.out);
		logger.pushInfo("Server [" + name + "] created on port " + this.port + ".");
		logger.pushInfo("ID: " + this.SERVERID);
	}


	@Override
	public void run() {
		if (Server.DEV_DEBUG) logger.pushInfo("Starting server...");
		
		// Open the socket on the specified port
		if (!createSocket()) {
			logger.pushError("Error opening port.");
			return;
		}
		else if (Server.DEV_DEBUG) logger.pushInfo("Server bound to port.");
		
		// Start a new thread to listen for new clients
		listening = true;
		listenThread = new Thread(new ServerListener(), "listen");
		listenThread.start();
		
		// Start a new thread to poll all existing client connections
		polling = true;
		pollThread = new Thread(new ClientConnectionPoller(), "poll");
		pollThread.start();
		
		//Start a new thread to listen to user commands
		interpreter = new ConsoleServerInterpreter(this);
		interpreterThread = new Thread((ServerInterpreter) interpreter, "interpret");
		interpreterThread.start();
	}
	
	
	/**
	 * Tries to open a socket on the port the server has been assigned to
	 * @return true if binding is successful
	 */
	private boolean createSocket() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	/**
	 * Handler for when a client is polled
	 * @param client
	 */
	protected abstract void pollClient(ClientConnection client);
	

	/**
	 * Listens to the server's assigned port for any new connections from clients.
	 * Client connections are added to the server's client list.
	 * @author Benjamin
	 * 
	 */
	private class ServerListener implements Runnable {
		@Override
		public void run() {
			while (listening) {	
				try {
					if (DEV_DEBUG) logger.pushInfo("Listening for clients...");
					
					serverSocket.setSoTimeout(5000);
					Socket socket = serverSocket.accept();
					ClientConnection client = createClientConnection(SERVERID, socket, logger, authenticator, interpreter);
					
					// Check if exceeding connected clients
					if (clients.size() >= MAX_CLIENTS) {
						logger.pushCon("Client tried to connect: " + socket.getInetAddress() + ":" + socket.getPort());
						logger.pushCon("SERVER FULL: " + socket.getInetAddress() + ":" + socket.getPort() + " kicked");
						client.sendToClient(authenticator.generateServerFullMessage());
						client.state = ConnectionState.DISCONNECTING;
					}
					else {
						// Add client to connected clients
						clients.add(client);
						logger.pushCon("New connection from " + socket.getInetAddress() + ":" + socket.getPort());
					}
					
					client.start();
				} 
				catch (SocketTimeoutException e) {
					//Socket timeout is expected.
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}

			//Listening thread exiting
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Create a new ClientConnection object.
	 * This method should be used to instantiate subclasses of ClientConnection.
	 * @param SERVERID
	 * @param socket
	 * @param logger
	 * @param authenticator
	 * @return
	 */
	protected abstract ClientConnection createClientConnection(UUID serverID, Socket socket, ServerLogger logger, ServerAuthenticator authenticator, InterpreterInterface interpreter);
	
	
	/**
	 * Polls all existing client connections for their state
	 * @author Benjamin
	 *
	 */
	private class ClientConnectionPoller implements Runnable {
		@Override
		public void run() {
			while (polling) {
				for (int i = 0; i < clients.size(); i++) {
					synchronized (clients) {
						pollClient(clients.get(i));
					}
				}
			}
		}
	}
	
	
	/**
	 * Safely disconnects all clients and the server down
	 */
	public synchronized void shutdown() {
		//TODO
		//Save stuff
		//
		
		for (ClientConnection c : clients) {
			c.kick("Server shutting down.");
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		listening = false;
		polling = false;
		interpreter.stop();
		
		logger.pushInfo("Server shutting down.");
	}
	
	
	/**
	 * Get the ID of the server
	 * @return
	 */
	public UUID getID() {
		return SERVERID;
	}
	
	
	/**
	 * Get the port the server is listening on
	 * @return
	 */
	public int getPort() {
		return port;
	}
	
	
	/**
	 * Gets the ServerInterpreter for this client.
	 * @return
	 */
	public ServerInterpreter getInterpreter() {
		return (ServerInterpreter) interpreter;
	}
	
	
	/**
	 * Get the list of clients connected to the server.
	 * @return
	 */
	public List<ClientConnection> getClients() {
		return clients;
	}
	
	
	public boolean isRunning() {
		return listening && polling && interpreter.isRunning();
	}

	
}
