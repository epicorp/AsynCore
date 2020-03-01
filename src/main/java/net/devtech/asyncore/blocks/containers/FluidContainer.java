package net.devtech.asyncore.blocks.containers;

import net.devtech.asyncore.blocks.containers.fluid.FluidInventory;
import org.bukkit.block.BlockFace;

/**
 * a block that stores fluid
 */
public interface FluidContainer {
	/**
	 * get the inventory
	 */
	FluidInventory getFluidInventory(BlockFace face);
}
