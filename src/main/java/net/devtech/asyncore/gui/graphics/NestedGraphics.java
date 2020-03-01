package net.devtech.asyncore.gui.graphics;

import org.bukkit.inventory.ItemStack;

public class NestedGraphics implements InventoryGraphics {
	private final InventoryGraphics graphics;
	private final int offsetX, offsetY;

	public NestedGraphics(InventoryGraphics graphics, int offsetX, int offsetY) {
		this.graphics = graphics;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public NestedGraphics(InventoryGraphics inventory) {
		this(inventory, 0, 0);
	}

	@Override
	public void setItem(ItemStack stack, int x, int y) {
		this.graphics.setItem(stack, x + this.offsetX, y + this.offsetY);
	}

	@Override
	public ItemStack getItem(int x, int y) {
		return this.graphics.getItem(x + this.offsetX, y + this.offsetY);
	}

}
