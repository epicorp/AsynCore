package net.devtech.asyncore.blocks.events;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * called when a block is destroyed
 */
public class DestroyEvent extends BlockEvent {
	public DestroyEvent(int x, int y, int z, World world) {
		super(x, y, z, world);
	}

	public DestroyEvent(Location location) {
		super(location);
	}
}
