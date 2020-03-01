package net.devtech.asyncore.crafting.anvil;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface AnvilRecipe {
	/**
	 * offer the output of the recipe, or null if it's invalid
	 * @param player the player crafting the recipe
	 * @param inputs do not mutate this in any way if the recipe is not valid
	 * @return
	 */
	AnvilResult offer(Player player, ItemStack[] inputs);

	/**
	 * whether or not the recipe should override any vanilla recipe
	 * @return
	 */
	default boolean override() {
		return false;
	}
}
