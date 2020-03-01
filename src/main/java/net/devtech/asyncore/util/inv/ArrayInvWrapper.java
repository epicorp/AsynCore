package net.devtech.asyncore.util.inv;

import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.List;

public class ArrayInvWrapper implements InvWrapper {
	private final List<ItemStack> contents;

	public ArrayInvWrapper(List<ItemStack> contents) {
		this.contents = contents;
	}

	public ArrayInvWrapper(ItemStack[] contents) {
		this.contents = Arrays.asList(contents);
	}

	@Override
	public ItemStack getStack(int index) {
		return this.contents.get(index);
	}

	@Override
	public void setStack(ItemStack stack, int index) {
		this.contents.set(index, stack);
	}

	@Override
	public int size() {
		return this.contents.size();
	}
}
