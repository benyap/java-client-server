package com.bwyap.network.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.bwyap.network.ConnectionState;
import com.bwyap.network.authenticator.ClientAuthenticator;
import com.bwyap.network.authenticator.ClientAuthenticatorInterface;
import com.bwyap.network.interpreter.ClientInterpreter;
import com.bwyap.network.interpreter.ConsoleClientInterpreter;
import com.bwyap.network.message.MessagePacket;
import com.bwyap.network.message.MessageType;
import com.bwyap.utility.interpreter.InterpreterInterface;

/**
 * An abstract class representing a client that can connect to a server.
 * A client object is created with its intended connection address and port.
 * When it is run (ideally on a new thread), it will attempt to make a connection to the server at the specified location.
 * <p>
 * Upon a successful connection, the client will listen for messages from a server and send processes when appropriate on separate threads.
 * @author bwyap
 *
 */
public abstract class Client implements Runnable {
	
	public static final String VERSION = "0.8a";
	
	public static final int TIMEOUT_LIMIT = 2;
	public static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
	public static final boolean DEV_DEBUG = false;
	
	protected String clientName;
	protected String serverAddress;
	protected int serverPort;
	protected ConnectionState state;
	protected ClientAuthenticatorInterface authenticator;

	protected ClientLogger logger;
	
	protected Socket socket;
	protected InetAddress serverIP;
	
	protected UUID serverAssignedID;
	protected UUID serverID;

	protected volatile boolean hasConnection = false;
	protected ObjectOutputStream out = null;
	protected ObjectInputStream in = null;
	protected OutgoingProcessor outProcessor;
	protected Thread outThread;
	protected IncomingProcessor inProcessor;
	protected Thread inThread;
	
	protected Thread interpreterThread;
	protected InterpreterInterface interpreter; 
	
	protected PriorityBlockingQueue<MessagePacket> incomingMessageQueue = 
			new PriorityBlockingQueue<MessagePacket>();
	
	protected PriorityBlockingQueue<MessagePacket> outgoingMessageQueue = 
			new PriorityBlockingQueue<MessagePacket>();
	

