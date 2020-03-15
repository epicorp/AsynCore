package net.devtech.asyncore.blocks.events;

import org.bukkit.Location;
import org.bukkit.World;

public class ExplodeEvent extends BlockEvent {
	public ExplodeEvent(Location location) {
		super(location);
	}

	public ExplodeEvent(int x, int y, int z, World world) {
		super(x, y, z, world);
	}
}
