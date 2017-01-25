package com.bwyap.network.resource;

import java.io.File;

import com.bwyap.utility.resource.ResourceLoader;

/**
 * The Resource class is responsible for the loading of all resources required for the game and 
 * contains paths and file names for all resource files.
 * 
 * @author bwyap
 *
 */
public class Resource {
	
	public static final String IN_ROOT = "/com/bwyap/network/";
	
	/* ==============
	 * Internal paths
	 * ==============
	 */
	public static final String IN_RESPATH = IN_ROOT + "resource/defaultcontent/";
	
	
	/* ==============
	 * External files
	 * ==============
	 */
	public static final File EX_DATAFOLDER = new File("data/");
	//public static final File EX_SHADERFOLDER = new File("data/shader");
	public static final File EX_CONFIGFOLDER = new File("data/config");
	
	
	private static final File EX_DOMINATIONCONFIG_JSON = new File("data/config/config.json");
	
	
	/* ==========
	 * JSON files
	 * ==========
	 */
	public static Settings Settings;
	
	
	/* =======
	 * Shaders
	 * =======
	 */
	public static String vertexShaderCode;
	public static String fragmentShaderCode;
	
	public static String particleVertexShaderCode;
	public static String particleGeometryShaderCode;
	public static String particleFragmentShaderCode;
	
	
	/**
	 * Load all resources.
	 */
	public static void load() {
		loadFolders();
		loadConfig();
		//TODO
		//
	}
	
	
	
	
	/**
	 * Loads all folders required for the game content.
	 * If required directories do not exist, they are created.
	 */
	private static void loadFolders() {
		ResourceLoader.loadFolder(EX_DATAFOLDER);
		ResourceLoader.loadFolder(EX_CONFIGFOLDER);
		//TODO
		//
	}	
	
	
	
	
	/**
	 * Loads all config files.
	 */
	private static void loadConfig() {
		Settings = new Settings(ResourceLoader.loadJSON(EX_DOMINATIONCONFIG_JSON));
		if (!Settings.isValid()) {
			ResourceLoader.copyFileFromJar(EX_DOMINATIONCONFIG_JSON, null);
			Settings = new Settings(ResourceLoader.loadJSON(EX_DOMINATIONCONFIG_JSON));
		}
		
		//TODO
		//
	}
	
	
}
