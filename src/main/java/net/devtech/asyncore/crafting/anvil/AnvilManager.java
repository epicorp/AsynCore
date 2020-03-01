package net.devtech.asyncore.crafting.anvil;

import net.devtech.asyncore.crafting.AbstractRecipeManager;
import net.devtech.asyncore.gui.graphics.GuiManager;
import net.devtech.asyncore.util.inv.Inventories;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

public class AnvilManager extends AbstractRecipeManager<AnvilRecipe> {
	private final GuiManager manager;

	public AnvilManager(@Nullable GuiManager manager) {
		this.manager = manager;
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void offer(PrepareAnvilEvent event) {
		AnvilInventory inventory = event.getInventory();
		Player player = (Player) event.getView().getPlayer();
		ItemStack result = this.result(player,  inventory.getContents(), inventory, event.getResult());
		if(result != null) {
			event.setResult(result);
			this.restricted.add(player.getUniqueId());
		}
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void craft(InventoryClickEvent event) {
		Player player = (Player) event.getView().getPlayer();
		Inventory inv = event.getInventory();
		int slot = event.getSlot();
		if(inv instanceof AnvilInventory && this.restricted.contains(player.getUniqueId()) && slot == 2 && (this.manager == null || this.manager.isInCustomGui(event.getWhoClicked()))) {
			ItemStack[] contents = inv.getContents();
			ItemStack result = this.result(player, contents, (AnvilInventory) inv, null);
			inv.setContents(contents);
			if(!Inventories.empty(result))
				event.getView().setCursor(result);
		}
	}

	private ItemStack result(Player player, ItemStack[] grid, AnvilInventory inventory, ItemStack result) {
		Stream<AnvilRecipe> recipe = this.recipes.stream();
		if (!Inventories.empty(result)) recipe = recipe.filter(AnvilRecipe::override);

		ItemStack[] astack = new ItemStack[1];

		recipe.map(r -> r.offer(player, grid)).filter(Objects::nonNull).findFirst().ifPresent(a -> {
			ItemStack stack = a.getOutput();
			if (a.isRename()) {
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName(inventory.getRenameText());
				stack.setItemMeta(meta);
			}

			astack[0] = stack;
			inventory.setRepairCost(a.getXp());
		});

		return astack[0];
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void close(InventoryCloseEvent event) {
		this.restricted.remove(event.getPlayer().getUniqueId());
	}
}
