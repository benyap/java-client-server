package com.bwyap.utility.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.bwyap.network.resource.Resource;
import com.bwyap.utility.StreamLogger;
import com.bwyap.utility.interpreter.Command;

public class ResourceLoader {
	
	private static StreamLogger logger = null;
	
	
	/**
	 * Loads a JSON object from a file on disk.
	 * If the file is not found on disk, a default version will be copied from the jar.
	 * @param file <tt>File</tt> which describes the location of the physical file
	 * @return
	 */
	public static JSONObject loadJSON(File file) {
		JSONObject jsonObject = null;
		
		if (!file.exists()) {
			log("Config file <" + file.getName() + "> not found. Using default config.");
			copyFileFromJar(file, null);
		}
		
		JSONParser parser = new JSONParser();
		try {
			Object o = parser.parse(new FileReader(file));
			jsonObject = (JSONObject) o;
		}
		catch (IOException e) {
			printStackTrace(e);
		}
		catch (ParseException e) {
			log("An error occurred while trying to parse JSON file <" + file.getName() + ">.");
			printStackTrace(e);
		}
		
		return jsonObject;
	}
	
	
	/**
	 * Reads an internal CSV file from within the jar and loads the data into a hash map.
	 * @param path
	 * @return
	 */
	public static HashMap<String, String[]> loadInternalCSV(String path) {
		log("Loading internal CSV @ " + path);
				
		BufferedReader txtReader = new BufferedReader(new InputStreamReader(ResourceLoader.class.getResourceAsStream(path)));
		HashMap<String, String[]> data = new HashMap<String, String[]>();
		
		try {
			for(String line; (line = txtReader.readLine()) != null; ) {
				String[] args = line.split(",");
				
				if (args.length > 0) {
					if (args[0].substring(0, 1).equals("#")) {
						//Line is a comment so it does not need to be loaded
					}
					else {
						data.put(args[0], args);
					}
				}
			 }
		} 
		catch (IOException e) {
			printStackTrace(e);
		}
		
		log("CSV file load complete.");
		return data;
	}
	
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public static HashMap<String, Command> loadCommands(String path) {
		HashMap<String, Command> commands = new HashMap<String, Command>();
		
		HashMap<String, String[]> csv = loadInternalCSV(path);
		for (String key : csv.keySet()) {
			String[] line = csv.get(key);
			commands.put(key, new Command(
					line[0], 					// Command name
					Integer.parseInt(line[1].trim()),	// min arguments
					Integer.parseInt(line[2].trim()),	// max arguments
					line[3], 					// usage
					line[4]));					// description
		}
		
		return commands;
	}
	
	

	/**
	 * Loads a text file as a string from within the jar file.
	 * @param file
	 * @return
	 */
	public static List<String> loadInternalTextFileAsList(String path) {
		//log("Loading internal text file @ " + file.getPath());
		List<String> lines = new ArrayList<String>();
		
		try (
				InputStream in = ResourceLoader.class.getClass().getResourceAsStream(path);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
			) {
				
				String line = null;
				
				while (true) {
					line = br.readLine();
					if (line != null) lines.add(line);
					else break;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		return lines;
	}
	
	
	/**
	 * Loads a text file as a string from within the jar file.
	 * @param file
	 * @return
	 */
	public static String loadInternalTextFileAsString(String path) {
		//log("Loading internal text file @ " + file.getPath());
		String s = "";
		
		for (String line : loadInternalTextFileAsList(path)) {
			s += line + "\n";
		}
		
		return s;
	}
	
	
	/**
	 * Loads a text file as a string.
	 * @param file
	 * @return
	 */
	public static String loadTextFile(File file) {
		//log("Loading text file @ " + file.getPath());
		String s = "";
		
		try (
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
		) {
			
			String line = null;
			
			while (true) {
				line = br.readLine();
				if (line != null) s += line + "\n";
				else break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return s;
	}

	
	/**
	 * Copy an internal file to an external location.
	 * If the parameter {@code internalPath} is {@code null} 
	 * the path of {@code file} will be used to search within the the 
	 * {@code IN_RESPATH} folder for the specified file.
	 * <p>
	 * WARNING: this will overwrite anything that is in {@code file}.
	 * @param file the external file to copy the internal file to
	 * @param internalPath the path of the internal file. This can be null.
	 */
	public static void copyFileFromJar(File file, String internalPath) {
		InputStream in = null;
		
		try (PrintWriter out = new PrintWriter(file.getPath(), "UTF-8"); 
			FileOutputStream outStream = new FileOutputStream(file)) {
			
			if (internalPath == null) {
				in = ResourceLoader.class.getClass().getResourceAsStream(Resource.IN_RESPATH + file.getPath());
			}
			else {
				in = ResourceLoader.class.getClass().getResourceAsStream(internalPath);
			}
			
			log("Copying file <" + file.getName() + ">");
			while (in.available() > 0) {
				out.write(in.read());
			}
			log("File <" + file.getName() + "> copy complete.");
		}
		catch (Exception e) {
			log("ERROR occured when writing <" + file.getName() + "> to file.");
			printStackTrace(e);
		}
	}
	

	/**
	 * Creates a folder if it is valid
	 * @param file the <tt>File</tt> representing the directory
	 */
	public static void loadFolder(File file) {
		if(!file.isDirectory() | !file.exists()) {
			log("Directory <" + file.getPath() + "> not found.");
			file.mkdirs();
			log("<" + file.getPath() + "> created.");
		}
	}

	
	/**
	 * Sets the logger for the resource loader to use
	 * @param logger
	 */
	public static void setLogger(StreamLogger logger) {
		ResourceLoader.logger = logger;
	}
	
	
	/**
	 * Log a message to the logger if it has been initialised.
	 * Otherwise, the message is printed to the {@code System.out} stream
	 * @param message
	 */
	private static void log(String message) {		
		if (logger != null) {
			logger.pushResource(message);
		}
		else System.out.println("RES >> " + message);
	}
	
	
	/**
	 * A private method for printing stack traces to a logger if it exists.
	 * If a custom logger has not been assigned, traces are printed to their default location.
	 * @param e
	 */
	private static void printStackTrace(Exception e) {
		if (logger != null)	e.printStackTrace(logger.getStream());
		else e.printStackTrace();
	}
	
}
