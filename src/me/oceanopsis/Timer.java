package me.oceanopsis;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Timer extends BukkitRunnable {

	Horror plugin;

	public Timer(Horror plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setFoodLevel(20);
			player.setHealth(20);
		}
		OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString("916b1e79-6529-4b3c-9bdc-7ab42acec194"));
		if (!plugin.playing.hasPlayer(op))
			if (!plugin.blue.hasPlayer(op))
				plugin.blue.addPlayer(op);
	}
}