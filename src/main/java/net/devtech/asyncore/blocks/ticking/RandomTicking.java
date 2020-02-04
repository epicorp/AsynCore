package net.devtech.asyncore.blocks.ticking;

public interface RandomTicking {
	/**
	 * This is called randomly when the block is loaded
	 * @param x the x coordinate of the object
	 * @param y the y coordinate of the object
	 * @param z the z coordinate of the object
	 */
	void randTick(int x, int y, int z);
}
