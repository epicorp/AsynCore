package net.devtech.asyncore.crafting.craftingtable;

import net.devtech.asyncore.util.inv.Inventories;
import org.bukkit.inventory.ItemStack;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class CShaped implements CraftingRecipe {

	private final ItemStack[] inputs;
	private final Supplier<ItemStack> ouput;
	private final BiPredicate<ItemStack, ItemStack> isSimilar;
	public CShaped(Supplier<ItemStack> output, BiPredicate<ItemStack, ItemStack> isSimilar, ItemStack... inputs) {
		this.inputs = inputs;
		this.ouput = output;
		this.isSimilar = isSimilar;
	}

	public CShaped(ItemStack output, BiPredicate<ItemStack, ItemStack> isSimilar, ItemStack...inputs) {
		this(output::clone, isSimilar, inputs);
	}

	public CShaped(Supplier<ItemStack> output, ItemStack... inputs) {
		this(output, Inventories::isSimilar, inputs);
	}

	public CShaped(ItemStack output, ItemStack...inputs) {
		this(output::clone, inputs);
	}

	@Override
	public ItemStack output(ItemStack[] matrix, boolean craftAll) {
		if(matrix.length != this.inputs.length)
			return null;
		int lowest = Integer.MAX_VALUE;
		for (int x = 0; x < matrix.length; x++)
			if (this.isSimilar.test(matrix[x], this.inputs[x])) {
				int inps = Inventories.getAmount(this.inputs[x]);
				int div;
				if (inps != 0) div = Inventories.getAmount(matrix[x]) / inps;
				else div = Integer.MAX_VALUE;
				if (div < lowest) lowest = div;
			} else return null;
		if (lowest == 0) return null;
		if (!craftAll)
			lowest = 1;

		for (int x = 0; x < matrix.length; x++)
			if (matrix[x] != null)
				matrix[x].setAmount((matrix[x].getAmount() - this.inputs[x].getAmount() * lowest));
		ItemStack output = this.ouput.get();
		output.setAmount(output.getAmount() * lowest);
		return output;
	}
}
