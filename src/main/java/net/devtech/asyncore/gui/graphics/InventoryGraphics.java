package net.devtech.asyncore.gui.graphics;

import org.bukkit.inventory.ItemStack;

public interface InventoryGraphics {
	void setItem(ItemStack stack, int x, int y);
	ItemStack getItem(int x, int y);
}
