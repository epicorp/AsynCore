package net.devtech.asyncore.gui.components;

import net.devtech.asyncore.gui.graphics.InventoryGraphics;
import net.devtech.asyncore.util.Size2i;
import org.bukkit.inventory.ItemStack;
import java.awt.Point;

/**
 * "Item Component", a component of a GUI that is made of, or is an item
 */
public interface AComponent {
	/**
	 * draw the component on an inventory graphics screen
	 */
	void draw(InventoryGraphics inventory);

	/**
	 * attempt to insert the itemstack into the coordinate
	 * you should *not* modify the stacks, only return a result
	 * the item may not stack
	 * @return if the vent should be cancelled
	 */
	boolean attemptAdd(Point point, ItemStack add);

	/**
	 * attempt to take the itemstack at the coordinates
	 * you should *not* modify the stacks, only return a result
	 * @param point the location where the component was clicked on
	 * @param stack the amount of the stack the player is attempting to take
	 * @return if the vent should be cancelled
	 */
	boolean attemptTake(Point point, ItemStack stack);

	/**
	 * attempt to swap an item out for a different one,
	 * you should *not* modify the stacks, only return a result
	 * @return if the vent should be cancelled
	 */
	boolean attemptSwap(Point point, ItemStack add, ItemStack take);

	/**
	 * a resync should be used to resynchornize any backing inventory if there is any
	 */
	default void resync(InventoryGraphics graphics) {}

	/**
	 * @return the size of the component
	 */
	Size2i getSize();
}
