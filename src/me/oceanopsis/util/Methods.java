package me.oceanopsis.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Methods {

	public static void playFirework(Location location, Color color) {
		CustomEntityFirework.spawn(location, FireworkEffect.builder().withColor(color).build());
	}

	public static ItemStack setSkin(ItemStack item, String nick) {
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwner(nick);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack createItem(ItemStack i, String name, String lore) {
		ArrayList<String> a = new ArrayList<String>();
		a.clear();
		a.add(lore);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + name);
		meta.setLore(a);
		i.setItemMeta(meta);
		return i;
	}

	public static ItemStack createColorArmor(ItemStack i, Color c) {
		LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
		meta.setColor(c);
		i.setItemMeta(meta);
		return i;
	}

	public static int pingPlayer(Player who) {
		try {
			// Building the version of the server in such a form we can use it
			// in NMS code.
			String bukkitversion = Bukkit.getServer().getClass().getPackage().getName().substring(23);
			// Getting craftplayer
			Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit." + bukkitversion + ".entity.CraftPlayer");
			// Invoking method getHandle() for the player
			Object handle = craftPlayer.getMethod("getHandle").invoke(who);
			// Getting field "ping" that holds player's ping obviously
			Integer ping = (Integer) handle.getClass().getDeclaredField("ping").get(handle);
			// Returning the ping
			return ping.intValue();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			// Handle exceptions however you like, i chose to return value of
			// -1; since player's ping can't be -1.
			return -1;
		}
	}

	public static void sendMessage(Player player, String text) {
		StatusBar.setStatusBar(player, text, 1);
	}

}
