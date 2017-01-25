package com.bwyap.network.server;

import java.io.PrintStream;

import com.bwyap.utility.StreamLogger;

/**
 * Extension of a StreamLogger to be used on a server.
 * TODO
 * @author bwyap
 *
 */
public class ServerLogger extends StreamLogger {

	public ServerLogger(String name, PrintStream printStream) {
		super(name, printStream);
	}
}
