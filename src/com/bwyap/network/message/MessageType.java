package com.bwyap.network.message;

import java.io.Serializable;

/**
 * Enumeration of message types sent between a client and server
 * @author bwyap
 * @version 1.0
 *
 */
public enum MessageType implements Serializable {
	MESSAGE,
	MESSAGE_BROADCAST,
	MESSAGE_CHAT,
	CONNECTION,
	KICK,
	SERVER_FULL;
}
