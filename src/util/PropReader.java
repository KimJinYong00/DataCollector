package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.util.Properties;

public class PropReader {
	private static PropReader propReader = null;
	private static Properties prop = null;
	
	private PropReader() {
		init();
	}
	
	private void init() {
		String resource = "./conf/config.properties";
		try {
			File file = new File(resource);
			prop = new Properties();
			prop.load(new FileInputStream(file));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static PropReader getInstance() {
		if(propReader == null) {
			propReader = new PropReader();
		}
		
		return propReader;
	}
	
	public String getProperty(String key) {
		if(prop == null)
			return null;
		
		return prop.getProperty(key);
	}
}
