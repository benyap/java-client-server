package com.bwyap.network.driver.server;

import java.net.Socket;
import java.util.UUID;

import com.bwyap.network.ConnectionState;
import com.bwyap.network.authenticator.ServerAuthenticator;
import com.bwyap.network.driver.authenticator.DriverServerAuthenticator;
import com.bwyap.network.server.ClientConnection;
import com.bwyap.network.server.Server;
import com.bwyap.network.server.ServerLogger;
import com.bwyap.utility.interpreter.InterpreterInterface;

/**
 * A DominationServer hosts a game of Domination and handles all connected clients.
 * @author bwyap
 * @since 16-Nov-2016
 */
public class DriverServer extends Server {
	
	public static final String VERSION = "0.1";
	
	
	/**
	 * Create a DominationServer on the default port.
	 * @param name
	 * @param maxConnections the maximum number of connected clients allowed
	 */
	public DriverServer(String name, final int maxConnections, DriverServerAuthenticator authenticator) {
		super(name, maxConnections, authenticator);
		
	}
	
	
	/**
	 * Create a DominationServer on the specified port.
	 * @param name
	 * @param port
	 * @param maxConnections the maximum number of connected clients allowed
	 */
	public DriverServer(String name, int port, final int maxConnections, DriverServerAuthenticator authenticator) {
		super(name, port, maxConnections, authenticator);
	}


	@Override
	protected void pollClient(ClientConnection client) {
		if (client.getConnectionState() == ConnectionState.TERMINATED) {
			clients.remove(client);
		}
		
		//TODO
	}
	
	
	@Override
	protected ClientConnection createClientConnection(UUID serverID, Socket socket, ServerLogger logger, ServerAuthenticator authenticator, InterpreterInterface interpreter) {
		return new DriverClientConnection(serverID, socket, logger, authenticator, interpreter);
	}
	
}
