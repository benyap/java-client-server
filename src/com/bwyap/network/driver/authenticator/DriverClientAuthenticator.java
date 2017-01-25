package com.bwyap.network.driver.authenticator;

import java.util.UUID;

import com.bwyap.network.authenticator.ClientAuthenticator;
import com.bwyap.network.message.MessagePacket;
import com.bwyap.network.message.MessageType;

public class DriverClientAuthenticator extends ClientAuthenticator {

	private final String key = "password";

	
	public DriverClientAuthenticator(UUID id) {
		super(id);
	}
	

	public DriverClientAuthenticator() {
		super(null);
	}

	
	@Override
	public boolean isRequestingAuthentication(MessagePacket m) {
		try {
			//TODO
			if (m.type == MessageType.CONNECTION) return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	
	@Override
	public MessagePacket generateAuthenticator(UUID destinationID, String clientName) {
		String[] data = {clientName, key};
		MessagePacket authentication = new MessagePacket(id, destinationID, MessageType.CONNECTION, data);
		return authentication;
	}

	
	@Override
	public boolean confirmAuthentication(MessagePacket m) {
		try {
			//TODO
			if (m.data[0].equals("connected")) return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}

	
	@Override
	public MessagePacket generateDisconnectRequest(UUID destinationID) {
		String[] data = {"disconnect"};
		MessagePacket dc = new MessagePacket(id, destinationID, MessageType.CONNECTION, data);
		return dc;
	}

	
	@Override
	public boolean disconnectAcknowledged(MessagePacket m) {
		try {
			//TODO
			if (m.data[0].equals("disconnect_acknowledged")) return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	
	@Override
	public boolean isServerFull(MessagePacket m) {
		try {
			//TODO
			if (m.type == MessageType.SERVER_FULL) {
				if (m.data != null) {
					if (m.data[0].equals("server_full")) return true;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}
	
	@Override
	public boolean gettingKicked(MessagePacket m) {
		try {
			//TODO
			if (m.type == MessageType.KICK && m.receiverID.equals(id)) {
				return true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
