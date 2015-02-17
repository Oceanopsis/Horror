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
		try {
			Object nmsPlayer = killed.getClass().getMethod("getHandle").invoke(killed);
			Object packet = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".PacketPlayInClientCommand").newInstance();
			Class<?> enumClass = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EnumClientCommand");

			for (Object ob : enumClass.getEnumConstants()) {
				if (ob.toString().equals("PERFORM_RESPAWN")) {
					packet = packet.getClass().getConstructor(enumClass).newInstance(ob);
				}
			}

			Object con = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
			con.getClass().getMethod("a", packet.getClass()).invoke(con, packet);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}