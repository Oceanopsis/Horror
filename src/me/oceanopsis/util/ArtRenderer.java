package me.oceanopsis.util;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

public class ArtRenderer implements Listener {

    private Plugin plugin;

    public ArtRenderer(Plugin plugin) {
	this.plugin = plugin;
	plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void reloadAll() {
	for (Player p : plugin.getServer().getOnlinePlayers()) {
	    reloadPlayer(p);
	}
    }

    public Image getImageFromURL(String url) {
	try {
	    return ImageIO.read(new URL(url));
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    public void reloadPlayer(Player p) {
	for (ItemFrame frame : this.frames) {
	    @SuppressWarnings("deprecation")
	    MapView map = this.plugin.getServer().getMap(frame.getItem().getDurability());
	    p.sendMap(map);
	}
    }

    public void makeArt(Location location, final Image image) {
	MapView map = this.plugin.getServer().createMap(location.getWorld());
	for (MapRenderer render : map.getRenderers()) {
	    map.removeRenderer(render);
	}
	map.addRenderer(new CustomMapRenderer(image, this.plugin));
	@SuppressWarnings("deprecation")
	ItemStack stack = new ItemStack(Material.MAP, 1, map.getId());
	ItemFrame frame = getFrame(location);
	frame.setItem(stack);
	frames.add(frame);
    }

    private List<ItemFrame> frames = new ArrayList<ItemFrame>();

    private ItemFrame getFrame(Location loc) {
	for (Entity e : loc.getChunk().getEntities()) {
	    if (e instanceof ItemFrame) {
		if (e.getLocation().getBlock().getLocation().distance(loc) == 0D) {
		    return (ItemFrame) e;
		}
	    }
	}
	return null;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
	final Player p = event.getPlayer();
	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    public void run() {
		for (ItemFrame frame : ArtRenderer.this.frames) {
		    MapView map = Bukkit.getServer().getMap(frame.getItem().getDurability());
		    p.sendMap(map);
		}
	    }
	}, 30L);
    }

    @EventHandler
    private void onBreak(HangingBreakEvent event) {
	if (frames.contains(event.getEntity())) {
	    event.setCancelled(true);
	}
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
	Player p = event.getPlayer();
	for (CustomMapRenderer rendered : renderers) {
	    if (rendered.rendered.contains(p.getName())) {
		rendered.rendered.remove(p.getName());
	    }
	}
    }

    private List<CustomMapRenderer> renderers = new ArrayList<CustomMapRenderer>();

    private class CustomMapRenderer extends MapRenderer {

	private Image image;

	private List<String> rendered = new ArrayList<String>();

	public CustomMapRenderer(Image image, Plugin plugin) {
	    renderers.add(this);
	    this.image = image;
	}

	@Override
	public void render(MapView view, final MapCanvas canvas, Player p) {

	    if (rendered.contains(p.getName())) {
		return;
	    }

	    rendered.add(p.getName());

	    new Thread(new Runnable() {

		@Override
		public void run() {
		    canvas.drawImage(0, 0, image);
		}
	    }).start();
	}
    }
}