package com.bwyap.network.authenticator;

import java.util.UUID;


/**
 * An interface providing methods for a client/server pair to perform an authentication handshake.
 * Both the client and server should use the same protocol.
 * @author bwyap
 *
 */
public interface AuthenticatorInterface {

	
	/**
	 * Sets the UUID of the object using the interface.
	 * This UUID will be used as the ID of the sender in any generated MessagePackets.
	 * @param id
	 */
	public void setUUID(UUID id);
	
	
}
