package com.bwyap.network.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.bwyap.network.ConnectionState;
import com.bwyap.network.authenticator.ServerAuthenticator;
import com.bwyap.network.authenticator.ServerAuthenticatorInterface;
import com.bwyap.network.message.MessagePacket;
import com.bwyap.utility.interpreter.InterpreterInterface;

/**
 * Used to monitor the socket that is connected to the client by 
 * processing any incoming messages and sending messages as directed by the server.
 * 
 * @author bwyap
 *
 */
public class ClientConnection extends Thread {
	
	public static final int TIMEOUT_LIMIT = 2;
	public static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
	
	protected final UUID SERVERID;
	protected UUID serverAssignedID;
	protected ServerLogger logger;
	protected Socket socket;
	protected String clientName;
	
	protected volatile ConnectionState state;
	protected volatile boolean alive = false;
	
	protected ObjectOutputStream out = null;
	protected ObjectInputStream in = null;
	protected Thread outThread;
	protected Thread inThread;
	
	protected ServerAuthenticatorInterface authenticator;
	protected InterpreterInterface interpreter;
	
	protected PriorityBlockingQueue<MessagePacket> incomingMessageQueue = 
			new PriorityBlockingQueue<MessagePacket>();
	
	protected PriorityBlockingQueue<MessagePacket> outgoingMessageQueue = 
			new PriorityBlockingQueue<MessagePacket>();
	
	
	public ClientConnection(UUID ServerID, Socket socket, ServerLogger logger, ServerAuthenticator authenticator, InterpreterInterface interpreter) {
		this.SERVERID = ServerID;
		this.socket = socket;
		this.logger = logger;
		this.authenticator = authenticator;
		this.interpreter = interpreter;
		this.authenticator.setUUID(SERVERID);
		this.state = ConnectionState.NEW;
		this.setName("new-client");
	}
	
	
	@Override
	public void run() {
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
			this.alive = true;
			this.state = ConnectionState.AUTHENTICATE; 
			
			// Start threads for sending and receiving messages
			outThread = new Thread(new OutgoingProcessor(), "outgoing");
			outThread.start();
			
			inThread = new Thread(new IncomingProcessor(), "incoming");
			inThread.start();
			
			// main loop for processing messages
			while (alive) {
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
			processAuthentication();
			break;
		case CONNECTED:
			MessagePacket m;
			try {
				m = incomingMessageQueue.poll(TIMEOUT_LIMIT, TIMEOUT_UNIT);
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
	
	
	private void processAuthentication() {
		//Request authentication
		if (outgoingMessageQueue.isEmpty()) {
			//Authentication message should be the first message in the queue
			//If it isn't, there may be a disconnect message in the queue.
			outgoingMessageQueue.offer(authenticator.generateAuthenticationRequestMessage(serverAssignedID));
		}
		
		// Get authentication
		MessagePacket m = null;
		try {
			if (Server.DEV_DEBUG) logger.pushCon("Awaiting authentication message from client...");
			m = incomingMessageQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Authenticate message
		if (m != null) {
			if (authenticator.authenticateResponse(m)) {
				if (Server.DEV_DEBUG) logger.pushCon("Received valid authentication message from <" + m.data[0] + ">");
				clientName = m.data[0];
				
				//Assign a new UUID to the client
				serverAssignedID = UUID.randomUUID();
				this.setName(clientIdentifier());
				
				// Send message
				MessagePacket response = authenticator.generateResponse(serverAssignedID);
				logger.pushCon("Client connected! Sending response to client with assigned ID " + serverAssignedID);
				sendToClient(response);
				
				//Change state
				state = ConnectionState.CONNECTED;
			}
			else {
				logger.pushError("Received invalid authentication message from <" + m.data[0] + ">");
				
				//Change state
				state = ConnectionState.DISCONNECTING;
			}
		}
	}
	
	
	/**
	 * Process messages sent to the server from this client.
	 * This method should be extended to implement custom message handling.
	 * @param m
	 */
	protected void processMessage(MessagePacket m) {
		if (m != null) {
			switch (m.type) {
			case CONNECTION:
				if (authenticator.isRequestingDisconnect(m)) {
					if (Server.DEV_DEBUG) logger.pushInfo("Disconnect request received.");
					sendToClient(authenticator.generateDisconnectAckPacket(serverAssignedID));
					state = ConnectionState.DISCONNECTING;
				}
				break;
			case MESSAGE_CHAT:
				printMessage(m); break;
			default:
				logger.pushError("Unhandled message.");
				break;
			}
		}
	}
	
	
	/**
	 * Start the process to disconnect from the server safely.
	 * This method should be extended if a subclass requires to perform other tasks before disconnecting.
	 */
	protected void processDisconnect() {
		int count = 0;
		while (!outgoingMessageQueue.isEmpty()) {
			// wait until messages are sent
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//time out of about ~10 seconds
			if (count++ > 10) break;
		}
		terminateConnection();
	}
	
	
	/**
	 * Terminates the connection from the client to the server.
	 * Incoming and outgoing message handling threads are stopped and the socket connection is closed.
	 */
	public void terminateConnection() {
		if (state != ConnectionState.TERMINATED) {
			state = ConnectionState.TERMINATED;
			alive = false;
			
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			logger.pushCon("Connection with " + clientIdentifier() + " terminated.");
		}
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
			while (alive) {
				try {
					Object object = (MessagePacket) in.readObject();
					MessagePacket packet = (MessagePacket) object;
					incomingMessageQueue.offer(packet);
					logger.pushReceive("Received message from " + 
							(packet.senderID != null ? packet.senderID.toString().substring(0, 5) : "<unknown>") + 
							" of type " + packet.type);
				}
				catch (EOFException e) {
					if (Server.DEV_DEBUG) e.printStackTrace(logger.getStream());

					if (state != ConnectionState.DISCONNECTING && state != ConnectionState.TERMINATED) {
						logger.pushInfo("Connection with client " + clientIdentifier() + " lost. ");
						terminateConnection();
					}
				}
				catch (SocketException e) {
					// assuming socket disconnection was intentional
				}
				catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
			if (Server.DEV_DEBUG) logger.pushInfo("s incoming finished.");
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
			while (alive) {	
				try { 
					MessagePacket p = outgoingMessageQueue.poll(TIMEOUT_LIMIT, TIMEOUT_UNIT);
					if (p != null) {
						out.writeObject(p);
						logger.pushSend("Sent message to " + 
								(p.receiverID != null ? p.receiverID.toString().substring(0, 5) : "<unknown>") +  
								" @ " + socket.getInetAddress() + ":" + socket.getPort());
					}
				} 
				catch (EOFException e) {
					if (Server.DEV_DEBUG) e.printStackTrace(logger.getStream());
					
					if (alive) {
						logger.pushInfo("Connection with client " + clientIdentifier() + " lost. ");
						terminateConnection();
					}
				}
				catch (SocketException e) {
					// assuming socket disconnection was intentional
				}
				catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
			if (Server.DEV_DEBUG) logger.pushInfo("s outgoing finished.");
		}
	}
	
	
	/**
	 * Prints a message received by the client
	 * @param m
	 */
	protected void printMessage(MessagePacket m) {
		String message = "";
		for (int i = 1; i < m.data.length; i++) message += m.data[i] + " ";
		
		interpreter.println(clientIdentifier() + " says: " + message);
	}
	
	
	/**
	 * Puts a message in the queue to be sent to the client
	 * @param p
	 */
	public void sendToClient(MessagePacket p) {
		outgoingMessageQueue.offer(p);
	}
	
	
	/**
	 * Kick the client from the server.
	 * @param reason
	 */
	public void kick(String reason) {
		sendToClient(authenticator.generateKickMessage(serverAssignedID, reason));		
		state = ConnectionState.DISCONNECTING;
	}
	

	/**
	 * Gets a shorthand identifier string for this client connection.
	 * @return
	 */
	public String clientIdentifier() {
		String identifier = "";
		final int NAME_LENGTH = 8;
		
		if (serverAssignedID != null) {
			identifier = serverAssignedID.toString().substring(0, 5) + "-";
			identifier += clientName.substring(0, (NAME_LENGTH > clientName.length() ? clientName.length() : NAME_LENGTH));
		}
		else identifier = "<unknown>";
		
		return identifier;
	}
	
	
	/**
	 * Gets the ID of the client as assigned by the server.
	 * @return
	 */
	public UUID getID() {
		return serverAssignedID;
	}
	
	
	/**
	 * Get the ID of the server
	 * @return
	 */
	public UUID getServerID() {
		return SERVERID;
	}

	
	/**
	 * Gets the current state of the connection
	 * @return
	 */
	public ConnectionState getConnectionState() {
		return state;
	}
	
	
	/**
	 * Gets the Inet address of the client
	 * @return
	 */
	public InetAddress getInetAddress() {
		return socket.getInetAddress();
	}
	
	
	/**
	 * Gets the port of the client
	 * @return
	 */
	public int getPort() {
		return socket.getPort();
	}
	
}
