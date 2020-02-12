package net.devtech.asyncore.blocks;

import org.bukkit.World;

/**
 * a custom block
 * please, for the love of god,
 * do <b>NOT</b> store the location/world unless you actually have to, it is <b>NOT</b>
 * guaranteed to be the same, as the block may move from pistons or other things
 */
public interface CustomBlock {
	/**
	 * called when the block is placed in the world
	 */
	void place(World world, int x, int y, int z);

	/**
	 * called when the block is destroyed in the world
	 */
	void destroy(World world, int x, int y, int z);
}
