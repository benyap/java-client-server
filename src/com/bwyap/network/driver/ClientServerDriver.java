package com.bwyap.network.driver;

import java.awt.EventQueue;

import com.bwyap.network.resource.Resource;
import com.bwyap.network.driver.authenticator.DriverClientAuthenticator;
import com.bwyap.network.driver.authenticator.DriverServerAuthenticator;
import com.bwyap.network.driver.client.DriverClient;
import com.bwyap.network.driver.interpreter.ConsoleDriverClientServerInterpreter;
import com.bwyap.network.driver.interpreter.DriverClientServerInterpreter;
import com.bwyap.network.driver.server.DriverServer;
import com.bwyap.utility.interpreter.InterpreterInterface;
import com.bwyap.utility.resource.ResourceLoader;

/**
 * A Domination client interface.
 * It allows a user access to client-side functionality and the ability to create 
 * a server for other clients to connect to. 
 * @author bwyap
 *
 */
public class ClientServerDriver {
	
	public static final String VERSION = "0.3";
	
	private InterpreterInterface interpreter;
	private DriverClient client;
	private DriverServer server;
	
	private boolean isHosting = false;
	
	private Thread interpreterThread;
	
	
	/**
	 * Private construtor as the Domination instance should be created using 
	 * the static method {@code Domination.launch(String name)} 
	 * @param name
	 */
	private ClientServerDriver(String name) {
		client = createClient(name);
	}
	
	
	/**
	 * Initialize the client with an interpreter and start the program.
	 * @param interpreter
	 */
	public void init(InterpreterInterface interpreter) {
		//Load resources
		ResourceLoader.setLogger(client.getLogger());
		Resource.load();

		this.interpreter = interpreter;
		interpreterThread = new Thread((DriverClientServerInterpreter) this.interpreter);
		interpreterThread.start();
		interpreter.println("Welcome to ClientServerDriver v" + VERSION);
	}
	
	
	/**
	 * Creates a new Domination Client object
	 * @param clientName
	 * @return
	 */
	public DriverClient createClient(String clientName) {
		return new DriverClient(clientName, new DriverClientAuthenticator());
	}
	
	
	/**
	 * Create a server on the specified port
	 * @param port
	 */
	public void createServer(int port) {
		server = new DriverServer("server", port, 2, new DriverServerAuthenticator());
		new Thread(server, "server").start();
	}
	
	
	/**
	 * Shutdown the program
	 */
	public void shutdown() {
		client.shutdown();
		if (server != null) server.shutdown();
		interpreter.stop();
	}
	
	
	/**
	 * Gets the current client object for this instance of Domination
	 * @return
	 */
	public DriverClient getClient() {
		return client;
	}
	
	
	/**
	 * Gets the current server object. 
	 * May return null if a server is not being hosted.
	 * @return
	 */
	public DriverServer getServer() {
		return server;
	}
	
	
	/**
	 * Checks whether a server is being hosted in this instance
	 * @return
	 */
	public boolean isHosting() {
		return isHosting;
	}
	
	
	/**
	 * A static launcher method to create a new instance of Domination.
	 * @param name name of the user 
	 * @return the Domination instance
	 */
	public static ClientServerDriver launch(String name) {
		ClientServerDriver domination = new ClientServerDriver(name);
		domination.init(new ConsoleDriverClientServerInterpreter(domination));
		return domination;
	}
	
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				ClientServerDriver.launch("user");
			}
		});
	}
	
}
