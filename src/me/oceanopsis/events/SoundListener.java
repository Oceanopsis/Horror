package me.oceanopsis.events;

import me.oceanopsis.GameControl;
import me.oceanopsis.Horror;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class SoundListener extends PacketAdapter {

	Horror p;

	public SoundListener(Horror plugin, ListenerPriority listenerPriority, Iterable<? extends PacketType> types) {
		super(plugin, listenerPriority, types);
		this.p = plugin;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
			if (event.getPacket().getStrings().read(0).startsWith("step")) {
				Player player = event.getPlayer();
				if (GameControl.hunter != null) {
					if (player.getUniqueId() != GameControl.hunter.getUniqueId()) {
						event.getPacket().getStrings().write(0, "mob.cow.step");
						//GameControl.hunter.playSound(player.getLocation(), Sound.HORSE_GALLOP, 1, 1);
					} else
						event.setCancelled(true);
				}
			}
		}
	}

}
