package com.bwyap.network.driver.client;

import java.awt.EventQueue;

import com.bwyap.network.driver.authenticator.DriverClientAuthenticator;

/*
 * My public IP: 114.76.59.180
 */

/**
 * Launches a client that connects to a specified address and port.
 * If no arguments are given, it will attempt to connect to localhost on the default port (8080).
 * 
 * @author bwyap
 * 
 */
@Deprecated
public class DriverClientLauncher {
	
	public DriverClientLauncher() {
		new DriverClient("poo", "localhost", 8080, new DriverClientAuthenticator()).runInterpreter();
	}
	
	public DriverClientLauncher(String name, String address, int port) {
		new DriverClient(name, address, port, new DriverClientAuthenticator()).runInterpreter();
	}
	
	
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (args.length == 0) {
					new DriverClientLauncher();
				}
				else if (args.length == 3) {
					new DriverClientLauncher(args[0], args[1], Integer.parseInt(args[2]));
				}
				else {
					System.out.println("Usage: java -jar Client_xx.jar <name> <ip> <port>");
				}
			}
		});
		
	}
	
}
