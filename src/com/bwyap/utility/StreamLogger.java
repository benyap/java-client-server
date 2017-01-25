package com.bwyap.utility;

import java.io.PrintStream;

/**
 * A wrapper for a collection of methods that print to a PrintStream.
 * The logger specifies a name and a line level at the start of each line.
 * Line levels are one of: 
 * {@code RECEIVE}, {@code SEND}, {@code INFO}, {@code MESSAGE} or {@code ERROR}.
 * @author bwyap
 *
 */
public class StreamLogger {

	
	static enum LineLevel {
		RECEIVE, SEND, INFO, MESSAGE, ERROR, CONNECTION, RESOURCE;
		
		@Override
		public String toString() {
			switch (this) {
			case ERROR:	 	return "ERR";
			case INFO:		return "INF";
			case CONNECTION:return "CON";
			case MESSAGE:	return "MSG";
			case RECEIVE:	return "RCV";
			case SEND:		return "SND";
			case RESOURCE:	return "RES";
			}
			return null;
		}
	}
	

	static enum DebugLevel {
		DEBUG, NORMAL;
	}
	
	private String name;
	private PrintStream printStream;
	private DebugLevel debugLevel;
	
	
	private static LineLevel[] LEVEL_DEBUG = {
			LineLevel.ERROR, 
			LineLevel.INFO, 
			LineLevel.CONNECTION, 
			LineLevel.MESSAGE, 
			LineLevel.RECEIVE, 
			LineLevel.SEND, 
			LineLevel.RESOURCE
	};
	
	
	private static LineLevel[] LEVEL_NORMAL = {
			LineLevel.ERROR, 
			LineLevel.INFO, 
			LineLevel.CONNECTION, 
			LineLevel.MESSAGE,
			LineLevel.RESOURCE
	};

	
	public StreamLogger(String name, PrintStream printStream) {
		this.name = name;
		this.printStream = printStream;
		this.debugLevel = DebugLevel.NORMAL;
	}
	
	
	/**
	 * Gets the stream the logger is outputting to
	 * @return
	 */
	public PrintStream getStream() {
		return printStream;
	}
	
	/**
	 * Push a receive line
	 * @param message
	 */
	public void pushReceive(String message) {
		pushLine(LineLevel.RECEIVE, message);
	}
	
	/**
	 * Push an error line
	 * @param message
	 */
	public void pushError(String message) {
		pushLine(LineLevel.ERROR, message);
	}

	/**
	 * Push an info line
	 * @param message
	 */
	public void pushInfo(String message) {
		pushLine(LineLevel.INFO, message);
	}
	
	/**
	 * Push a connection line
	 * @param message
	 */
	public void pushCon(String message) {
		pushLine(LineLevel.CONNECTION, message);
	}
	
	/**
	 * Push a message line
	 * @param message
	 */
	public void pushMessage(String message) {
		pushLine(LineLevel.MESSAGE, message);
	}
	
	/**
	 * Push a send line
	 * @param message
	 */
	public void pushSend(String message) {
		pushLine(LineLevel.SEND, message);
	}
	
	/**
	 * Push a resource line
	 * @param message
	 */
	public void pushResource(String message) {
		pushLine(LineLevel.RESOURCE, message);
	}

	/**
	 * Push a line to the server console at the specified level
	 * @param level
	 * @param message
	 */
	public void pushLine(LineLevel level, String message) {
		
		LineLevel[] acceptedLevels = null;
		
		switch (debugLevel) {
			case NORMAL: acceptedLevels = LEVEL_NORMAL; break;
			case DEBUG: acceptedLevels = LEVEL_DEBUG; break;
		}
		
		//Check accepted levels filter
		for (int i = 0; i < acceptedLevels.length; i++) {
			if (acceptedLevels[i] == level) {
				String header = "[" + Thread.currentThread().getName() + "] ";
				String tail =  " " + name + "> " + message;
				
				printStream.println(header + level + tail);
				return;
			}
		}
	}
	
	
	/**
	 * Set the name of the logger
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Get the name of the logger
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Set the debug level of the logger.
	 * @param level
	 */
	public void setDebugLevel(DebugLevel level) {
		this.debugLevel = level;
	}
	
	
	/**
	 * Get the current debug level of the logger.
	 * @return
	 */
	public DebugLevel getDebugLevel() {
		return debugLevel;
	}
}
