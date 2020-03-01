package net.devtech.asyncore.gui.components;

import net.devtech.asyncore.gui.graphics.InventoryGraphics;
import net.devtech.asyncore.util.Size2i;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * a sideways progress bar
 */
public class AHorizontalStatusBar implements AComponent {
	private final ItemStack fill;
	private final ItemStack empty;
	private final ItemStack icon;
	private final int rows;
	private final DoubleSupplier progress;
	private final Size2i size2i;

	/**
	 * @param fill the item that represents a "filled" slot
	 * @param empty the item that represents an "empty" slot
	 * @param icon the icon for the status bar, is optional, set to null if none
	 * @param rows the number of items across the status bar can occupy
	 */
	public AHorizontalStatusBar(ItemStack fill, ItemStack empty, @Nullable ItemStack icon, DoubleSupplier progress, int rows) {
		this.fill = fill;
		this.empty = empty;
		this.icon = icon;
		this.rows = rows;
		this.progress = progress;
		this.size2i = new Size2i(rows, 1);
	}

	public double getCurrentProgress() {
		return this.progress.getAsDouble();
	}


	@Override
	public void draw(InventoryGraphics inventory) {
		int len = this.rows;
		if (this.icon != null) len--;
		double progress = this.progress.getAsDouble();
		int filled = Math.min((int) Math.round(len * progress), this.rows);
		ItemStack fillClone = displayProgress(this.fill, progress);
		for (int i = 0; i < filled; i++) {
			inventory.setItem(fillClone, i, 0);
		}

		ItemStack emptyClone = displayProgress(this.empty, progress);
		for (int i = filled; i < len; i++) {
			inventory.setItem(emptyClone, i, 0);
		}

		if (this.icon != null) {
			inventory.setItem(displayProgress(this.icon, progress), this.rows - 1, 0);
		}
	}

	@Override
	public boolean attemptAdd(Point point, ItemStack add) {
		return true;
	}

	@Override
	public boolean attemptTake(Point point, ItemStack stack) {
		return true;
	}

	@Override
	public boolean attemptSwap(Point point, ItemStack add, ItemStack take) {
		return true;
	}


	@Override
	public Size2i getSize() {
		return this.size2i;
	}

	protected static ItemStack displayProgress(ItemStack stack, double progress) {
		ItemStack clone = stack.clone();
		ItemMeta meta = clone.getItemMeta();
		List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
		lore.add(String.format("%s%3.3f", getColor(progress), progress * 100));
		if(progress > .75) {
			clone.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		}
		meta.setLore(lore);
		clone.setItemMeta(meta);
		return clone;
	}

	protected static ChatColor getColor(double progress) {
		if (progress < .25) {
			return ChatColor.RED;
		} else if (progress < .5) {
			return ChatColor.GOLD;
		} else if (progress < .75) {
			return ChatColor.YELLOW;
		} else if (progress < 1) {
			return ChatColor.GREEN;
		} else {
			return ChatColor.WHITE;
		}
	}
}
