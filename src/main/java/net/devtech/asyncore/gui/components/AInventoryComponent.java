package net.devtech.asyncore.gui.components;

import net.devtech.asyncore.gui.graphics.InventoryGraphics;
import net.devtech.asyncore.util.Size2i;
import net.devtech.asyncore.util.inv.InvWrapper;
import org.bukkit.inventory.ItemStack;
import java.awt.Point;

/**
 * a component that is backed by an inventory, the component requires synchronization
 */
public class AInventoryComponent implements AComponent {
	private final InvWrapper sync;
	private final Size2i dimensions;

	public AInventoryComponent(InvWrapper sync, Size2i dimensions) {
		this.sync = sync;
		this.dimensions = dimensions;
	}

	@Override
	public void draw(InventoryGraphics inventory) {
		for (int i = 0; i < this.sync.size(); i++) {
			inventory.setItem(this.sync.getStack(i), i % this.dimensions.getWidth(), i / this.dimensions.getWidth());
		}
	}

	@Override
	public void resync(InventoryGraphics graphics) {
		for (int i = 0; i < this.sync.size(); i++) {
			// resync inventory on redraws
			this.sync.setStack(graphics.getItem(i % this.dimensions.getWidth(), i / this.dimensions.getWidth()), i);
		}
	}

	@Override
	public boolean attemptAdd(Point point, ItemStack add) {
		return false;
	}

	@Override
	public boolean attemptTake(Point point, ItemStack stack) {
		return false;
	}

	@Override
	public boolean attemptSwap(Point point, ItemStack add, ItemStack take) {
		this.attemptTake(point, take);
		this.attemptAdd(point, take);
		return false;
	}

	@Override
	public Size2i getSize() {
		return this.dimensions;
	}

}
