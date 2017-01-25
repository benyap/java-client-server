package com.bwyap.network.interpreter;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.bwyap.network.server.Server;
import com.bwyap.utility.interpreter.ConsoleReader;

/**
 * A Server interpreter that gets input from the console
 * @author bwyap
 *
 */
public class ConsoleServerInterpreter extends ServerInterpreter {

	protected Scanner scan;
	protected PrintStream out;
	protected ConsoleReader reader;
	
	public ConsoleServerInterpreter(Server server) {
		super(server);
		out = System.out;
		scan = new Scanner(System.in);
		reader = new ConsoleReader();
	}

	
	@Override
	public String getInput() {
		String s = null;
		try {
			s = reader.readLine(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return s;
		//return scan.nextLine();
	}
	
	
	@Override
	public void stop() {
		super.stop();
		reader.stop();
	}
	
	
	@Override
	public void println(String s) {
		out.println(s);
	}
	

	@Override
	public void println() {
		out.println();
	}


	@Override
	public void print(String s) {
		out.print(s);
	}
}
