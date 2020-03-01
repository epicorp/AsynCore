package net.devtech.asyncore.util.inv;

import org.bukkit.inventory.ItemStack;

public class SlotInvWrapper implements InvWrapper {
	private final int index;
	private final InvWrapper parent;

	public SlotInvWrapper(int index, InvWrapper parent) {
		this.index = index;
		this.parent = parent;
	}

	@Override
	public ItemStack getStack(int index) {
		assert index == 0;
		return this.parent.getStack(this.index);
	}

	@Override
	public void setStack(ItemStack stack, int index) {
		assert index == 0;
		this.parent.setStack(stack, this.index);
	}

	@Override
	public int size() {
		return 1;
	}
}
