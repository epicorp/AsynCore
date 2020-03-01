package net.devtech.asyncore.crafting.anvil;

import org.bukkit.inventory.ItemStack;

public class AnvilResult {
	private final ItemStack output;
	private final int xp;
	private boolean rename;

	/**
	 * This is the result of an anvil recipe
	 * @param output the output item
	 * @param xp the cost to make it
	 */
	public AnvilResult(ItemStack output, int xp, boolean rename) {
		this.output = output;
		this.xp = xp;
		this.rename = rename;
	}

	public AnvilResult(ItemStack output, int xp) {
		this(output, xp, true);
	}

	public ItemStack getOutput() {
		return this.output;
	}

	public int getXp() {
		return this.xp;
	}

	public boolean isRename() {
		return this.rename;
	}
}
