package com.bwyap.network.authenticator;

import java.util.UUID;

import com.bwyap.network.message.MessagePacket;

/**
 * <b>Connection handshake</b><br>
 * The following methods can be used to perform a connection handshake to verify
 * a connection with a valid client/server.
 * <ol>
 * 	<li> Server: use {@code generateAuthenticationRequestMessage} to send message
 * 	<li> <b>Client</b>: check {@code isRequestingAuthentication} 
 * 	<li> <b>Client</b>: if valid, respond with {@code generateAuthenticator}
 * 	<li> Server: check that authentication is valid using {@code authenticateResponse}
 * 	<li> Server: inform client of valid authentication using {@code generateResponse}
 * 	<li> <b>Client</b>: confirm that authentication was successful using {@code confirmAuthentication}
 * </ol>
 * 
 * <p>
 * 
 * <b>Disconnect handshake</b><br>
 * The following methods can be used to perform a safe disconnect between a server and client.
 * <ol>
 * 	<li> <b>Client</b>: use {@code generateDisconnectRequest} to inform server of disconnect
 * 	<li> Server: check for a disconnect request using {@code isRequestingDisconnect}
 * 	<li> Server: acknowledge disconnect request by sending {@code generateDisconnectAckPacket}
 * 	<li> <b>Client</b>: confirm disconnect acknowledgement by using {@code disconnectAcknowledged}
 * </ol>
 * 
 * <p>
 * 
 * <b>Server full</b><br>
 * Signal to a connecting client that the server is full.
 * <ol>
 * 	<li> Server: use {@code generateServerFullMessage} to send to client
 * 	<li> <b>Client</b>: use {@code isServerFull} to check server full message 
 * </ol>
 * 
 * @author bwyap
 *
 */
public abstract class ClientAuthenticator extends Authenticator implements ClientAuthenticatorInterface {

	public ClientAuthenticator(UUID id) {
		super(id);
	}

	@Override
	public abstract boolean isRequestingAuthentication(MessagePacket m);

	@Override
	public abstract MessagePacket generateAuthenticator(UUID destinationID, String clientName);

	@Override
	public abstract boolean confirmAuthentication(MessagePacket m);

	@Override
	public abstract MessagePacket generateDisconnectRequest(UUID destinationID);
	
	@Override
	public abstract boolean disconnectAcknowledged(MessagePacket m);
	
	@Override
	public abstract boolean isServerFull(MessagePacket m);
	
}
