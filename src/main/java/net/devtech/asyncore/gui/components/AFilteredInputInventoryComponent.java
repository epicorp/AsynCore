package net.devtech.asyncore.gui.components;

import net.devtech.asyncore.util.Size2i;
import net.devtech.asyncore.util.inv.InvWrapper;
import org.bukkit.inventory.ItemStack;
import java.awt.Point;
import java.util.function.Predicate;

/**
 * an inventory components that does not allow for items that do not match a certain requirement to be inserted
 */
public class AFilteredInputInventoryComponent extends AInventoryComponent {
	private final Predicate<ItemStack> predicate;

	public AFilteredInputInventoryComponent(InvWrapper sync, Size2i dimensions, Predicate<ItemStack> predicate) {
		super(sync, dimensions);
		this.predicate = predicate;
	}

	@Override
	public boolean attemptAdd(Point point, ItemStack add) {
		return !this.predicate.test(add);
	}

	@Override
	public boolean attemptSwap(Point point, ItemStack add, ItemStack take) {
		if (this.attemptAdd(point, add)) // if add was cancelled
			return true; // cancel
		else {
			return this.attemptTake(point, take); // remove
		}
	}
}
