package net.devtech.asyncore.util.inv;

import org.bukkit.inventory.ItemStack;

/**
 * an wrapper for inventories, like item stack arrays, lists, or bukkit inventories
 * to reduce code duplication
 */
public interface InvWrapper {
	ItemStack getStack(int index);
	void setStack(ItemStack stack, int index);
	int size();

	default InvWrapper copy() {
		ItemStack[] array = new ItemStack[this.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = this.getStack(i).clone();
		}
		return new ArrayInvWrapper(array);
	}
}
