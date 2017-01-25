package com.bwyap.network.resource;

import org.json.simple.JSONObject;

import com.bwyap.utility.resource.JSONWrapper;

/**
 * A wrapper class for the settings config file.
 * Allows easy access to the different properties stored in the config file.
 * @author bwyap
 *
 */
public class Settings extends JSONWrapper {
	
	public Settings(JSONObject object) {
		super(object);
	}

	
	/**
	 * Get the default port
	 */
	public int getDefaultPort() {
		return getInteger(object, "default_port");
	}

	
	
	/**
	 * Validate the loaded settings file.
	 * @return
	 */
	@Override
	public boolean isValid() {
		try {
			getDefaultPort();
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}
	
	
}
