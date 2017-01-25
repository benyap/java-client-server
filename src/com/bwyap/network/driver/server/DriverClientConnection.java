package com.bwyap.network.driver.server;

import java.net.Socket;
import java.util.UUID;

import com.bwyap.network.authenticator.ServerAuthenticator;
import com.bwyap.network.message.MessagePacket;
import com.bwyap.network.server.ClientConnection;
import com.bwyap.network.server.ServerLogger;
import com.bwyap.utility.interpreter.InterpreterInterface;

public class DriverClientConnection extends ClientConnection {

	
	public DriverClientConnection(UUID ServerID, Socket socket, ServerLogger logger, ServerAuthenticator authenticator, InterpreterInterface interpreter) {
		super(ServerID, socket, logger, authenticator, interpreter);
	}
	
	
	@Override
	protected void processMessage(MessagePacket m) {
		if (m != null) {
			switch (m.type) {
			//TODO
			//
			//
			default:
				super.processMessage(m); break;
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
