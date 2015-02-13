package me.oceanopsis;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import me.oceanopsis.commands.Game;
import me.oceanopsis.commands.Stats;
import me.oceanopsis.events.PlayerDeathListener;
import me.oceanopsis.events.PlayerJoinListener;
import me.oceanopsis.events.SoundListener;
import me.oceanopsis.util.ArtRenderer;
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

	public Horror plugin;

	private String currentMap = "";

	private HashMap<UUID, Yaml> playerYaml = new HashMap<UUID, Yaml>();

	private HashMap<String, Yaml> maps = new HashMap<String, Yaml>();

	private Yaml config;

	public Scoreboard scoreboard;
	public ScoreboardManager manager;
	public Objective objective;
	public Team playing;
	public Team blue;
	
	public ProtocolManager protocol;

	@Override
	public void onEnable() {
		this.plugin = this;
		
		protocol = ProtocolLibrary.getProtocolManager();

		this.getLogger().info("Loading files into memory...");

		// load configs
		loadConfigs();

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

		// register events
		registerEvents();

		// initiate PlayerRecord
		initializePlayerRecord();

		// set command executers
		setCommandExecuters();
		
		// sound handler
		protocol.addPacketListener(new SoundListener(this, ListenerPriority.NORMAL, Arrays.asList(PacketType.Play.Server.NAMED_SOUND_EFFECT)));

		this.getLogger().info("Files loaded");
		
		this.getLogger().info("Starting scheduler");
		new Timer(plugin).runTaskTimer(this, 20L, 20L);
		this.getLogger().info("Scheduler started");
	}

	@Override
	public void onDisable() {
		unloadConfigs();
	}

	public String getNmsVersion() {
		String NMS = null;
		try {
			NMS = Bukkit.getServer().getClass().getPackage().getName().split(".")[3];
		} catch (ArrayIndexOutOfBoundsException ex) {
			NMS = "pre";
		}
		return NMS;
	}

	public GameControl getGame() {
		return new GameControl(this);
	}

	public Map getMap(String string) {
		if (this.maps.containsKey(string)) {
			return new Map(maps.get(string));
		} else {
			Yaml yaml = this.getMapIOFile(string);
			this.maps.put(string, yaml);
			return new Map(yaml);
		}
	}

	public Yaml config() {
		return config;
	}

	private Yaml getConfigIOFile() {
		return new Yaml(plugin.getDataFolder().getAbsolutePath(), "config");
	}

	public Map getCurrentMap() {
		return new Map(maps.get(currentMap));
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

	public void loadConfigs() {
		
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
			this.maps.put(fileName.replace(".yml", ""), map);
			currentMap = fileName.replace(".yml", "");
		}
	}

	public void unloadConfigs() {

		for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
			Yaml yaml = this.getPlayerYaml(op.getUniqueId());
			yaml.save();
		}
	}

	private void initializePlayerRecord() {
		this.getLogger().info("Initiating PlayerRecord and uploading playerbase to it");
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
		this.getLogger().info("Successfully uploaded playerbase to PlayerRecord");
	}

	private void registerEvents() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new PlayerJoinListener(this), this);
		pm.registerEvents(new PlayerDeathListener(this), this);
	}

	private void setCommandExecuters() {
		getCommand("game").setExecutor(new Game(this));
		getCommand("stats").setExecutor(new Stats(this));
	}

}
