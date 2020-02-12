package net.devtech.asyncore.items;

import org.bukkit.event.block.BlockPlaceEvent;

/**
 * an item that can be placed in the world
 */
public interface CanPlace {
	void place(BlockPlaceEvent event);
}
