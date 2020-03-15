package net.devtech.asyncore.testing;

import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.blocks.AbstractBlock;
import net.devtech.asyncore.blocks.custom.HopperExtractable;
import net.devtech.asyncore.blocks.events.PlaceEvent;
import net.devtech.asyncore.blocks.events.TickEvent;
import net.devtech.asyncore.gui.components.AFilteredInputInventoryComponent;
import net.devtech.asyncore.gui.components.AHorizontalStatusBar;
import net.devtech.asyncore.gui.components.AOutputInventoryComponent;
import net.devtech.asyncore.gui.components.APanel;
import net.devtech.asyncore.gui.graphics.InventoryGraphics;
import net.devtech.asyncore.blocks.world.events.LocalEvent;
import net.devtech.asyncore.items.blocks.BlockItem;
import net.devtech.asyncore.util.Size2i;
import net.devtech.asyncore.util.inv.ArrayInvWrapper;
import net.devtech.asyncore.util.inv.InvWrapper;
import net.devtech.asyncore.util.inv.Inventories;
import net.devtech.asyncore.world.server.ServerAccess;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import java.awt.Point;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class SuperFurnace extends AbstractBlock implements BlockItem, HopperExtractable {
	private Inventory display = Bukkit.createInventory(null, 36);
	private static final ItemStack BACKGROUND = new ItemStack(Material.STONE);
	private ItemStack[] output = new ItemStack[9];
	private ItemStack[] input = new ItemStack[9];
	private int cooldown;

	public SuperFurnace(PersistentRegistry registry, ServerAccess<Object> access) {
		super(registry, access);
	}

	@LocalEvent
	public void onClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
			event.setCancelled(true);
			APanel panel = new APanel(new Size2i(9, 4)) {
				@Override
				public void draw(InventoryGraphics inventory) {
					super.draw(inventory);
					inventory.setItem(BACKGROUND, 0, 0);
					inventory.setItem(BACKGROUND, 0, 1);
					inventory.setItem(BACKGROUND, 0, 2);
					inventory.setItem(BACKGROUND, 4, 0);
					inventory.setItem(BACKGROUND, 4, 1);
					inventory.setItem(BACKGROUND, 4, 2);
					inventory.setItem(BACKGROUND, 8, 0);
					inventory.setItem(BACKGROUND, 8, 1);
					inventory.setItem(BACKGROUND, 8, 2);
				}
			};
			panel.addComponent(new Point(1, 0), new AFilteredInputInventoryComponent(new ArrayInvWrapper(this.input), new Size2i(3, 3), SuperFurnace::smeltable));
			panel.addComponent(new Point(5, 0), new AOutputInventoryComponent(new ArrayInvWrapper(this.output), new Size2i(3, 3)));
			panel.addComponent(new Point(0, 3), new AHorizontalStatusBar(new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.BUCKET), null, () -> this.cooldown / 10d, 9));
			AsynCore.guiManager.openGui(event.getPlayer(), panel, this.display);
		}
	}

	@LocalEvent
	private void tick(TickEvent event) {
		AsynCore.guiManager.resync(this.display); // resync inventory
		List<ItemStack> toSmelt = Inventories.remove(Inventories.clone(this.input), 9); // try smelt 9 items at once
		if(!toSmelt.isEmpty()) { // no redundant stuff
			for (int i = 0; i < toSmelt.size(); i++) { // smelt up all the items virtually
				toSmelt.set(i, smelt(toSmelt.get(i)));
			}
			if (Inventories.canAddStacks(new ArrayInvWrapper(toSmelt), new ArrayInvWrapper(this.output))) { // if output has space
				if (this.cooldown++ >= 10) {
					this.cooldown = 0;
					Inventories.remove(this.input, 9); // remove for real
					// is safe
					Inventories.addAll(new ArrayInvWrapper(toSmelt), new ArrayInvWrapper(this.output)); // add to output
				}
				AsynCore.guiManager.redraw(this.display); // redraw after smelting
			}
		} else {
			this.cooldown = 0;
			AsynCore.guiManager.redraw(this.display); // redraw after cooldown reset
		}
	}

	@Override
	public void onChange() {
		AsynCore.guiManager.redraw(this.display);
	}

	@LocalEvent
	private void place(PlaceEvent event) {
		event.getBlock().setType(Material.GLASS);
	}

	@Override
	public ItemStack createBaseStack() {
		ItemStack stack = new ItemStack(Material.GLASS);
		stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "Super furnace");
		stack.setItemMeta(meta);
		return stack;
	}

	@Writer (638954032L)
	public void writeNew(PersistentOutput output) throws IOException {
		this.write(output);
		output.writeInt(this.cooldown);
	}

	@Reader (638954032L)
	public void readNew(PersistentInput input) throws IOException { // added cooldown
		this.read(input);
		this.cooldown = input.readInt();
	}

	@Reader (43092091209L)
	public void read(PersistentInput input) throws IOException {
		for (int i = 0; i < 9; i++) {
			this.input[i] = (ItemStack) input.readPersistent();
			this.output[i] = (ItemStack) input.readPersistent();
		}
	}

	@Writer (43092091209L)
	public void write(PersistentOutput output) throws IOException {
		for (int i = 0; i < 9; i++) {
			output.writePersistent(this.input[i], true);
			output.writePersistent(this.output[i], true);
		}
	}

	public static ItemStack smelt(ItemStack stack) {
		if (stack == null) return null;
		Iterator<Recipe> iterator = Bukkit.recipeIterator();
		while (iterator.hasNext()) {
			Recipe recipe = iterator.next();
			if (recipe instanceof FurnaceRecipe) {
				boolean input = ((FurnaceRecipe) recipe).getInput().isSimilar(stack);
				if (input) {
					ItemStack in = recipe.getResult().clone();
					in.setAmount(stack.getAmount());
					return in;
				}
			}
		}

		return stack;
	}

	public static boolean smeltable(ItemStack stack) {
		if (stack == null) return false;
		Iterator<Recipe> iterator = Bukkit.recipeIterator();
		while (iterator.hasNext()) {
			Recipe recipe = iterator.next();
			if (recipe instanceof FurnaceRecipe) {
				boolean input = ((FurnaceRecipe) recipe).getInput().isSimilar(stack);
				if (input) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public @Nullable InvWrapper getInventory(@Nullable BlockFace face) {
		if (face == BlockFace.DOWN) {
			return new ArrayInvWrapper(this.output);
		}
		return new ArrayInvWrapper(this.input);
	}
}
