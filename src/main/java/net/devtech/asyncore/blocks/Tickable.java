package net.devtech.asyncore.blocks;

import org.bukkit.World;

/**
 * a block that ticks
 */
public interface Tickable {
	/**
	 * Tick the block, this is called on regular intervals
	 * @param world the world the object is in
	 * @param x the x coordinate of the object
	 * @param y the y coordinate of the object
	 * @param z the z coordinate of the object
	 */
	void tick(World world, int x, int y, int z);
}
