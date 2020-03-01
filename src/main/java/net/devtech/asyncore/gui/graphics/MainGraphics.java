package net.devtech.asyncore.gui.graphics;

import net.devtech.asyncore.util.inv.Inventories;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * only compatible with 9x9 graphics
 */
public class MainGraphics implements InventoryGraphics {
	private final Inventory inventory;
	private final int offsetX, offsetY, modulus;

	public MainGraphics(Inventory inventory, int offsetX, int offsetY) {
		this.inventory = inventory;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.modulus = Inventories.getWidth(inventory.getType());
	}


	public MainGraphics(Inventory inventory) {
		this(inventory, 0, 0);
	}

	@Override
	public void setItem(ItemStack stack, int x, int y) {
		this.inventory.setItem((y + this.offsetY) * this.modulus + (x + this.offsetX), stack);
	}

	@Override
	public ItemStack getItem(int x, int y) {
		return this.inventory.getItem((y + this.offsetY) * this.modulus + (x + this.offsetX));
	}

	public Inventory getInventory() {
		return this.inventory;
	}
}
