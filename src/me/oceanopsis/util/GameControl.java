package me.oceanopsis.util;

import java.util.ArrayList;
import java.util.UUID;

import me.oceanopsis.Horror;
import me.oceanopsis.util.Methods;
import me.oceanopsis.util.ReflectionUtils;
import me.oceanopsis.util.ReflectionUtils.RefClass;
import me.oceanopsis.util.ReflectionUtils.RefConstructor;
import me.oceanopsis.util.ReflectionUtils.RefField;
import me.oceanopsis.util.ReflectionUtils.RefMethod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

public class GameControl extends BukkitRunnable {

	private Horror plugin;

	// hunter
	public static Player hunter;
	
	public static ArrayList<UUID> alive = new ArrayList<UUID>();

	// reflection
	RefClass classPacket = ReflectionUtils.getRefClass("{nms}.Packet");
	RefClass classCraftPlayer = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
	RefMethod methodGetHandle = classCraftPlayer.getMethod("getHandle");
	RefClass classEntityPlayer = ReflectionUtils.getRefClass("{nms}.EntityPlayer");
	RefField fieldPlayerConnection = classEntityPlayer.getField("playerConnection");
	RefClass classPlayerConnection = ReflectionUtils.getRefClass("{nms}.PlayerConnection");
	RefMethod methodSendPacket = classPlayerConnection.getMethod("sendPacket", classPacket);

	RefClass classChatSerializer = ReflectionUtils.getRefClass("{nms}.ChatSerializer");
	RefClass classPacketPlayOutChat = ReflectionUtils.getRefClass("{nms}.PacketPlayOutChat");
	RefClass classIChatBaseComponent = ReflectionUtils.getRefClass("{nms}.IChatBaseComponent");
	RefConstructor ppoc = classPacketPlayOutChat.getConstructor(classIChatBaseComponent, byte.class);

	// is the game running
	public static boolean running = false;

	// counts the time left in the game
	int counter = 0;

	public GameControl(Horror plugin) {
		this.plugin = plugin;
	}

