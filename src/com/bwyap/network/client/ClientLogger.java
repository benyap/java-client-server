package com.bwyap.network.client;

import java.io.PrintStream;

import com.bwyap.utility.StreamLogger;

/**
 * Extension of a StreamLogger to be used by a client.
 * TODO
 * @author bwyap
 *
 */
public class ClientLogger extends StreamLogger {

	public ClientLogger(String name, PrintStream printStream) {
		super(name, printStream);
	}

}
