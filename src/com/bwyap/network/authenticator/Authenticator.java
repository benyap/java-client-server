package com.bwyap.network.authenticator;

import java.util.UUID;

/**
 * An abstract class that contains constructors for an AuthenticatorInterface.
 * Must be extended to provide concrete implementations of the methods of AuthenticatorInterface
 * @author bwyap
 *
 */
public abstract class Authenticator implements AuthenticatorInterface {
	
	protected UUID id; 
	
	public Authenticator(UUID id) { setUUID(id); }
	
	@Override
	public void setUUID(UUID id) { this.id = id; }
	
}
