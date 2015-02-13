package me.oceanopsis.commands;

import me.oceanopsis.Horror;
import me.oceanopsis.util.Methods;
import me.oceanopsis.util.Yaml;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
					((Player) sender).playSound(((Player) sender).getLocation(), Sound.AMBIENCE_THUNDER, 2, (float) 0.5);
					((Player) sender).playSound(((Player) sender).getLocation(), Sound.AMBIENCE_CAVE, 2, (float) 2);
					Yaml yaml = plugin.getPlayerYaml((Player)sender);
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
