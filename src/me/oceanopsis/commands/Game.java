package me.oceanopsis.commands;

import me.oceanopsis.Horror;
import me.oceanopsis.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Game implements CommandExecutor {

	Horror plugin;

	public Game(Horror plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String label, String[] args) {
		if (label.equalsIgnoreCase("game")) {
			if (sender.isOp()) {
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("start")) {
						int i = plugin.getGame().start();
						if (i == 0)
							sender.sendMessage(ChatColor.YELLOW + "Game started");
						else
							sender.sendMessage(ChatColor.RED + "Game already started!");
						return true;
					}
					if (args[0].equalsIgnoreCase("stop")) {
						int i = plugin.getGame().stop(ChatColor.DARK_RED + "The Game Was Forcefully Stopped!");
						if (i == 0)
							sender.sendMessage(ChatColor.YELLOW + "Game stopped");
						else
							sender.sendMessage(ChatColor.RED + "Game not currently running!");
						return true;
					}
				} else {
					sendHelp(sender);
					return false;
				}
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (args.length == 2) {
						if (args[0].equalsIgnoreCase("newspawn")) {
							Map map = plugin.getMap(args[1].toLowerCase());
							map.addSpawn(player.getLocation());
							map.save();
							sender.sendMessage(ChatColor.YELLOW + "New spawn point created!");
						}
						if (args[0].equalsIgnoreCase("deleteallspawns")) {
							Map map = plugin.getMap(args[1].toLowerCase());
							map.clearSpawns();
							map.save();
							sender.sendMessage(ChatColor.YELLOW + "All spawn points deleted!");
						}
					} else {
						sendHelp(sender);
						return false;
					}
					if (args.length == 1) {
						if (args[0].equalsIgnoreCase("setlobbyspawn")) {
							Location loc = player.getLocation();
							String location = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
							plugin.config().set("lobby", location);
							plugin.config().save();
							sender.sendMessage(ChatColor.YELLOW + "Lobby spawn point set!");
						}
					} else {
						sendHelp(sender);
						return false;
					}
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Nope!");
			}
		}
		return false;
	}

	public void sendHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.AQUA + "Command Syntax:");
		sender.sendMessage(ChatColor.BLUE + "/game start - starts the game");
		sender.sendMessage(ChatColor.BLUE + "/game stop - stops the game");
		sender.sendMessage(ChatColor.BLUE + "/game newspawn [map] - creates a new spawn point for specified map");
		sender.sendMessage(ChatColor.BLUE + "/game deleteallspawns [map] - deletes all spawn points for specified map");
		sender.sendMessage(ChatColor.BLUE + "/game setlobbyspawn - sets lobby spawnpoint");
	}

}