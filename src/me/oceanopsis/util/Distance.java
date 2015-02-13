package me.oceanopsis.util;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Distance {

	public static Location getHighestPoint(Location loc) {
		int y = 256;

		// couldn't just use the for loop value as y, not sure why, it just
		// didn't work
		for (int i = 0; i < 256; i++) {
			Location newLoc = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
			if (newLoc.getBlock().getType().isSolid()) {
				return newLoc;
			}
			y--;
		}
		return null;
	}

	public static Location getTargetLocation(Player player, int distance) {
		Location loc = player.getEyeLocation();

		Location location = null;

		// rectangular coordinates
		double px = loc.getX();
		double py = loc.getY();
		double pz = loc.getZ();

		// yaw and pitch
		double yaw = Math.toRadians(loc.getYaw() + 90);
		double pitch = Math.toRadians(loc.getPitch() + 90);

		// polar coordinates
		double x = Math.sin(pitch) * Math.cos(yaw);
		double y = Math.sin(pitch) * Math.sin(yaw);
		double z = Math.cos(pitch);

		// loop for the distance
		for (int i = 1; i <= distance; i++) {
			// add to polar coordinates then translate them to rectangular so
			// that bukkit understands what we're telling it
			Location loc1 = new Location(player.getWorld(), px + i * x, py + i * z, pz + i * y);
			// if not solid
			if (!loc1.getBlock().getType().isSolid()) {
				location = loc1;
				break;
			}
			if (i == distance) {
				if (location == null) {
					location = loc1;
				}
			}
		}
		return location;
	}

	public static ArrayList<Entity> getEntitiesBetweenPlayerAndBlockLookingAt(Player player, int distance) {
		Location loc = player.getEyeLocation();

		ArrayList<Entity> entitylist = new ArrayList<Entity>();

		// rectangular coordinates
		double px = loc.getX();
		double py = loc.getY();
		double pz = loc.getZ();

		// yaw and pitch
		double yaw = Math.toRadians(loc.getYaw() + 90);
		double pitch = Math.toRadians(loc.getPitch() + 90);

		// polar coordinates
		double x = Math.sin(pitch) * Math.cos(yaw);
		double y = Math.sin(pitch) * Math.sin(yaw);
		double z = Math.cos(pitch);

		for (int i = 1; i <= distance; i++) {
			// increment polar coordinates then convert to rectangular
			Location loc1 = new Location(player.getWorld(), px + i * x, py + i * z, pz + i * y);
			// get entities in 3 block radius
			for (Entity e : Distance.getEntitiesInRadius(loc1, 3)) {
				// add them to the list
				entitylist.add(e);
			}
		}
		return entitylist;
	}

	public static ArrayList<Block> getBlocksBetweenPlayerAndBlockLookingAt(Player player, int distance) {
		Location loc = player.getEyeLocation();

		ArrayList<Block> blocklist = new ArrayList<Block>();

		// rectangular coordinates
		double px = loc.getX();
		double py = loc.getY();
		double pz = loc.getZ();

		// yaw and pitch
		double yaw = Math.toRadians(loc.getYaw() + 90);
		double pitch = Math.toRadians(loc.getPitch() + 90);

		// polar coordinates
		double x = Math.sin(pitch) * Math.cos(yaw);
		double y = Math.sin(pitch) * Math.sin(yaw);
		double z = Math.cos(pitch);

		for (int i = 1; i <= distance; i++) {
			// increment polar coordinates then convert to rectangular
			Location loc1 = new Location(player.getWorld(), px + i * x, py + i * z, pz + i * y);
			// add the block at location to list
			blocklist.add(loc1.getBlock());
		}
		return blocklist;
	}

	// same thing as above but with locations instead of blocks
	public static ArrayList<Location> getLocationsBetweenPlayerAndBlockLookingAt(Player player, int distance) {
		Location loc = player.getEyeLocation();

		ArrayList<Location> loclist = new ArrayList<Location>();

		double px = loc.getX();
		double py = loc.getY();
		double pz = loc.getZ();

		double yaw = Math.toRadians(loc.getYaw() + 90);
		double pitch = Math.toRadians(loc.getPitch() + 90);

		double x = Math.sin(pitch) * Math.cos(yaw);
		double y = Math.sin(pitch) * Math.sin(yaw);
		double z = Math.cos(pitch);

		for (int i = 1; i <= distance; i++) {
			Location loc1 = new Location(player.getWorld(), px + i * x, py + i * z, pz + i * y);
			loclist.add(loc1);
		}
		return loclist;
	}

	public static Location lookAt(Location loc, Location lookat) {

		loc = loc.clone();

		// distance between x, y, and z
		double dx = lookat.getX() - loc.getX();
		double dy = lookat.getY() - loc.getY();
		double dz = lookat.getZ() - loc.getZ();

		if (dx != 0) {
			if (dx < 0) {
				loc.setYaw((float) (1.5 * Math.PI));
			} else {
				loc.setYaw((float) (0.5 * Math.PI));
			}
			loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
		} else if (dz < 0) {
			loc.setYaw((float) Math.PI);
		}

		double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

		loc.setPitch((float) -Math.atan(dy / dxz));

		loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
		loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

		return loc;
	}

	public static Location move(Location loc, Vector offset) {

		float ryaw = -loc.getYaw() / 180f * (float) Math.PI;
		float rpitch = loc.getPitch() / 180f * (float) Math.PI;

		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		z -= offset.getX() * Math.sin(ryaw);
		z += offset.getY() * Math.cos(ryaw) * Math.sin(rpitch);
		z += offset.getZ() * Math.cos(ryaw) * Math.cos(rpitch);
		x += offset.getX() * Math.cos(ryaw);
		x += offset.getY() * Math.sin(rpitch) * Math.sin(ryaw);
		x += offset.getZ() * Math.sin(ryaw) * Math.cos(rpitch);
		y += offset.getY() * Math.cos(rpitch);
		y -= offset.getZ() * Math.sin(rpitch);
		return new Location(loc.getWorld(), x, y, z, loc.getYaw(), loc.getPitch());
	}

	public static ArrayList<Player> getPlayersInRadius(Location loc, double radius) {

		ArrayList<Player> players = new ArrayList<Player>();

		double i1 = loc.getX();
		double j1 = loc.getY();
		double k1 = loc.getZ();

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getWorld().equals(loc.getWorld())) {
				double i2 = player.getLocation().getX();
				double j2 = player.getLocation().getY();
				double k2 = player.getLocation().getZ();

				double ad = (i2 - i1) * (i2 - i1) + (j2 - j1) * (j2 - j1) + (k2 - k1) * (k2 - k1);

				if (ad < radius * radius) {
					players.add(player);
				}
			}
		}
		return players;
	}

	public static ArrayList<Player> getPlayersInRadius(Player p, double radius) {

		Location loc = p.getLocation();

		ArrayList<Player> players = new ArrayList<Player>();

		double i1 = loc.getX();
		double j1 = loc.getY();
		double k1 = loc.getZ();

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getUniqueId() != p.getUniqueId()) {
				if (player.getWorld().equals(loc.getWorld())) {
					double i2 = player.getLocation().getX();
					double j2 = player.getLocation().getY();
					double k2 = player.getLocation().getZ();

					double ad = (i2 - i1) * (i2 - i1) + (j2 - j1) * (j2 - j1) + (k2 - k1) * (k2 - k1);

					if (ad < radius * radius) {
						players.add(player);
					}
				}
			}
		}
		return players;
	}

	public static ArrayList<Entity> getEntitiesInRadius(Location loc, double radius) {

		ArrayList<Entity> entities = new ArrayList<Entity>();

		double i1 = loc.getX();
		double j1 = loc.getY();
		double k1 = loc.getZ();

		for (Entity e : loc.getWorld().getEntities()) {
			if (e.getWorld().equals(loc.getWorld())) {
				double i2 = e.getLocation().getX();
				double j2 = e.getLocation().getY();
				double k2 = e.getLocation().getZ();

				double ad = (i2 - i1) * (i2 - i1) + (j2 - j1) * (j2 - j1) + (k2 - k1) * (k2 - k1);

				if (ad < radius * radius) {
					entities.add(e);
				}
			}
		}
		return entities;
	}

	public static ArrayList<Entity> getEntitiesInRadius(Entity entity, double radius) {
		
		Location loc = entity.getLocation();

		ArrayList<Entity> entities = new ArrayList<Entity>();

		double i1 = loc.getX();
		double j1 = loc.getY();
		double k1 = loc.getZ();

		for (Entity e : loc.getWorld().getEntities()) {
			if (e.getUniqueId() != entity.getUniqueId()) {
				if (e.getWorld().equals(loc.getWorld())) {
					double i2 = e.getLocation().getX();
					double j2 = e.getLocation().getY();
					double k2 = e.getLocation().getZ();

					double ad = (i2 - i1) * (i2 - i1) + (j2 - j1) * (j2 - j1) + (k2 - k1) * (k2 - k1);

					if (ad < radius * radius) {
						entities.add(e);
					}
				}
			}
		}
		return entities;
	}

	public static Entity getClosestEntity(Entity e, double radius) {

		double minimalDistance = Math.pow(radius, 2);
		double curDist;
		Entity closest = null;
		for (Entity p : e.getWorld().getEntities()) {
			if (!p.equals(e)) {
				curDist = e.getLocation().distanceSquared(p.getLocation());
				if (curDist < minimalDistance) {
					minimalDistance = curDist;
					closest = p;
				}
			}
		}
		return closest;
	}

	public static Entity getClosestEntity(Location loc, double radius) {

		double minimalDistance = Math.pow(radius, 2);
		double curDist;
		Entity closest = null;
		for (Entity p : loc.getWorld().getEntities()) {
			curDist = loc.distanceSquared(p.getLocation());
			if (curDist < minimalDistance) {
				minimalDistance = curDist;
				closest = p;
			}
		}
		return closest;
	}

	public static Player getClosestPlayer(Entity e, double radius) {

		double minimalDistance = Math.pow(radius, 2);
		double curDist;
		Player closest = null;
		for (Player p : Bukkit.getOnlinePlayers()) {
			curDist = e.getLocation().distanceSquared(p.getLocation());
			if (curDist < minimalDistance) {
				minimalDistance = curDist;
				closest = p;
			}
		}
		return closest;
	}

	public static Player getClosestPlayer(Location loc, double radius) {

		double minimalDistance = Math.pow(radius, 2);
		double curDist;
		Player closest = null;
		for (Player p : Bukkit.getOnlinePlayers()) {
			curDist = loc.distanceSquared(p.getLocation());
			if (curDist < minimalDistance) {
				minimalDistance = curDist;
				closest = p;
			}
		}
		return closest;
	}
}