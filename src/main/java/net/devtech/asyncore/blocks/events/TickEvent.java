package net.devtech.asyncore.blocks.events;

import org.bukkit.World;

/**
 * this event is invoked every N ticks on every block by the
 * {@link net.devtech.asyncore.blocks.world.CustomBlockServer}
 */
public class TickEvent extends BlockEvent {
	public TickEvent(int x, int y, int z, World world) {
		super(x, y, z, world);
	}
}
