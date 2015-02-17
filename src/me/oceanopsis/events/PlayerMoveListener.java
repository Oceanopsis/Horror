package me.oceanopsis.events;

import java.util.ArrayList;
import java.util.UUID;

import me.oceanopsis.GameControl;
import me.oceanopsis.Horror;
import me.oceanopsis.util.Cooldowns;
import me.oceanopsis.util.Distance;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

	Horror plugin;

	public PlayerMoveListener(Horror plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (GameControl.hunter != null) {
			if (player.getUniqueId() == GameControl.hunter.getUniqueId()) {
				for (UUID uuid : GameControl.alive) {
					Player alive = Bukkit.getPlayer(uuid);
					ArrayList<Location> list = Distance.getLocationsBetweenLocations(player.getEyeLocation(), alive.getEyeLocation());
					boolean spook = true;
					for (Location loc : list) {
						if (loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.GLASS) {
							spook = false;
							break;
						}
					}
					if (spook) {
						if (Cooldowns.tryCooldown(alive, "spook", 5000)) {
							alive.playSound(alive.getLocation(), Sound.AMBIENCE_THUNDER, 2, (float) 0.5);
							alive.playSound(alive.getLocation(), Sound.AMBIENCE_CAVE, 2, (float) 2);
						}
					}
				}
			}
		}
	}

}
