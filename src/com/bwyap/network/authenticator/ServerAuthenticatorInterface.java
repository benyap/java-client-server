package com.bwyap.network.authenticator;

import java.util.UUID;

import com.bwyap.network.message.MessagePacket;

/**
 * Provides methods to be used the the Server to perform authentication handshakes.
 * The corresponding client should use the ClientAuthenticatorInterface.
 * @author bwyap
 *
 */

public interface ServerAuthenticatorInterface extends AuthenticatorInterface {
	
	

	/**
	 * Generates a MessagePacket requesting an authentication request.
	 * This should be sent by the server to the client. 
	 * @param destinationID the UUID of the recipient
	 * @return
	 */
	public MessagePacket generateAuthenticationRequestMessage(UUID destinationID);
	

	/**
	 * Checks that an authentication message is valid.
	 * This method should be used by the server to verify that the received MessagePacket is valid. 
	 * Returns true if the authentication credentials are valid.
	 * @param m
	 * @return
	 */
	public boolean authenticateResponse(MessagePacket m);


	/**
	 * Generates a message to confirm successful authentication.
	 * This should be sent by the server to the client.
	 * @param destinationID the UUID of the recipient
	 * @return
	 */
	public MessagePacket generateResponse(UUID destinationID);
	
	
	/**
	 * Checks if a message received is for requesting a disconnect.
	 * This method should be used by the server to verify a disconnection request.
	 * @return
	 */
	public boolean isRequestingDisconnect(MessagePacket m);
	

	/**
	 * Generates a message to be sent from the server to the client
	 * to acknowledge that the disconnect request has been received.
	 * @param destinationID
	 * @return
	 */
	public MessagePacket generateDisconnectAckPacket(UUID destinationID);
	

	/**
	 * Generates a message that indicates the server is full.
	 * This method should be used by the server.
	 * @return
	 */
	public MessagePacket generateServerFullMessage();
	
	
	
	/**
	 * Generates a message that notifies the client that it is being kicked.
	 * @param destinationID
	 * @param reason
	 * @return
	 */
	public MessagePacket generateKickMessage(UUID destinationID, String reason);
	
}
