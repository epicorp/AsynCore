package net.devtech.asyncore.items;

import org.bukkit.inventory.ItemStack;

public interface CustomItem {
	/**
	 * do <b>NOT</b> call this, use a {@link CustomItemFactory} instead!
	 * @return a base item
	 */
	ItemStack createBaseStack();

	/**
	 * this method is called just after the item has been saved to the stack,
	 * here you can update the lore or name of the itemstack
	 * @param stack the stack
	 */
	default void transform(ItemStack stack) {}
}
