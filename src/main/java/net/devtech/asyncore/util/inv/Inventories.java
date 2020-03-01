package net.devtech.asyncore.util.inv;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Inventories {
	/**
	 * takes 1 item from the export inventory and adds it to the input inventory
	 * this emulates a hopper
	 *
	 * @return true if there was enough space in the input inventory
	 */
	public static boolean mergeOne(@NotNull Inventory export, @NotNull Inventory input) {
		return mergeOne(new InventoryInvWrapper(export), new InventoryInvWrapper(input));
	}

	public static boolean mergeOne(@NotNull ItemStack[] export, @NotNull ItemStack[] input) {
		return mergeOne(new ArrayInvWrapper(export), new ArrayInvWrapper(input));
	}

	public static boolean mergeOne(@NotNull InvWrapper export, @NotNull InvWrapper input) {
		for (int i = 0; i < export.size(); i++) {
			ItemStack content = export.getStack(i);
			if (content != null) {
				ItemStack stack = content.clone();
				stack.setAmount(1);
				if (canAddStack(stack, input)) {
					if (getAmount(addStack(stack, input)) == 0) { // none returned
						remove(stack, export);
						return true;
					}
					return false; // unable to merge stack
				}
			}
		}
		return false;
	}

	public static int getLeftover(@NotNull ItemStack stack, @NotNull InvWrapper wrapper) {
		int items = stack.getAmount();
		for (int i = 0; i < wrapper.size(); i++) {
			ItemStack current = wrapper.getStack(i);
			if (canStack(current, stack))
				items -= getLeft(current); // subtract the left over space for stacking in the item
			if (items <= 0) // if there is enough space, we good
				return 0;
		}
		return items;
	}

	/**
	 * @see Inventories#getLeftover(ItemStack, ItemStack[])
	 */
	public static int getLeftover(@NotNull ItemStack stack, @NotNull Inventory target) {
		return getLeftover(stack, new InventoryInvWrapper(target));
	}

	/**
	 * gets the amount of items that would be left over after a merge with the target inventory
	 *
	 * @param stack the stack
	 * @param target the target inventory
	 * @return the amount of that item that would have been excluded
	 */
	public static int getLeftover(@NotNull ItemStack stack, @NotNull ItemStack[] target) {
		return getLeftover(stack, new ArrayInvWrapper(target));
	}


	@Nullable
	public static ItemStack addStack(@NotNull ItemStack stack, @NotNull InvWrapper wrapper) {
		stack = stack.clone();
		int items = stack.getAmount();
		for (int i = 0; i < wrapper.size(); i++) {
			ItemStack current = wrapper.getStack(i);
			if (current == null) {// if there is a totally empty slot in the inventory, then there is definitely space for an item
				wrapper.setStack(stack.clone(), i);
				return null;
			} else if (current.isSimilar(stack)) {
				int old = items;
				items -= getLeft(current); // subtract the left over space for stacking in the item
				if (items > 0) current.setAmount(current.getMaxStackSize());
				else current.setAmount(old + current.getAmount());
			}
			if (items <= 0) // if there is enough space, we good
				return null;
		}
		ItemStack left = stack.clone();
		left.setAmount(items);
		return left;
	}

	/**
	 * adds the stack to the itemstacks
	 *
	 * @param stack the stack to add
	 * @param target the target stack
	 * @return the remaining stacks
	 */
	public static ItemStack addStack(@NotNull ItemStack stack, @NotNull ItemStack[] target) {
		return addStack(stack, new ArrayInvWrapper(target));
	}

	public static boolean canAddStack(@NotNull ItemStack stack, @NotNull InvWrapper invWrapper) {
		return getLeftover(stack, invWrapper) == 0;
	}

	/**
	 * checks if the inventory has space for the item
	 * uses {@link Inventory#getStorageContents()}
	 *
	 * @param stack the item
	 * @param inventory the inventory to insert
	 */
	public static boolean canAddStack(@NotNull ItemStack stack, @NotNull Inventory inventory) {
		return canAddStack(stack, new InventoryInvWrapper(inventory));
	}

	/**
	 * Check if the inventory has enough space for the itemstack
	 *
	 * @param stack the stack you wish to check for
	 * @param target the "inventory" you want to check for space
	 */
	public static boolean canAddStack(ItemStack stack, ItemStack[] target) {
		return canAddStack(stack, new ArrayInvWrapper(target));
	}

	public static boolean canAddStacks(@NotNull InvWrapper from, @NotNull InvWrapper to) {
		int[] set = new int[to.size()]; // to stack size copy
		Arrays.fill(set, -1); // fill with uninitialized
		for (int fromIndex = 0; fromIndex < from.size(); fromIndex++) {
			ItemStack adding = from.getStack(fromIndex);
			int addingSize = getAmount(adding);
			for (int toIndex = 0; toIndex < to.size(); toIndex++) {
				ItemStack merging = to.getStack(toIndex);
				if (set[toIndex] == -1) set[toIndex] = merging == null ? 0 : merging.getAmount(); // 10
				if (canStack(adding, merging)) {
					int max = merging == null ? 64 : merging.getMaxStackSize(); // max amount of items that can be merged
					int left = max - set[toIndex]; // how many more items the stack can accomodate
					set[toIndex] = Math.min(set[toIndex] + addingSize, max);
					addingSize -= left;
					// if add was able to merge fully, break
					if (addingSize <= 0) {
						break;
					}
				}
			}
			if (addingSize > 0) return false;
		}
		return true;
	}

	public static boolean canStack(@Nullable ItemStack a, @Nullable ItemStack b) {
		return a == null || b == null || a.isSimilar(b);
	}

	/**
	 * checks if the inventory has space for all of the items from both inventories
	 */
	public static boolean canAddStacks(@NotNull ItemStack[] stacks, @NotNull ItemStack[] input) {
		return canAddStacks(new ArrayInvWrapper(stacks), new ArrayInvWrapper(input));
	}

	public static boolean canAddStacks(@NotNull Inventory stacks, @NotNull Inventory input) {
		return canAddStacks(new InventoryInvWrapper(stacks), new InventoryInvWrapper(input));
	}

	/**
	 * returns the remaining capacity for stacking items
	 */
	public static int getLeft(@Nullable ItemStack stacks) {
		return stacks == null ? 64 : stacks.getMaxStackSize() - stacks.getAmount();
	}

	/**
	 * condense the stacks of items unsafely (ignoring the max stack size)
	 */
	@NotNull
	@Contract ("_ -> new")
	public static ItemStack[] condenseUnsafe(ItemStack[] stacks) {
		List<ItemStack> stackList = new ArrayList<>();
		for (ItemStack stack : stacks) {
			boolean newstack = true;
			for (ItemStack itemStack : stackList) {
				if (itemStack.isSimilar(stack)) {
					newstack = false;
					itemStack.setAmount(itemStack.getAmount() + stack.getAmount());
				}
			}
			if (stack != null && newstack) stackList.add(stack.clone());
		}

		return stackList.toArray(new ItemStack[0]);
	}

	/**
	 * uncondenses illegal stacks of items (items that exceed their maximum stack size)
	 */
	@NotNull
	@Contract ("_ -> new")
	public static ItemStack[] uncondense(ItemStack[] stack) {
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemStack itemStack : stack) {
			int max = itemStack.getMaxStackSize();
			int cur = itemStack.getAmount();
			if (max > cur) // if the stack is already safe
				stacks.add(itemStack.clone()); // add it
			else { // if the item is an unsafe (larger than max size)
				if (max == 0) continue;
				int over = cur / max;
				for (int x = 0; x < over; x++) {
					ItemStack newStack = itemStack.clone();
					newStack.setAmount(newStack.getMaxStackSize());
					stacks.add(newStack);
				}
				if (over * max != cur) {// if there is a remainder
					ItemStack newStack = itemStack.clone();
					newStack.setAmount(cur - over * max);
					stacks.add(newStack);
				}
			}
		}
		return stacks.toArray(new ItemStack[0]);
	}

	/**
	 * will replace all air stacks with null
	 */
	@Contract(mutates = "param1")
	private static void clean(InvWrapper stacks) {
		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.getStack(i);
			if (stack != null && (stack.getType() == Material.AIR || stack.getAmount() == 0)) stacks.setStack(null, i);
		}
	}

	public static void clean(ItemStack[] stacks) {
		clean(new ArrayInvWrapper(stacks));
	}

	public static void clean(Inventory inventory) {
		clean(new InventoryInvWrapper(inventory));
	}

	@NotNull
	@Contract(mutates = "param1")
	public static ItemStack[] sort(ItemStack[] array) {
		Arrays.sort(array, Comparator.comparingInt(ItemStack::hashCode));
		return array;
	}

	/**
	 * bad implementation
	 */
	public static boolean contains(ItemStack[] stacks, ItemStack[] inventory) {
		stacks = condenseUnsafe(stacks);
		inventory = condenseUnsafe(inventory);
		for (ItemStack stack : stacks)
			if (!containsUnsafe(stack, inventory)) return false;
		return true;
	}

	/**
	 * assumes the stacks contents has been condensed Unsafely
	 */
	public static boolean containsUnsafe(ItemStack stack, ItemStack[] stacks) {
		for (ItemStack itemStack : stacks)
			if (isSimilar(stack, itemStack)) return getAmount(stack) <= getAmount(itemStack);
		return false;
	}

	@NotNull
	@Contract ("_ -> new")
	public static ItemStack[] removeNulls(ItemStack[] stacks) {
		List<ItemStack> newStacks = new ArrayList<>();
		for (ItemStack stack : stacks) if (stack != null) newStacks.add(stack.clone());
		return newStacks.toArray(new ItemStack[0]);
	}

	public static ItemStack stack(Material material, int amount) {
		return new ItemStack(material, amount);
	}

	public static ItemStack stack(Material material) {
		return stack(material, 1);
	}

	public static boolean isSimilar(@Nullable ItemStack a, @Nullable ItemStack b) {
		if ((a == null) != (b == null)) return false;
		return a == null || a.isSimilar(b);
	}

	public static int getAmount(ItemStack a) {
		return a == null ? 0 : a.getAmount();
	}

	@Contract ("_ -> new")
	public static ItemStack[] clone(ItemStack[] stacks) {
		ItemStack[] inv = new ItemStack[stacks.length];
		for (int i = 0; i < stacks.length; i++) {
			if (stacks[i] != null) inv[i] = stacks[i].clone();
		}
		return inv;
	}

	/**
	 * removes a certain amount from every item stack in the contents
	 */
	public static void removeNFrom(ItemStack[] input, int sub) {
		for (ItemStack stack : input) {
			if (stack != null) stack.setAmount(stack.getAmount() - sub);
		}
	}

	public static ItemStack remove(ItemStack stack, ItemStack[] stacks) {
		return remove(stack, new ArrayInvWrapper(stacks));
	}

	public static ItemStack remove(ItemStack stack, Inventory inventory) {
		return remove(stack, new InventoryInvWrapper(inventory));
	}

	/**
	 * remove the item stack from the array
	 *
	 * @param stack the stack to remove
	 * @param array the array to deduct from
	 * @return the amount of items that could not be removed
	 */
	public static ItemStack remove(ItemStack stack, InvWrapper array) {
		stack = stack.clone();
		int count = stack.getAmount(); // get current amount
		for (int i = 0; i < array.size(); i++) {
			ItemStack current = array.getStack(i);
			if (isSimilar(current, stack)) { // if they are the same items
				int amount = current.getAmount(); // amount of items in that slot
				amount -= count;
				if (amount <= 0) { // if the stack is too small
					array.setStack(null, i);
					count = Math.abs(amount); // and set the counter to the remainder
				} else {
					current.setAmount(amount); // if it's large enough
					return null; // all of the stack was added
				}
			}
		}
		ItemStack newStack = stack.clone();
		newStack.setAmount(count);
		return newStack;
	}

	private static boolean contains(ItemStack stack, InvWrapper wrapper) {
		int size = stack.getAmount();
		for (int i = 0; i < wrapper.size(); i++) {
			ItemStack current = wrapper.getStack(i);
			if (isSimilar(stack, current)) size -= getAmount(current);
			if (size <= 0) return true;
		}
		return false;
	}

	public static boolean contains(ItemStack stack, ItemStack[] stacks) {
		return contains(stack, new ArrayInvWrapper(stacks));
	}

	public static boolean contains(ItemStack stack, Inventory inventory) {
		return contains(stack, new InventoryInvWrapper(inventory));
	}

	/**
	 * gets the first non-null itemstack
	 */
	public static ItemStack getFirst(Inventory inventory) {
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack != null) return stack;
		}
		return null;
	}

	/**
	 * checks if the itemstack is empty (air/null/0)
	 */
	public static boolean empty(ItemStack stack) {
		return stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0;
	}

	/**
	 * decreases the amount of items in the stack by amount, or returns null if the stack is now empty
	 *
	 * @param amount the amount to decrement by
	 * @return a cloned stack or null
	 */
	public static ItemStack decrement(ItemStack stack, int amount) {
		if (stack.getAmount() - amount > 0) {
			stack = stack.clone();
			stack.setAmount(stack.getAmount() - amount);
		} else stack = null;
		return stack;
	}

	/**
	 * equivalent to {@link Inventories#decrement(ItemStack, int)} but only by 1
	 */
	public static ItemStack decrement(ItemStack stack) {
		return decrement(stack, 1);
	}

	public static List<ItemStack> addAll(InvWrapper from, InvWrapper to) {
		List<ItemStack> extra = new ArrayList<>();
		for (int i = 0; i < from.size(); i++) {
			extra.add(addStack(from.getStack(i), to));
		}
		return extra;
	}

	public static List<ItemStack> remove(ItemStack[] stacks, int count) {
		return remove(new ArrayInvWrapper(stacks), count);
	}

	/**
	 * removes X amount of items from the container
	 */
	public static List<ItemStack> remove(InvWrapper wrapper, int count) {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < wrapper.size(); i++) {
			ItemStack stack = wrapper.getStack(i);
			int amount = getAmount(stack);
			if (amount - count <= 0) { // take out more/eqal items than there are in the stack
				if (stack != null) stacks.add(stack.clone());
				wrapper.setStack(null, i);
				count -= amount;
			} else { // stack has extra even after discount
				stack.setAmount(amount - count); // discount
				ItemStack clone = stack.clone();
				clone.setAmount(count);
				stacks.add(clone);
				count = 0;
			}

			if (count == 0) break;
		}
		return stacks;
	}


	/**
	 * get the width of an inventory
	 */
	public static int getWidth(InventoryType type) {
		switch (type) {
			case WORKBENCH:
			case DROPPER:
			case MERCHANT:
			case DISPENSER:
				return 3;
			case CRAFTING:
				return 4;
			case ANVIL:
			case BEACON:
			case BREWING:
			case FURNACE:
			case ENCHANTING:
				return 1;
			case CHEST:
			case CREATIVE:
			case PLAYER:
			case ENDER_CHEST:
			case SHULKER_BOX:
				return 9;
			case HOPPER:
				return 5;
			default:
				throw new UnsupportedOperationException("Unknown inventory type " + type);
		}
	}

	public static ItemStack clone(ItemStack item) {
		return item == null ? null : item.clone();
	}
}