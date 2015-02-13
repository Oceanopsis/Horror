package me.oceanopsis.events;

import me.oceanopsis.Horror;

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
			if (event.getPacket().getStrings().read(0).startsWith("step"))
				event.getPacket().getStrings().write(0, "mob.cow.step");
		}
	}

}
