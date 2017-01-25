package com.bwyap.network.driver.interpreter;

import java.io.PrintStream;
import java.util.Scanner;

import com.bwyap.network.driver.ClientServerDriver;


/**
 * A Domination interpreter that gets input from the console
 * @author bwyap
 *
 */
public class ConsoleDriverClientServerInterpreter extends DriverClientServerInterpreter {

	protected Scanner scan;
	protected PrintStream out;	
	
	
	public ConsoleDriverClientServerInterpreter(ClientServerDriver domination) {
		super(domination);
		out = System.out;
		scan = new Scanner(System.in);
	}

	
	@Override
	public String getInput() {
		return scan.nextLine();
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
