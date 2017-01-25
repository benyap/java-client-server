package com.bwyap.network.message;

import java.io.Serializable;
import java.util.UUID;

/**
 * A packet that contains generic members to be sent over a socket connection.
 * Implements the interface {@code java.io.Serializable}.
 * @author bwyap
 *
 */
public class MessagePacket implements Serializable, Comparable<MessagePacket> {

	private static final long serialVersionUID = 9188696360691400003L;
	
	public long timestamp;
	public UUID senderID;
	public UUID receiverID;
	public MessageType type;
	public String[] data;
	
	/**
	 * Create a message packet with a specified timestamp
	 * @param timestamp
	 * @param senderID
	 * @param receiverID
	 * @param type
	 * @param data
	 */
	public MessagePacket(long timestamp, UUID senderID, UUID receiverID, MessageType type, String[] data) {
		this.timestamp = timestamp;
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.type = type;
		this.data = data;
	}
	
	
	/**
	 * Create a message packet with a timestamp with the current system time
	 * @param senderID
	 * @param receiverID
	 * @param type
	 * @param data
	 */
	public MessagePacket(UUID senderID, UUID receiverID, MessageType type, String[] data) {
		this.timestamp = System.currentTimeMillis();
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.type = type;
		this.data = data;
	}

	
	@Override
	public int compareTo(MessagePacket o) {
		return (int) (o.timestamp - this.timestamp);
	}
	
}
