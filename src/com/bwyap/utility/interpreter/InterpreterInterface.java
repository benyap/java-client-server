package com.bwyap.utility.interpreter;

public interface InterpreterInterface {


	/**
	 * Checks if a given command is valid.
	 * For the command to be considered valid,
	 * it must be a recognised command and have the correct number of parameters.
	 * The validity of the parameters is NOT validated in this method.
	 * @param arguments
	 * @return
	 */
	public boolean validate(String[] args);
	
	
	/**
	 * Returns true if the interpreter is listening for input.
	 * @return
	 */
	public boolean isRunning();
	
	
	/**
	 * Stops the interpreter thread
	 */
	public void stop();
	
	
	/**
	 * Print a message to display to the user.
	 * @param s
	 */
	public void print(String s);
	
	
	/**
	 * Print a message on a new line to display to the user.
	 * @param s
	 */
	public void println(String s);
	
	
	public void println();
	
}