	public Client(String clientName, String serverAddress, int serverPort, ClientAuthenticator authenticator) {
		this.clientName = clientName;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.authenticator = authenticator;
		init();
	}
	
	
	public Client(String clientName, ClientAuthenticator authenticator) {
		this.clientName = clientName;
		this.serverAddress = "localhost";
		this.serverPort = 8080;
		this.authenticator = authenticator;
		init();
	}
	
	
	private void init() {
		if (DEV_DEBUG) System.out.println("CLIENT version " + VERSION);
		this.serverAssignedID = null;
		this.serverID = null;
		this.state = ConnectionState.NEW;
		this.logger = new ClientLogger(this.clientName, System.out);
		interpreter = new ConsoleClientInterpreter(this);
	}
	
	
	/**
	 * Start the thread to run the interpreter so that the user can enter commands to the client.
	 */
	public void runInterpreter() {
		if (interpreterThread == null) interpreterThread = new Thread((ClientInterpreter) interpreter);
		if (!interpreterThread.isAlive()) interpreterThread.start();
		else System.err.println("Error: Interpreter thread already running.");
	}
	
	
	@Override
	public void run() {
		if (!openConnection()) {
			logger.pushError("Unable to connect " + clientName + " to " + serverIP.toString() + ":" + serverPort);
			return;
		}
		else {
			logger.pushInfo("Opened connection for " + clientName + " to " + serverIP.toString() + ":" + serverPort);
			state = ConnectionState.AUTHENTICATE;
		}

		try {
			hasConnection = true;

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());			
			
			outProcessor = new OutgoingProcessor();
			outThread = new Thread(outProcessor, "outgoing");
			outThread.start();
			
			inProcessor = new IncomingProcessor();
			inThread = new Thread(inProcessor, "incoming");
			inThread.start();
			
			// main loop for processing messages
			while (hasConnection) {
				tick();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This method should be called every tick to handle any received messages.
	 */
	protected void tick() {
		switch (state) {
		case AUTHENTICATE:
			authenticate();
			break;
		case CONNECTED:
			MessagePacket m;
			try {
				m = incomingMessageQueue.poll(TIMEOUT_LIMIT, TimeUnit.SECONDS);
				processMessage(m);
			} catch (InterruptedException e) { }
			break;
		case DISCONNECTING:
			processDisconnect();
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * Sets the client to the {@code AUTHENTICATE} state and attempts to open a new connection.
	 * @return
	 */
	public boolean openConnection() {
		try {
			state = ConnectionState.AUTHENTICATE;
			serverIP = InetAddress.getByName(serverAddress);
			socket = new Socket(serverIP, serverPort);
			return true;
		}
		catch (ConnectException e) {
			logger.pushError("Connection refused.");
		}
		catch (UnknownHostException e) {
			logger.pushError("Unknown host.");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	/**
	 * Closes the socket connection gracefully.
	 * Server is notified of the disconnection.
	 * 
	 */
	private void terminateConnection() {
		if (state != ConnectionState.TERMINATED) {
			state = ConnectionState.TERMINATED;
			hasConnection = false;
			
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			serverAssignedID = null;
			serverID = null;
			logger.pushCon("Connection terminated.");
		}
	}
	
	
	/**
	 * Starts the disconnection sequence.
	 */
	public void disconnectFromServer() {
		state = ConnectionState.DISCONNECTING;
	}
	
	
	/**
	 * Sends a message packet to the server.
	 * @param m
	 */
	public void sendToServer(MessagePacket m) {
		outgoingMessageQueue.offer(m);
	}
	
	
	/**
	 * Perform the authentication handshake with the server to make a valid connection with the server.
	 * Uses the Authenticator object. 
	 */
	private void authenticate() {
		MessagePacket m = null;

		// Wait for permission to connect
		try {
			logger.pushCon("Found server.");
			m = incomingMessageQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Check message
		if (m != null) {
			if (authenticator.isServerFull(m)) {
				logger.pushError("Server full.");
				
				//Disconnect
				terminateConnection();
				return;
			}
			//TODO handle other cases
			else if (authenticator.isRequestingAuthentication(m)) {
				// Send message
				if (DEV_DEBUG) logger.pushCon("Sending authentication to server.");
				MessagePacket authentication = authenticator.generateAuthenticator(null, clientName);
				sendToServer(authentication);
			
				// Wait for connection confirmation
				try {
					if (DEV_DEBUG) logger.pushCon("Awaiting response...");
					m = incomingMessageQueue.poll(TIMEOUT_LIMIT, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				
				// Authenticate message
				if (m != null) {			
					if (authenticator.confirmAuthentication(m)) {
						logger.pushCon("Connection successful <" + m.data[0] + ">");
						
						serverID = m.senderID;
						serverAssignedID = m.receiverID;
						logger.pushInfo("Server ID: " + serverID);
						logger.pushInfo("Assigned ID: " + serverAssignedID);
						
						//Change state
						state = ConnectionState.CONNECTED;
						authenticator.setUUID(serverAssignedID);
					}
					else {
						//Disconnect
						logger.pushCon("Connection not authenticated. Disconnecting...");
						terminateConnection();
					}
				}
				else {
					logger.pushCon("No response from server. Disconnecting...");
					terminateConnection();
				}
			}
		}
	}
	
	
	/**
	 * Process messages sent to the client from the server.
	 * This method should be extended to implement custom message handling.
	 */
	protected void processMessage(MessagePacket m) {
		if (m != null) {
			switch (m.type) {
			case MESSAGE_CHAT:
			case MESSAGE_BROADCAST:
				printMessage(m); break;
			case KICK:
				if (authenticator.gettingKicked(m)) kick(m); break;
			default:
				logger.pushError("Unhandled message.");
				break;
			}
		}
	}
	
	
	/**
	 * Disconnects from the server as a result of a kick message from the server
	 * @param m
	 */
	public void kick(MessagePacket m) {
		logger.pushInfo("Kicked by server.");
		logger.pushInfo("REASON: " + m.data[0]);
		terminateConnection();
	}
	
	
	/**
	 * Prints a message sent to the client from the server
	 * @param m
	 */
	protected void printMessage(MessagePacket m) {
		String message = "", sender;

		if (m.senderID.equals(serverID)) sender = "SERVER";
		else sender = m.senderID.toString().substring(0, 5);
		
		if (m.type == MessageType.MESSAGE_CHAT) {
			for (int i = 2; i < m.data.length; i++) message += m.data[i] + " ";
			interpreter.println(sender + " whispers: " + message);
		}
		else {
			for (int i = 1; i < m.data.length; i++) message += m.data[i] + " ";
			interpreter.println(sender + " says: " + message);
		}
	}
	
	
	/**
	 * Starts the process to disconnect from the server safely.
	 */
	protected void processDisconnect() {
		logger.pushCon("Disconnecting from the server...");
		
		//TODO save stuff
		//
		
		sendToServer(authenticator.generateDisconnectRequest(serverID));
		
		// Wait for connection confirmation
		MessagePacket m = null;
		try {
			if (DEV_DEBUG) logger.pushCon("Awaiting response...");
			m = incomingMessageQueue.poll(TIMEOUT_LIMIT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		
		if (m != null) {
			if (authenticator.disconnectAcknowledged(m)) {
				logger.pushInfo("Server acknowledged disconnect request.");
			}
		}
		else {
			logger.pushError("No response from server. Terminating connection.");
		}
		
		terminateConnection();
	}
	
	
	/**
	 * Shutdown the client.
	 */
	public void shutdown() {
		this.disconnectFromServer();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		interpreter.stop();
		logger.pushInfo("Shutting down client.");
	}
	
	
	/**
	 * Get the server assigned ID of the client.
	 * This may be nullif the client is not connected to a server.
	 * @return
	 */
	public UUID getID() {
		return serverAssignedID;
	}
	
	
	/**
	 * Get the ID of the server the client is connected to.
	 * This may be null if the client is not connected to a server.
	 * @return
	 */
	public UUID getServerID() {
		return serverID;
	}
	
	
	/**
	 * Gets the name of the client.
	 * @return
	 */
	public String getClientName() {
		return clientName;
	}
	
	
	/**
	 * Sets the username of the client.
	 * @param name
	 */
	public void setClientName(String name) {
		this.clientName = name;
		this.logger.setName(name);
	}
	
	
	/**
	 * Check if the client currently has a connection.
	 * @return
	 */
	public boolean hasConnection() {
		return hasConnection;
	}
	
	
	/**
	 * Get the address the client is set to connect to.
	 * @return
	 */
	public String getServerAddress() {
		return serverAddress;
	}
	
	
	/**
	 * Set the address the client should connect to.
	 * @param address
	 */
	public void setServerAddress(String address) {
		this.serverAddress = address;
	}
	
	
	/**
	 * Get the port the client is set to connect through.
	 * @return
	 */
	public int getServerPort() {
		return serverPort;
	}
	
	
	/**
	 * Set the port the client should connect to.
	 * @param port
	 */
	public void setServerPort(int port) {
		this.serverPort = port;
	}
	
	
	/**
	 * Get the current state of the connection.
	 * @return
	 */
	public ConnectionState getState() {
		return state;
	}
	
	
	/**
	 * Gets the logger used by the client.
	 * @return
	 */
	public ClientLogger getLogger() {
		return logger;
	}
	
	
	/**
	 * Gets the ClientInterpreter for this client.
	 * @return
	 */
	public ClientInterpreter getInterpreter() {
		return (ClientInterpreter) interpreter;
	}
	

	/**
	 * A runnable class which constantly checks for incoming messages and appends
	 * them to the message queue for the server to process.
	 * @author Benjamin
	 *
	 */
	private class IncomingProcessor implements Runnable {
		@Override
		public void run() {
			while (hasConnection) {
				try {
					Object object = (MessagePacket) in.readObject();
					MessagePacket packet = (MessagePacket) object;
					incomingMessageQueue.offer(packet);
					logger.pushReceive("Received message from " + packet.senderID + " of type " + packet.type);
				}
				catch (EOFException e) {
					if (DEV_DEBUG) e.printStackTrace(logger.getStream());
					
					logger.pushError("Connection with server lost.");
					terminateConnection();
				} 
				catch (SocketException e) {
					//Do nothing: assuming socket closed intentionally
				}
				catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
			if (DEV_DEBUG) logger.pushInfo("c incoming finished.");
		}
	}
	
	
	/**
	 * A runnable class which checks constantly checks the outgoing queue
	 * and sends any messages to the client as assigned by the server.
	 * @author Benjamin
	 *
	 */
	private class OutgoingProcessor implements Runnable {
		
		@Override
		public void run() {			
			while (hasConnection) {	
				try { 
					MessagePacket p = outgoingMessageQueue.poll(TIMEOUT_LIMIT, TIMEOUT_UNIT);
					if (p != null) {
						out.writeObject(p);
						logger.pushSend("Sent message to " + socket.getInetAddress() + ":" + socket.getPort());
					}
				} 
				catch (EOFException e) {
					if (DEV_DEBUG) e.printStackTrace(logger.getStream());
					
					logger.pushError("Connection with server lost.");
					terminateConnection();
				}
				catch (SocketException e) {
					//Do nothing: assuming socket closed intentionally
				}
				catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
			if (DEV_DEBUG) logger.pushInfo("c outgoing finished.");
		}
	}
	
}
