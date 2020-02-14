package net.devtech.asyncore.items.blocks;

import net.devtech.asyncore.blocks.CustomBlock;
import net.devtech.asyncore.blocks.world.events.LocalEvent;
import net.devtech.asyncore.items.CanInteractWith;
import net.devtech.asyncore.items.CanPlace;
import net.devtech.asyncore.items.CustomItem;
import net.devtech.asyncore.items.CustomItemFactory;
import net.devtech.asyncore.world.server.ServerAccess;
import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class BlockItem implements CanPlace, CanInteractWith, CustomItem, CustomBlock {
	protected final ServerAccess<CustomBlock> access;
	protected final PersistentRegistry registry;
	protected BlockItem(PersistentRegistry registry, ServerAccess<CustomBlock> access) {
		this.access = access;
		this.registry = registry;
	}

	/**
	 * override if the item should drop it's item when a block where the block exists breaks
	 */
	protected boolean shouldDropItem() {
		return true;
	}

	// block events

	// these have priorities so you can cancel them early
	@LocalEvent
	private void _break(BlockBreakEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();
		if(this.shouldDropItem() && !event.isCancelled()) {
			event.setDropItems(false);
			block.getWorld().dropItemNaturally(location, CustomItemFactory.wrap(this.registry, this));
		}
		this.access.remove(location);
	}

	// item events
	@Override
	public void place(BlockPlaceEvent event) {
		if(!this.access.setIfVacant(event.getBlock().getLocation(), () -> this)) {
			event.setCancelled(true);
		}
	}

	@Override
	public void interact(PlayerInteractEvent event) {
		Action action = event.getAction();
		if(action == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock().getRelative(event.getBlockFace());
			if(!block.getType().isSolid()) {
				if(this.access.setIfVacant(block.getLocation(), () -> this)) {
					event.getItem().setAmount(event.getItem().getAmount()-1);
				}
			}
		}
	}
}
