package net.devtech.asyncore.util.inv;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryInvWrapper implements InvWrapper {
	private final Inventory inventory;

	public InventoryInvWrapper(Inventory inventory) {this.inventory = inventory;}

	@Override
	public ItemStack getStack(int index) {
		return this.inventory.getItem(index);
	}

	@Override
	public void setStack(ItemStack stack, int index) {
		this.inventory.setItem(index, stack);
	}

	@Override
	public int size() {
		return this.inventory.getSize();
	}
}
