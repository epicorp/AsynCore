package net.devtech.asyncore.items.blocks;

import net.devtech.asyncore.blocks.events.DestroyEvent;
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

public abstract class BlockItem implements CanInteractWith, CustomItem {
	protected final ServerAccess<Object> access;
	protected final PersistentRegistry registry;
	protected BlockItem(PersistentRegistry registry, ServerAccess<Object> access) {
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
		event.setDropItems(false); // replace with our own
		this.access.invoke(event.getBlock().getLocation(), new DestroyEvent(event.getBlock().getLocation()));
	}

	@LocalEvent
	private void destroy(DestroyEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();
		if(this.shouldDropItem()) {
			block.getWorld().dropItemNaturally(location, CustomItemFactory.wrap(this.registry, this));
		}
		// remove ourselves from the world
		this.access.remove(event.getLocation());
	}

	// item events

	@Override
	public void interact(PlayerInteractEvent event) {
		Action action = event.getAction();
		if(action == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock().getRelative(event.getBlockFace());
			if(!block.getType().isSolid()) {
				if(this.access.addIfVacant(block.getLocation(), () -> this)) {
					event.getItem().setAmount(event.getItem().getAmount()-1);
				}
			}
		}
	}
}
