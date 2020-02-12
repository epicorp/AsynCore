package net.devtech.asyncore.items;

import org.bukkit.inventory.ItemStack;

public interface CustomItem {
	/**
	 * do <b>NOT</b> call this, use a {@link CustomItemFactory} instead!
	 * @return a base item
	 */
	ItemStack createBaseStack();
}
