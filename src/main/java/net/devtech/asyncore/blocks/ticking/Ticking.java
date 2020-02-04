package net.devtech.asyncore.blocks.ticking;

/**
 * a block that ticks
 */
public interface Ticking {
	/**
	 * Tick the block, this is called on regular intervals
	 * @param x the x coordinate of the object
	 * @param y the y coordinate of the object
	 * @param z the z coordinate of the object
	 */
	void tick(int x, int y, int z);
}
