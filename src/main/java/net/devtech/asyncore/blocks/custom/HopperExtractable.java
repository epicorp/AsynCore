package net.devtech.asyncore.blocks.custom;

import net.devtech.asyncore.blocks.containers.InventoryContainer;
import net.devtech.asyncore.blocks.events.TickEvent;
import net.devtech.asyncore.blocks.world.events.LocalEvent;
import net.devtech.asyncore.util.inv.InvWrapper;
import net.devtech.asyncore.util.inv.Inventories;
import net.devtech.asyncore.util.inv.InventoryInvWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;

/**
 * a block that can be drained of/filled with items via a hopper
 */
public interface HopperExtractable extends InventoryContainer {
	@LocalEvent(value = 10) // after gui ticks and stuff
	default void extract(TickEvent event) {
		InvWrapper bottom = this.getInventory(BlockFace.DOWN);
		if(bottom != null) {
			Block floorHopper = event.getLocation().add(0, -1, 0).getBlock();
			if (floorHopper.getType() == Material.HOPPER) {
				Hopper hopper = (Hopper) floorHopper.getState();
				Inventory hopperInventory = hopper.getInventory();
				if(Inventories.mergeOne(bottom, new InventoryInvWrapper(hopperInventory))) this.onChange(); // attempt insert
			}
		}
		Location location = event.getLocation();
		this.takeFrom(location, BlockFace.UP);
		this.takeFrom(location, BlockFace.NORTH);
		this.takeFrom(location, BlockFace.EAST);
		this.takeFrom(location, BlockFace.SOUTH);
		this.takeFrom(location, BlockFace.WEST);
	}

	default void takeFrom(Location location, BlockFace face) {
		InvWrapper side = this.getInventory(face);
		if(side != null) {
			Block at = location.clone().add(face.getModX(), face.getModY(), face.getModZ()).getBlock();
			if (at.getType() == Material.HOPPER) {
				Hopper hopper = (Hopper) at.getState();
				Inventory inventory = hopper.getInventory();
				if(Inventories.mergeOne(new InventoryInvWrapper(inventory), side)) this.onChange();
			}
		}
	}

	/**
	 * called when the inventory is changed, redrawing and stuff should happen here
	 */
	default void onChange() {}
}
