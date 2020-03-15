package net.devtech.asyncore.items.blocks;

import net.devtech.asyncore.blocks.BlockDataAccess;
import net.devtech.asyncore.blocks.events.DestroyEvent;
import net.devtech.asyncore.blocks.events.ExplodeEvent;
import net.devtech.asyncore.blocks.world.events.LocalEvent;
import net.devtech.asyncore.items.CanInteractWith;
import net.devtech.asyncore.items.CustomItem;
import net.devtech.asyncore.items.CustomItemFactory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface BlockItem extends CanInteractWith, CustomItem, BlockDataAccess {

	/**
	 * override if the item should drop it's item when a block where the block exists breaks
	 */
	default boolean shouldDropItem() {
		return true;
	}

	// block events
	// these have priorities so you can cancel them early
	@LocalEvent
	default void _break(BlockBreakEvent event) {
		event.setDropItems(false); // replace with our own
		Location location = event.getBlock().getLocation();
		this.getAccess().invoke(location, new DestroyEvent(location));
	}

	@LocalEvent
	default void burn(BlockBurnEvent event) {
		Location location = event.getBlock().getLocation();
		this.getAccess().invoke(location, new DestroyEvent(location));
	}

	@LocalEvent
	default void fade(BlockFadeEvent event) {
		Location location = event.getNewState().getLocation();
		this.getAccess().invoke(location, new DestroyEvent(location));
	}

	@LocalEvent
	default void create(EntityChangeBlockEvent event) {
		if (event.getTo() == Material.AIR) {
			Block block = event.getBlock();
			Location location = block.getLocation();
			this.getAccess().invoke(location, new DestroyEvent(location));
			block.setType(Material.AIR); // prevent dropping normal items
			event.setCancelled(true);
		}
	}

	// custom events
	@LocalEvent
	default void onDestroy(DestroyEvent event) {
		if (!this.isInvalid()) {
			this.invalidate();
			Block block = event.getBlock();
			Location location = block.getLocation();
			if (this.shouldDropItem()) {
				block.getWorld().dropItemNaturally(location, CustomItemFactory.wrap(this.getRegistry(), this));
			}
			// remove ourselves from the world
			this.getAccess().remove(event.getLocation());
		}
	}

	@LocalEvent
	default void onExplode(ExplodeEvent event) {
		Location location = event.getLocation();
		this.getAccess().invoke(location, new DestroyEvent(location));
	}

	// item events

	@Override
	default void interact(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock().getRelative(event.getBlockFace());
			if (!block.getType().isSolid()) {
				if (this.getAccess().addIfVacant(block.getLocation(), () -> this)) {
					event.setCancelled(true);
					event.getItem().setAmount(event.getItem().getAmount() - 1);
				}
			}
		}
	}
}
