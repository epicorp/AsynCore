package net.devtech.asyncore.crafting.brewingstand;

import org.bukkit.inventory.BrewerInventory;

public interface BrewingRecipe {
	/**
	 * consume 1 
	 */
	void process(BrewerInventory inventory);

	/**
	 * if the recipe does not match, return -1.0
	 * progress the recipe, when the progress reaches 1.0, the recipe is done
	 * and {@link BrewingRecipe#process(BrewerInventory)} should be invoked shortly after,
	 * the recipe should not modify the inventory under any circumstances.
	 * @param progress the current progress
	 * @param inventory the current inventory
	 * @return the new progress
	 */
	float progress(BrewerInventory inventory, float progress);
}
