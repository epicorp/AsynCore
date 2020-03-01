package net.devtech.asyncore.crafting.craftingtable;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface CraftingRecipe {
	/**
	 * craft the item
	 * @param matrix mutate this ONLY if there is a isValid recipe
	 * @param craftAll if the crafting operation was a shift click, then this will be true (craft with matrix until empty)
	 * @return the output item, or null if the recipe was invalid
	 */
	ItemStack output(ItemStack[] matrix, boolean craftAll);

	/**
	 * an action to perform when the player has crafted something
	 * @param player
	 */
	default void onFinish(Player player) {
		player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, .5f, 1f);
	}
}
