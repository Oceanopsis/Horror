package me.oceanopsis.events;

import me.oceanopsis.Horror;
import me.oceanopsis.util.Yaml;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

	Horror plugin;

	public PlayerJoinListener(Horror plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (plugin.playing.hasPlayer(player))
			plugin.playing.removePlayer(player);

		if (!player.hasPlayedBefore()) {
			Yaml yaml = plugin.getPlayerYaml(player);
			yaml.set("kills", 0);
			yaml.set("deaths", 0);
			yaml.save();
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " title " + "{text:\"Welcome!\",color:gold}");
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " subtitle " + "{text:\"To your worst nightmare\",color:dark_red}");
				}
			}.runTaskLater(plugin, 40L);
		}
	}

}