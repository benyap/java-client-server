package com.bwyap.utility.interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An interruptible console input reader. 
 * This allows a program to read from the command line without the process being blocked.
 * <p>
 * {@link www.javaspecialists.eu/archive/Issu153.html}
 */
public class ConsoleReader {
	
	private boolean running = false;
	
	
	/**
	 * Read a line from {@code System.in} in a manner which can be interrupted.
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public String readLine(int timeout, TimeUnit unit) throws InterruptedException {
		ExecutorService ex = Executors.newSingleThreadExecutor();
		
		String input = null;
		running = true;
		
		try {
			while (running) {
				Future<String> result = ex.submit(new Reader());
				try {
					input = result.get(timeout, unit);
					break;
				}
				catch (ExecutionException e) {
					e.getCause().printStackTrace();
				}
				catch (TimeoutException e) {
					result.cancel(true);
				}
			}
		}
		finally {
			ex.shutdown();
		}
		
		return input;
	}
	
	
	/**
	 * Stop the reader from polling the input line.
	 */
	public void stop() {
		running = false;
	}
	
	
	private class Reader implements Callable<String> {
		@Override
		public String call() throws IOException {
			
			String input;
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
			do {
				try {
					while (!br.ready()) {
						Thread.sleep(100);
					}
					input = br.readLine();
				}
				catch (InterruptedException e) {
					//System.out.println("ConsoleReader interrupted.");
					return null;
				}
			} while ("".equals(input));
			
			return input;
		}
	}

	
}
