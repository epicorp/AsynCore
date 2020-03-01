package net.devtech.asyncore.crafting.craftingtable;

import net.devtech.asyncore.util.inv.Inventories;
import org.bukkit.inventory.ItemStack;
import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionalRecipe implements CraftingRecipe {
	private Function<ItemStack, Integer>[] functions;
	private Supplier<ItemStack> output;

	/**
	 * 9 functions for each crafting slot
	 * @param functions
	 */
	public FunctionalRecipe(Supplier<ItemStack> output, Function<ItemStack, Integer>...functions) {
		this.functions = functions;
		this.output = output;
	}

	@Override
	public ItemStack output(ItemStack[] matrix, boolean craftAll) {
		if(matrix.length != this.functions.length)
			return null;
		int lowest = Integer.MAX_VALUE;
		int[] inputs = new int[matrix.length];
		for (int x = 0; x < matrix.length; x++) {
			int test = this.functions[x].apply(matrix[x]);
			inputs[x] = test;
			if (test != -1) {
				int div;
				if (test != 0) div = Inventories.getAmount(matrix[x]) / test;
				else div = Integer.MAX_VALUE;
				if (div < lowest) lowest = div;
			} else return null;
		}
		if (lowest == 0) return null;
		if (!craftAll)
			lowest = 1;

		for (int x = 0; x < matrix.length; x++)
			if (matrix[x] != null)
				matrix[x].setAmount((matrix[x].getAmount() - inputs[x] * lowest));
		ItemStack output = this.output.get();
		output.setAmount(output.getAmount() * lowest);
		return output;
	}
}
