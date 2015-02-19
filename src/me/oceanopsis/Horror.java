package me.oceanopsis;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import me.oceanopsis.commands.Game;
import me.oceanopsis.commands.Stats;
import me.oceanopsis.events.PlayerDeathListener;
import me.oceanopsis.events.PlayerJoinListener;
import me.oceanopsis.events.PlayerMoveListener;
import me.oceanopsis.events.SoundListener;
import me.oceanopsis.util.PlayerRecord;
import me.oceanopsis.util.Yaml;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;

public class Horror extends JavaPlugin {

	//The plugin
	public Horror plugin;

	//Current map string
	private String currentMap = "";

	//Files
	private HashMap<UUID, Yaml> playerYaml = new HashMap<UUID, Yaml>();
	private HashMap<String, Map> maps = new HashMap<String, Map>();
	private Yaml config;

	//Scoreboard stuff
	public Scoreboard scoreboard;
	public ScoreboardManager manager;
	public Objective objective;
	public Team playing;
	public Team blue;
	
	//protocol manager for SoundListener.java
	public ProtocolManager protocol;
	
	//The game, alows start and stop
	public GameControl game;

	@Override
	public void onEnable() {
		this.plugin = this;
		
		//load config
		Yaml config = getConfigIOFile();
		config.load();
		this.config = config;

		// enter all players, offline and online, into memory
		for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
			Yaml yaml = this.getPlayerIOFile(op.getUniqueId());
			yaml.load();
			this.playerYaml.put(op.getUniqueId(), yaml);
		}

		// load maps into memory
		File mapfile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "maps");
		for (String fileName : mapfile.list()) {
			Yaml map = getMapIOFile(fileName.replace(".yml", ""));
			map.load();
			this.maps.put(fileName.replace(".yml", ""), new Map(map));
			currentMap = fileName.replace(".yml", "");
		}

		//set scoreboard variables
		manager = Bukkit.getScoreboardManager();
		scoreboard = manager.getMainScoreboard();
		
		//register the blue team
		blue = scoreboard.getTeam("Blue");
		if (blue != null)
			blue.unregister();
		blue = scoreboard.registerNewTeam("Blue");
		blue.setPrefix(ChatColor.BLUE + "" + ChatColor.BOLD + "");
		
		//register the main team
		playing = scoreboard.getTeam("Playing");
		if (playing != null)
			playing.unregister();
		playing = scoreboard.registerNewTeam("Playing");
		
		//remove name tags
		playing.setNameTagVisibility(NameTagVisibility.NEVER);
		
		//reset scoreboard
		objective = scoreboard.getObjective("Info");
		if (objective != null)
			objective.unregister();
		objective = scoreboard.registerNewObjective("Info", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		//set the game
		this.game = new GameControl(this);
		
		//set the protocol manager
		protocol = ProtocolLibrary.getProtocolManager();
		
		//register events
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new PlayerJoinListener(this), this);
		pm.registerEvents(new PlayerDeathListener(this), this);
		pm.registerEvents(new PlayerMoveListener(this), this);
		// sound handler
		protocol.addPacketListener(new SoundListener(this, ListenerPriority.NORMAL, Arrays.asList(PacketType.Play.Server.NAMED_SOUND_EFFECT)));

		//Start the PlayerRecord
		new PlayerRecord("Oceanopsis", 0.00);
		this.getLogger().info("PlayerRecord initiated");
		for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
			Yaml yaml = this.getPlayerYaml(op.getUniqueId());
			this.playerYaml.put(op.getUniqueId(), yaml);
			PlayerRecord killerrecord = PlayerRecord.getRecord(op.getName());
			if (PlayerRecord.rankingList.contains(killerrecord)) {
				PlayerRecord.rankingList.remove(killerrecord);
			}
			double kills = yaml.getDouble("kills");
			double deaths = yaml.getDouble("deaths");
			if (kills > 0 && deaths > 0) {
				new PlayerRecord(op.getName(), kills / deaths);
				PlayerRecord newkillerrecord = PlayerRecord.getRecord(op.getName());
				newkillerrecord.toString();
			}
		}
		
		//Set the command executors
		getCommand("game").setExecutor(new Game(this));
		getCommand("stats").setExecutor(new Stats(this));
		
		//start the plugin scheduler
		new Timer(plugin).runTaskTimer(this, 20L, 20L);
	}

	@Override
	public void onDisable() {
		for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
			Yaml yaml = this.getPlayerYaml(op.getUniqueId());
			yaml.save();
		}
	}

	public GameControl getGame() {
		return game;
	}

	public Map getMap(String string) {
		if (this.maps.containsKey(string)) {
			return maps.get(string);
		} else {
			Yaml yaml = this.getMapIOFile(string);
			Map map = new Map(yaml);
			this.maps.put(string, map);
			return map;
		}
	}

	public Yaml config() {
		return config;
	}

	private Yaml getConfigIOFile() {
		return new Yaml(plugin.getDataFolder().getAbsolutePath(), "config");
	}

	public Map getCurrentMap() {
		return maps.get(currentMap);
	}

	private Yaml getMapIOFile(String string) {
		return new Yaml(plugin.getDataFolder().getAbsolutePath() + File.separator + "maps", string);
	}

	public Yaml getPlayerYaml(UUID uuid) {
		if (this.playerYaml.containsKey(uuid)) {
			return this.playerYaml.get(uuid);
		} else {
			Yaml yaml = this.getPlayerIOFile(uuid);
			this.playerYaml.put(uuid, yaml);
			return yaml;
		}
	}

	public Yaml getPlayerYaml(Player player) {
		if (this.playerYaml.containsKey(player.getUniqueId())) {
			return this.playerYaml.get(player.getUniqueId());
		} else {
			Yaml yaml = this.getPlayerIOFile(player.getUniqueId());
			this.playerYaml.put(player.getUniqueId(), yaml);
			return yaml;
		}
	}

	private Yaml getPlayerIOFile(UUID uuid) {
		return new Yaml(plugin.getDataFolder().getAbsolutePath() + File.separator + "players", uuid.toString());
	}

}