	public int start() {
		// is the game not already going on
		if (!running) {

			// 180 seconds, or 3 minutes, or the length of disc 13
			counter = 180;
			this.runTaskTimer(plugin, 0L, 20L);

			// reset scoreboard
			plugin.objective = plugin.scoreboard.getObjective("Info");
			if (plugin.objective != null)
				plugin.objective.unregister();
			plugin.objective = plugin.scoreboard.registerNewObjective("Info", "dummy");
			plugin.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

			// get a random index for players on the server
			int random = (int) Math.round(Math.random() * (Bukkit.getOnlinePlayers().size() - 1));
			int i = 0;
			int id = 1;
			for (Player player : Bukkit.getOnlinePlayers()) {

				player.setGameMode(GameMode.ADVENTURE);
				// find one random player
				if (i == random) {

					// they are the hunter
					hunter = player;

					// tell them they are the hunter
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " subtitle " + "{text:\"Eliminate all other players\",color:red}");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " title " + "{text:\"You are the Hunter\",color:dark_red}");
					PlayerInventory inv = player.getInventory();

					// spawn them
					Location spawn = plugin.getCurrentMap().getSafeSpawn(player);
					player.teleport(spawn);

					// they are slow
					player.setWalkSpeed((float) 0.12);

					// set their inventory contents
					inv.clear();
					ItemStack item = Methods.createItem(new ItemStack(Material.DIAMOND_AXE), ChatColor.DARK_RED + "Hunters Axe", null);
					item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 10);
					item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
					inv.addItem(item);
					inv.setHelmet(Methods.createColorArmor(new ItemStack(Material.LEATHER_HELMET), Color.RED));
					inv.setChestplate(Methods.createColorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.RED));
					inv.setLeggings(Methods.createColorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.RED));
					inv.setBoots(Methods.createColorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.RED));
					break;
				} else {
					// if they are not the hunter add them to the scoreboard
					plugin.objective.getScore(player.getName()).setScore(id);
					alive.add(player.getUniqueId());
					id++;
				}
				i++;
			}

			// more scoreboard stuff
			plugin.objective.setDisplayName(ChatColor.RED + "Hunter: " + ChatColor.DARK_RED + ChatColor.BOLD + hunter.getName());
			plugin.objective.getScore("Alive players").setScore(0);
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerInventory inv = player.getInventory();
				
				if (!plugin.playing.hasPlayer(player))
					plugin.playing.addPlayer(player);

				Object handle = methodGetHandle.of(player).call();
				Object connection = fieldPlayerConnection.of(handle).get();

				// if they are not the hunter
				if (player.getUniqueId() != hunter.getUniqueId()) {

					// spawn them
					Location spawn = plugin.getCurrentMap().getSafeSpawn(player);
					player.teleport(spawn);

					// effects
					player.removePotionEffect(PotionEffectType.BLINDNESS);
					player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, true, false));
					player.setWalkSpeed((float) 0.11);

					// clear inventory
					inv.clear();
					inv.setArmorContents(null);
				}
				// play music
				plugin.getCurrentMap().playMusic();

				// action bar tell them who the hunter is, this also overrides
				// the text that shows the song currently playing
				String message = ChatColor.DARK_RED + hunter.getName() + " is the Hunter!";
				Object cbc = classChatSerializer.getMethod("a", String.class).of(classChatSerializer).call("{\"text\": \"" + message + "\"}");
				Object actionPacket = ppoc.create(cbc, (byte) 2);
				methodSendPacket.of(connection).call(actionPacket);

				// more info about hunter
				String msg = "Find out more about: ";
				String click = ChatColor.AQUA + hunter.getName();
				String command = "/stats " + hunter.getName();
				String code = "{\"text\":\"" + msg + "\",\"extra\":[{\"text\":\"" + click + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + command + "\"}}]}";
				Object comp = classChatSerializer.getMethod("a", String.class).of(classChatSerializer).call(code);
				Object packet = ppoc.create(comp, (byte) 0);
				methodSendPacket.of(connection).call(packet);

				// show scoreboard to all players
				player.setScoreboard(plugin.scoreboard);
			}
			return 0;
		} else
			return 1;
	}

	public int stop(String msg) {

		if (running) {
			// get lobby location
			String string = (String) plugin.config().getString("lobby");
			World world = Bukkit.getWorld(string.split(",")[0]);
			double x = Double.parseDouble(string.split(",")[1]);
			double y = Double.parseDouble(string.split(",")[2]);
			double z = Double.parseDouble(string.split(",")[3]);
			double yaw = Double.parseDouble(string.split(",")[4]);
			double pitch = Double.parseDouble(string.split(",")[5]);
			Location loc = new Location(world, x, y, z, (int) yaw, (int) pitch);

			for (Player player : Bukkit.getOnlinePlayers()) {
				if (plugin.playing.hasPlayer(player))
					plugin.playing.removePlayer(player);

				player.removePotionEffect(PotionEffectType.BLINDNESS);
				// clear inventory
				PlayerInventory inv = player.getInventory();
				inv.clear();
				inv.setArmorContents(null);

				// tell all players who won
				RefConstructor ppoc = classPacketPlayOutChat.getConstructor(classIChatBaseComponent, byte.class);
				Object handle = methodGetHandle.of(player).call();
				Object connection = fieldPlayerConnection.of(handle).get();
				Object cbc = classChatSerializer.getMethod("a", String.class).of(classChatSerializer).call("{\"text\": \"" + msg + "\"}");
				Object actionPacket = ppoc.create(cbc, (byte) 2);
				methodSendPacket.of(connection).call(actionPacket);

				// teleport them to lobby and remove the walk speed to normal
				player.setWalkSpeed((float) 0.2);
				player.teleport(loc);
			}

			// reset scoreboard
			plugin.objective = plugin.scoreboard.getObjective("Info");
			if (plugin.objective != null)
				plugin.objective.unregister();
			plugin.objective = plugin.scoreboard.registerNewObjective("Info", "dummy");

			return 0;
		} else
			return 1;
	}

	@Override
	public void run() {
		running = true;
		if (counter <= 0) {
			if (running) {
				this.stop(ChatColor.DARK_RED + "Players Win!");
				this.cancel();
				running = false;
			}
		}

		// change the time left on the scoreboard
		plugin.objective.getScore("Time left").setScore(counter);
		// decrement
		counter--;
	}

}