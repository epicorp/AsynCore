package net.devtech.asyncore.gui.graphics;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.devtech.asyncore.gui.components.AComponent;
import net.devtech.asyncore.util.inv.Inventories;
import net.devtech.asyncore.util.ref.EntityRef;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.awt.Point;
import java.util.logging.Logger;

public class GuiManager implements Listener {
	private static final Logger LOGGER = Logger.getLogger(GuiManager.class.getSimpleName());
	public final BiMap<EntityRef, AComponent> components = HashBiMap.create();

	/**
	 * redraw all the viewing players of an inventory
	 * @param inventory the inventory
	 */
	public void redraw(Inventory inventory) {
		this.draw(inventory, false);
	}

	public void resync(Inventory inventory) {
		this.draw(inventory, true);
	}

	public boolean isInCustomGui(HumanEntity player) {
		return this.components.containsKey(new EntityRef(player));
	}

	private void draw(Inventory inventory, boolean resync) {
		for (HumanEntity viewer : inventory.getViewers()) {
			EntityRef ref = new EntityRef(viewer);
			AComponent component = this.components.get(ref);
			if (component != null) {
				// redraw component
				if (resync) component.resync(new MainGraphics(inventory));
				else component.draw(new MainGraphics(inventory));
			}
		}
	}

	private boolean oGui; // hack, but it just works:tm:

	public void openGui(HumanEntity entity, AComponent component, Inventory inventory) {
		this.components.forcePut(new EntityRef(entity), component);
		this.oGui = true;
		entity.openInventory(inventory);
		component.draw(new MainGraphics(inventory));
	}

	public void closeGui(HumanEntity entity) {
		if (this.oGui) {
			this.oGui = false;
			return;
		}
		EntityRef ref = new EntityRef(entity);
		if (this.components.remove(ref) != null) {
			entity.closeInventory();
		}
	}


	@EventHandler
	public void close(InventoryCloseEvent event) {
		HumanEntity player = event.getPlayer();
		this.closeGui(player);
	}

	// highest priority
	// otherwise dupe exploits may happen, so keep it this way
	@EventHandler
	public void touch(InventoryClickEvent event) {
		final HumanEntity clicked = event.getWhoClicked();
		final AComponent component = this.components.get(new EntityRef(clicked));
		if (component != null) { // ohyes
			final InventoryAction action = event.getAction();
			final Inventory inventory = event.getInventory(); // top inventory
			final InventoryType type = inventory.getType();
			boolean guiClick = event.getRawSlot() < inventory.getSize();
			if (action == InventoryAction.COLLECT_TO_CURSOR) {
				event.setCancelled(true);
				clicked.sendMessage(ChatColor.YELLOW + "Collect to cursor not yet supported by AsynCore GUIs");
			} else if (guiClick) { // inventory -> player logic
				ItemStack hand = event.getCursor();
				hand = hand == null ? null : hand.clone();
				ItemStack current = event.getCurrentItem();
				current = current == null ? null : current.clone();
				final int slot = event.getSlot();
				final int width = Inventories.getWidth(type);
				final Point loc = new Point(slot % width, slot / width);
				boolean cancelled = false;
				switch (action) {
					case PLACE_ALL:
						cancelled = component.attemptAdd(loc, hand);
						break;
					case PLACE_ONE:
						hand.setAmount(1);
						cancelled = component.attemptAdd(loc, hand);
						break;
					case HOTBAR_MOVE_AND_READD:
					case DROP_ALL_SLOT:
					case MOVE_TO_OTHER_INVENTORY:
					case PICKUP_ALL:
						cancelled = component.attemptTake(loc, current);
						break;
					case DROP_ONE_SLOT:
					case PICKUP_ONE:
						current.setAmount(1);
						cancelled = component.attemptAdd(loc, current);
						break;
					case PLACE_SOME:
						int placed = hand.getAmount() - Inventories.getLeftover(hand, new ItemStack[]{current});
						hand.setAmount(placed);
						cancelled = component.attemptAdd(loc, hand);
						break;
					case HOTBAR_SWAP:
						int hotbar = event.getHotbarButton();
						cancelled = component.attemptSwap(loc, clicked.getInventory().getItem(hotbar), current);
						break;
					case PICKUP_HALF:
						current.setAmount(current.getAmount() / 2);
						cancelled = component.attemptTake(loc, current);
						break;
					case PICKUP_SOME: // how is this even possible
						int picked = current.getAmount() - Inventories.getLeftover(current, new ItemStack[]{hand});
						current.setAmount(picked);
						cancelled = component.attemptTake(loc, current);
						break;
					case SWAP_WITH_CURSOR:
						cancelled = component.attemptSwap(loc, hand, current);
						break;
					case UNKNOWN:
						cancelled = true;
						break;
					default: // clone, nothing, unkown, drop cursor, drop 1 cursor
						break;
				}

				event.setCancelled(cancelled);
			} else if (event.isShiftClick()) { // player -> inventory shift click logic
				event.setCancelled(true);
				clicked.sendMessage(ChatColor.RED + "Shift clicking is not supported by AsynCore GUIs");
			}

		}
	}

	@EventHandler
	public void leave(PlayerQuitEvent event) { // just in case
		this.closeGui(event.getPlayer());
	}

	@EventHandler
	public void join(PlayerJoinEvent event) {
		this.closeGui(event.getPlayer()); // for good measure
	}
}
