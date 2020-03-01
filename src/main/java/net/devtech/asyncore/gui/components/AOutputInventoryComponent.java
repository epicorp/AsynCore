package net.devtech.asyncore.gui.components;

import net.devtech.asyncore.util.Size2i;
import net.devtech.asyncore.util.inv.InvWrapper;
import org.bukkit.inventory.ItemStack;
import java.awt.Point;

public class AOutputInventoryComponent extends AInventoryComponent {
	public AOutputInventoryComponent(InvWrapper sync, Size2i dimensions) {
		super(sync, dimensions);
	}

	@Override
	public boolean attemptAdd(Point point, ItemStack add) {
		return true;
	}

	@Override
	public boolean attemptSwap(Point point, ItemStack add, ItemStack take) {
		return true;
	}
}
