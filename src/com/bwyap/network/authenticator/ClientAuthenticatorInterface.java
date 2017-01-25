package com.bwyap.network.authenticator;

import java.util.UUID;

import com.bwyap.network.message.MessagePacket;

/**
 * Provides methods to be used the the Client to perform authentication handshakes.
 * The corresponding server should use the ServerAuthenticatorInterface.
 * @author bwyap
 *
 */
public interface ClientAuthenticatorInterface extends AuthenticatorInterface {

	
	/**
	 * Checks if a MessagePacket received (by the client) is a valid one requesting for an authentication packet.
	 * Returns true if the MessagePacket is valid.
	 * @param m
	 * @return
	 */
	public boolean isRequestingAuthentication(MessagePacket m);
	
	
	/**
	 * Generates an authentication message to be sent to the server.
	 * The packet should contain data that identifies the client to the server.
	 * @param destinationID the UUID of the recipient
	 * @return
	 */
	public MessagePacket generateAuthenticator(UUID destinationID, String clientName);
	
	
	/**
	 * Checks that an authentication response is valid.
	 * Returns true if the authentication was successful.
	 * This method should be used by the client to verify a successful authentication.
	 * @param m
	 * @return
	 */
	public boolean confirmAuthentication(MessagePacket m);
	
	
	/**
	 * Generates a message that requests a disconnection.
	 * This should be sent from the client to the server.
	 * @param destinationID the UUID of the recipient
	 * @return
	 */
	public MessagePacket generateDisconnectRequest(UUID destinationID);
	
	
	/**
	 * Confirms that a disconnect request has been acknowledged.
	 * This method should be used by the client.
	 * @return
	 */
	public boolean disconnectAcknowledged(MessagePacket m);
	
	
	/**
	 * Checks if a message is indicating that the server is full.
	 * @param m
	 * @return
	 */
	public boolean isServerFull(MessagePacket m);
	
	
	/**
	 * Checks if a message is indicating that the server is kicking the client.
	 * @param m
	 * @return
	 */
	public boolean gettingKicked(MessagePacket m);
	
}
