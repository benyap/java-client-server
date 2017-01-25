package com.bwyap.network.driver.server;

import java.awt.EventQueue;

import com.bwyap.network.driver.authenticator.DriverServerAuthenticator;

/**
 * A launcher for creating a DominationServer object.
 * A launcher contains one instance of a DominationServer which listens to a specified port.
 * If no port is specified, the default port (8080) will be used.
 * @author bwyap
 * @since 16-Nov-2016
 */
@Deprecated
public class DriverServerLauncher {

	private static final int DEFAULT_PORT = 8080;
	
	private DriverServer server;
	
	
	/**
	 * A DominationServerLauncher object is used to launch an instance of a DominationServer.
	 * The server instance can be accessed using the <tt>getServer</tt> method.
	 * @param port the port to listen for connections on
	 */
	public DriverServerLauncher(int port) {
		server = new DriverServer("DriverServer", port, 2, new DriverServerAuthenticator());
		launch();
	}
	
	
	/**
	 * Start the server
	 */
	public void launch() {
		new Thread(server, "server").start();
	}
	
	
	/**
	 * Get the DominationServer instance attached to the launcher.
	 * @return the server instance
	 */
	public DriverServer getServer() {
		return server;
	}
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (args.length == 0) {
					new DriverServerLauncher(DEFAULT_PORT);
				}
				else if (args.length == 1) {
					new DriverServerLauncher(Integer.parseInt(args[0]));
				}
			}
		});
	}
	
}
