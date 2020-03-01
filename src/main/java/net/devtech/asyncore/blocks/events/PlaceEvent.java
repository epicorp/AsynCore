package net.devtech.asyncore.blocks.events;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * called when a block is first placed in the world
 */
public class PlaceEvent extends BlockEvent {
	public PlaceEvent(int x, int y, int z, World world) {
		super(x, y, z, world);
	}

	public PlaceEvent(Location location) {
		super(location);
	}
}
