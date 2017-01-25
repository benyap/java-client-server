package com.bwyap.network.authenticator;

import java.util.UUID;

import com.bwyap.network.message.MessagePacket;

/**
 * <b>Connection handshake</b><br>
 * The following methods can be used to perform a connection handshake to verify
 * a connection with a valid client/server.
 * <ol>
 * 	<li> <b>Server</b>: use {@code generateAuthenticationRequestMessage} to send message
 * 	<li> Client: check {@code isRequestingAuthentication} 
 * 	<li> Client: if valid, respond with {@code generateAuthenticator}
 * 	<li> <b>Server</b>: check that authentication is valid using {@code authenticateResponse}
 * 	<li> <b>Server</b>: inform client of valid authentication using {@code generateResponse}
 * 	<li> Client: confirm that authentication was successful using {@code confirmAuthentication}
 * </ol>
 * 
 * <p>
 * 
 * <b>Disconnect handshake</b><br>
 * The following methods can be used to perform a safe disconnect between a server and client.
 * <ol>
 * 	<li> Client: use {@code generateDisconnectRequest} to inform server of disconnect
 * 	<li> <b>Server</b>: check for a disconnect request using {@code isRequestingDisconnect}
 * 	<li> <b>Server</b>: acknowledge disconnect request by sending {@code generateDisconnectAckPacket}
 * 	<li> Client: confirm disconnect acknowledgement by using {@code disconnectAcknowledged}
 * </ol>
 * 
 * <p>
 * 
 * <b>Server full</b><br>
 * Signal to a connecting client that the server is full.
 * <ol>
 * 	<li> <b>Server</b>: use {@code generateServerFullMessage} to send to client
 * 	<li> Client: use {@code isServerFull} to check server full message 
 * </ol>
 * 
 * @author bwyap
 *
 */
public abstract class ServerAuthenticator extends Authenticator implements ServerAuthenticatorInterface {

	public ServerAuthenticator(UUID id) {
		super(id);
	}

	@Override
	public abstract MessagePacket generateAuthenticationRequestMessage(UUID destinationID);

	@Override
	public abstract boolean authenticateResponse(MessagePacket m);

	@Override
	public abstract MessagePacket generateResponse(UUID destinationID);
	
	@Override
	public abstract boolean isRequestingDisconnect(MessagePacket m);
	
	@Override
	public abstract MessagePacket generateDisconnectAckPacket(UUID destinationID);

	@Override
	public abstract MessagePacket generateServerFullMessage();
	
}
