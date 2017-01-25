package com.bwyap.network.driver.client;

import com.bwyap.network.client.Client;
import com.bwyap.network.driver.authenticator.DriverClientAuthenticator;
import com.bwyap.network.message.MessagePacket;

/**
 * A concrete implementation of a client for Domination.
 * 
 * TODO 
 * 
 * @author bwyap
 *
 */
public class DriverClient extends Client {
	
	public static final String VERSION = "0.1";
	
	
	public DriverClient(String clientName, String serverAddress, int serverPort, DriverClientAuthenticator authenticator) {
		super(clientName, serverAddress, serverPort, authenticator);
		
	}
	

	public DriverClient(String clientName, DriverClientAuthenticator authenticator) {
		super(clientName, authenticator);
		
	}
	
	
	@Override 
	protected void processMessage(MessagePacket m) {
		if (m != null) {
			switch (m.type) {
			//TODO 
			// 
			//do stuff
			//
			default:
				super.processMessage(m);
				break;
			}
		}		
	}
	
	
	@Override
	protected void processDisconnect() {
		//TODO
		//
		//save stuff
		//
		super.processDisconnect();
	}


	@Override
	protected void printMessage(MessagePacket m) {
		super.printMessage(m);
	}
	
}
