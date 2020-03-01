package net.devtech.asyncore.blocks.containers;

import net.devtech.asyncore.util.inv.InvWrapper;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

/**
 * a block that has an inventory at a face, may return the same inventory for multiple faces
 */
public interface InventoryContainer {
	/**
	 * returns the inventory at the given face, the face may be null
	 * and in that case the inventory may return a default inventory
	 * or null. The method may return null if there is no inventory for that face either
	 * @param face the face being accessed
	 * @return the inventory
	 */
	@Nullable InvWrapper getInventory(@Nullable BlockFace face);
}
