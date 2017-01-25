package com.bwyap.network.driver.authenticator;

import java.util.UUID;

import com.bwyap.network.authenticator.ServerAuthenticator;
import com.bwyap.network.message.MessagePacket;
import com.bwyap.network.message.MessageType;

public class DriverServerAuthenticator extends ServerAuthenticator {

	private final String key = "password";

	
	public DriverServerAuthenticator(UUID id) {
		super(id);
	}
	
	
	public DriverServerAuthenticator() {
		super(null);
	}

	
	@Override
	public MessagePacket generateAuthenticationRequestMessage(UUID destinationID) {
		return new MessagePacket(id, destinationID, MessageType.CONNECTION, null);
	}

	
	@Override
	public boolean authenticateResponse(MessagePacket m) {
		try {
			//TODO
			if (m.data[1].equals(key)) return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	
	@Override
	public MessagePacket generateResponse(UUID destinationID) {
		String[] data = {"connected"};
		MessagePacket authentication = new MessagePacket(id, destinationID, MessageType.CONNECTION, data);
		return authentication;
	}

	
	@Override
	public boolean isRequestingDisconnect(MessagePacket m) {
		try {
			//TODO
			if (m.data[0].equals("disconnect") && m.receiverID.equals(id)) return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}

	
	
	@Override
	public MessagePacket generateDisconnectAckPacket(UUID destinationID) {
		//TODO
		String[] data = {"disconnect_acknowledged"};
		MessagePacket dc = new MessagePacket(id, destinationID, MessageType.CONNECTION, data);
		return dc;
	}

	
	@Override
	public MessagePacket generateServerFullMessage() {
		String[] data = {"server_full"};
		MessagePacket full = new MessagePacket(id, null, MessageType.SERVER_FULL, data);
		return full;
	}
	
	
	@Override
	public MessagePacket generateKickMessage(UUID destinationID, String reason) {
		String[] data = {reason};
		MessagePacket kick = new MessagePacket(id, destinationID, MessageType.KICK, data);
		return kick;
	}

}
