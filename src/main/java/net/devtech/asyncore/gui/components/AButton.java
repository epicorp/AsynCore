package net.devtech.asyncore.gui.components;

import net.devtech.asyncore.gui.graphics.InventoryGraphics;
import net.devtech.asyncore.util.Size2i;
import org.bukkit.inventory.ItemStack;
import java.awt.Point;

public class AButton implements AComponent {
	private final ItemStack icon;
	private final Runnable onClick;
	public AButton(ItemStack icon, Runnable click) {
		this.icon = icon;
		this.onClick = click;
	}

	@Override
	public void draw(InventoryGraphics inventory) {
		inventory.setItem(this.icon, 0, 0);
	}

	@Override
	public boolean attemptAdd(Point point, ItemStack add) {
		this.onClick.run();
		return true;
	}

	@Override
	public boolean attemptTake(Point point, ItemStack stack) {
		this.onClick.run();
		return true;
	}

	@Override
	public boolean attemptSwap(Point point, ItemStack add, ItemStack take) {
		this.onClick.run();
		return true;
	}

	@Override
	public Size2i getSize() {
		return new Size2i(1, 1);
	}
}
