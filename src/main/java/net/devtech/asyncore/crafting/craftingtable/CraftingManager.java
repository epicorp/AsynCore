package net.devtech.asyncore.crafting.craftingtable;

import net.devtech.asyncore.crafting.AbstractRecipeManager;
import net.devtech.asyncore.gui.graphics.GuiManager;
import net.devtech.asyncore.util.inv.Inventories;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.UUID;

public class CraftingManager extends AbstractRecipeManager<CraftingRecipe> {
	@Nullable
	private final GuiManager manager;

	public CraftingManager(@Nullable GuiManager manager) {this.manager = manager;}

	@EventHandler(priority = EventPriority.MONITOR)
	public void craftEvent(InventoryClickEvent event) { // here, we are setting the item in the user's hand
		Inventory inv = event.getInventory();
		if (inv instanceof CraftingInventory && (this.manager == null || this.manager.isInCustomGui(event.getWhoClicked()))) { // if the user clicked the output slot of the inventory
			if (event.getSlot() == 0) {
				boolean craftAll = false;
				if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) craftAll = true;
				final boolean craft = craftAll;
				ItemStack[] matrix = Inventories.clone(((CraftingInventory) inv).getMatrix());
				Inventories.clean(matrix);
				this.recipes.stream().map(c -> c.output(matrix, craft)).filter(Objects::nonNull).findFirst().ifPresent(i -> {
					if (craft && !Inventories.canAddStack(i, event.getView().getBottomInventory())) // if shiftclick and inventory is full
						return;
					UUID uuid = event.getView().getPlayer().getUniqueId();
					this.restricted.add(uuid);
					event.setCurrentItem(i);
					Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
						try {
							Inventories.clean(matrix);
							((CraftingInventory) inv).setMatrix(matrix);
							this.display((CraftingInventory) inv);
							((Player) event.getWhoClicked()).updateInventory();
						} finally {
							this.restricted.remove(uuid);
						}
					}, 0);
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void prep(PrepareItemCraftEvent event) { // only thing that happens here, is that we are DISPLAYING the result item
		if (!this.restricted.contains(event.getView().getPlayer().getUniqueId())) this.display(event.getInventory());
	}

	private void display(CraftingInventory inventory) {
		ItemStack[] matrix = Inventories.clone(inventory.getMatrix());
		Inventories.clean(matrix);
		this.recipes.stream().map(c -> c.output(matrix, false)).filter(Objects::nonNull).findFirst().ifPresent(inventory::setResult);
	}
}
