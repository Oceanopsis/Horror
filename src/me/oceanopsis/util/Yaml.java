package me.oceanopsis.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Yaml {

	File file;

	FileConfiguration fileConfig;

	public Yaml(String directory, String string) {
		file = new File(directory, string + ".yml");
		fileConfig = new YamlConfiguration();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void load() {
		try {
			fileConfig.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			fileConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean contains(String s) {
		return fileConfig.contains(s);
	}

	public void set(String s, Object o) {
		fileConfig.set(s, o);
	}

	public Double getDouble(String s) {
		return fileConfig.getDouble(s);
	}

	public Integer getInteger(String s) {
		return fileConfig.getInt(s);
	}

	public Object get(String s) {
		return fileConfig.get(s);
	}

	public String getString(String s) {
		return fileConfig.getString(s);
	}
	
	public String getName() {
		 return file.getName().replace(".yml", "");
	}

}