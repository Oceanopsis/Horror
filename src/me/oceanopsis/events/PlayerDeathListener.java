package me.oceanopsis.events;

import me.oceanopsis.GameControl;
import me.oceanopsis.Horror;
import me.oceanopsis.util.Yaml;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

	Horror plugin;

	public PlayerDeathListener(Horror plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player killed = event.getEntity();
		Player killer = killed.getKiller();
		Yaml killedYaml = plugin.getPlayerYaml(killed);
		if (killer instanceof Player) {
			Yaml killerYaml = plugin.getPlayerYaml(killer);

			killedYaml.set("deaths", killedYaml.getInteger("deaths") + (int) 1);
			killerYaml.set("kills", killerYaml.getInteger("kills") + (int) 1);
			killedYaml.save();
			killerYaml.save();
			if (GameControl.alive.contains(killed.getUniqueId()))
				GameControl.alive.remove(killed.getUniqueId());
			if (GameControl.alive.size() == 0)
				plugin.getGame().stop(ChatColor.DARK_RED + "The Hunter Wins!");
			plugin.scoreboard.resetScores(killed.getName());
		}
	}

}