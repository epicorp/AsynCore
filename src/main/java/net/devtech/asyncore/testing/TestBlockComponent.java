package net.devtech.asyncore.testing;

import net.devtech.asyncore.gui.components.AComponent;
import net.devtech.asyncore.gui.graphics.InventoryGraphics;
import net.devtech.asyncore.util.Size2i;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.awt.Point;

public class TestBlockComponent implements AComponent {
	private final TestBlock block;

	public TestBlockComponent(TestBlock block) {this.block = block;}

	@Override
	public void draw(InventoryGraphics inventory) {
		inventory.setItem(new ItemStack(Material.STONE, this.block.i), 0, 0);
	}

	@Override
	public boolean attemptAdd(Point point, ItemStack add) {
		System.out.println("attempting to add " + add + " at " + point);
		return true;
	}

	@Override
	public boolean attemptTake(Point point, ItemStack stack) {
		System.out.println("attempting to take " + stack + " at " + point);
		return true;
	}

	@Override
	public boolean attemptSwap(Point point, ItemStack add, ItemStack take) {
		System.out.println("attempting to swap " + take + " for " + add + " at " + point);
		return true;
	}

	@Override
	public Size2i getSize() {
		return new Size2i(9, 4);
	}
}
