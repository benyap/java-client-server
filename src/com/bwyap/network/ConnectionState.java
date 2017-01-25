package com.bwyap.network;

/**
 * Enumeration of states of a connection.
 * 
 * <p>
 * NEW:
 * A new connection.
 * 
 * <p>
 * AUTHENTICATE:
 * A state where only authentication data is sent to verify a connection
 * 
 * <p>
 * CONNECTED: 
 * A state where the connection has been authenticated and is free to send data
 * 
 * <p>
 * DISCONNECTING:
 * A state where the connection is requesting it be disconnected safely
 * 
 * <p>
 * TERMINATED:
 * The connection has been terminated.
 * 
 * @author bwyap
 *
 */
public enum ConnectionState {
	NEW,
	AUTHENTICATE, 
	CONNECTED, 
	DISCONNECTING,
	TERMINATED
}
