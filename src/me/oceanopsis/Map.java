package me.oceanopsis;

import java.util.ArrayList;

import me.oceanopsis.util.Distance;
import me.oceanopsis.util.Yaml;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Map {

	public Yaml yaml;

	public ArrayList<Location> spawns = new ArrayList<Location>();

	public Map(Yaml yaml) {
		this.yaml = yaml;
	}

	public Map(Map map) {
		this.yaml = map.getYaml();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Location> refreshSpawns() {
		spawns.clear();
		ArrayList<String> newSpawns = null;
		if (yaml.contains("spawns"))
			newSpawns = (ArrayList<String>) yaml.get("spawns");
		else
			newSpawns = new ArrayList<String>();
		for (String string : newSpawns) {
			World world = Bukkit.getWorld(string.split(",")[0]);
			double x = Double.parseDouble(string.split(",")[1]);
			double y = Double.parseDouble(string.split(",")[2]);
			double z = Double.parseDouble(string.split(",")[3]);
			double yaw = Double.parseDouble(string.split(",")[4]);
			double pitch = Double.parseDouble(string.split(",")[5]);
			Location loc = new Location(world, x, y, z, (int) yaw, (int) pitch);
			spawns.add(loc);
		}
		return spawns;
	}

	public void playMusic() {
		refreshSpawns();
		for (Location loc : spawns) {
			loc.getWorld().playEffect(loc, Effect.RECORD_PLAY, Material.GOLD_RECORD);
		}
	}

	public void clearSpawns() {
		spawns.clear();
		yaml.set("spawns", null);
		yaml.save();
	}

	public Location getSafeSpawn(Player player) {
		refreshSpawns();
		int r = (int) (Math.random() * spawns.size());
		Location spawn = spawns.get(r);
		for (int i = 0; i < spawns.size(); i++) {
			Player closest = Distance.getClosestPlayer(spawn, 10);
			if (closest != null) {
				if (closest.getUniqueId() != player.getUniqueId()) {
					r = (int) (Math.random() * spawns.size());
					spawn = spawns.get(r);
				} else
					break;
			} else
				break;
		}
		return spawn;
	}

	@SuppressWarnings("unchecked")
	public void addSpawn(Location loc) {
		ArrayList<String> list = null;
		if (yaml.contains("spawns"))
			list = (ArrayList<String>) yaml.get("spawns");
		else
			list = new ArrayList<String>();
		String location = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
		list.add(location);
		yaml.set("spawns", list);
		yaml.save();
	}
	
	public void save() {
		this.yaml.save();
	}

	public Yaml getYaml() {
		return yaml;
	}
	
	public String getName() {
		return yaml.getName();
	}

}
