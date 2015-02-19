package me.oceanopsis.commands;

import me.oceanopsis.Horror;
import me.oceanopsis.util.Methods;
import me.oceanopsis.util.Yaml;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Stats implements CommandExecutor {

	Horror plugin;

	public Stats(Horror plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String label, String[] args) {
		if (label.equalsIgnoreCase("stats")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					new BukkitRunnable() {
						double d = 0;
						int size = 30;
						double pattern = (double) 3 / (double) 11;
						Location loc = ((Player) sender).getLocation();

						@Override
						public void run() {
							// get a point on a rose based on the variables
							// given
							double t = (Math.PI / 180) * d;
							double rm = size * Math.cos(pattern * t);
							// convert to x
							double x = Math.sin(d) * rm;
							// convert to z
							double z = Math.cos(d) * rm;
							Location rose = new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z);
							rose.getBlock().setType(Material.STONE);
							plugin.getLogger().info("d: " + d);
							d += 1;
							if (d >= 360)
								this.cancel();
						}
					}.runTaskTimer(plugin, 0L, 1L);
					Yaml yaml = plugin.getPlayerYaml((Player) sender);
					sender.sendMessage(ChatColor.DARK_RED + "Your stats");
					sender.sendMessage(ChatColor.YELLOW + "Kills: " + yaml.getInteger("kills"));
					sender.sendMessage(ChatColor.YELLOW + "Deaths: " + yaml.getInteger("deaths"));
					sender.sendMessage(ChatColor.GREEN + "Ping: " + Methods.pingPlayer((Player) sender));
				} else {
					sender.sendMessage("Console can't have stats!");
				}
			} else if (args.length == 1) {
				OfflinePlayer target = null;
				try {
					target = Bukkit.getOfflinePlayer(args[0]);
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Could not find player: " + args[0]);
					return false;
				}
				Yaml yaml = plugin.getPlayerYaml(target.getUniqueId());
				sender.sendMessage(ChatColor.DARK_RED + "Stats for: " + ChatColor.GOLD + target.getName());
				sender.sendMessage(ChatColor.YELLOW + "Kills: " + yaml.getInteger("kills"));
				sender.sendMessage(ChatColor.YELLOW + "Deaths: " + yaml.getInteger("deaths"));
			}
		}
		return false;
	}

}
