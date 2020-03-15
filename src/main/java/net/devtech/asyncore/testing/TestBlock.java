package net.devtech.asyncore.testing;

import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.blocks.AbstractBlock;
import net.devtech.asyncore.blocks.events.DestroyEvent;
import net.devtech.asyncore.blocks.events.PlaceEvent;
import net.devtech.asyncore.blocks.events.TickEvent;
import net.devtech.asyncore.gui.components.AHorizontalStatusBar;
import net.devtech.asyncore.gui.components.APanel;
import net.devtech.asyncore.blocks.world.events.LocalEvent;
import net.devtech.asyncore.items.blocks.BlockItem;
import net.devtech.asyncore.util.Size2i;
import net.devtech.asyncore.world.server.ServerAccess;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.awt.Point;
import java.io.IOException;

public class TestBlock extends AbstractBlock implements BlockItem {
	// the icon for a "filled" slot in the status bar
	private static final ItemStack FILL = new ItemStack(Material.STONE);
	// the icon for an "empty" slot in the status bar
	private static final ItemStack EMPTY = new ItemStack(Material.GLASS);
	// this is our display inventory, the "screen" for this block
	private final Inventory display = Bukkit.createInventory(null, 27);
	// some random info
	int i;

	public TestBlock(PersistentRegistry registry, ServerAccess<Object> access) {
		super(registry, access);
	}

	@Override
	public ItemStack createBaseStack() {
		ItemStack stack = new ItemStack(Material.DIAMOND);
		stack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
		return stack;
	}

	@LocalEvent // we have to listen to the place event to set a block at the location
	private void place(PlaceEvent event) {
		System.out.println("placed!");
		event.getBlock().setType(Material.STONE);
	}

	@LocalEvent
	public void onClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			APanel panel = new APanel(new Size2i(9, 3));
			panel.addComponent(new Point(0, 0), new AHorizontalStatusBar(FILL, EMPTY, null, () -> this.i / 100f, 9));
			panel.addComponent(new Point(0, 1), new AHorizontalStatusBar(FILL, EMPTY, null, () -> .5f, 9));
			panel.addComponent(new Point(0, 2), new AHorizontalStatusBar(FILL, EMPTY, null, () -> .75f, 9));
			AsynCore.guiManager.openGui(event.getPlayer(), panel, this.display);
		}
	}


	@LocalEvent(4) // events can have priorities too
	private void destroy(DestroyEvent event) {
		System.out.println("destroy!");
	}

	@LocalEvent
	private void tick(TickEvent event) {
		this.i = (this.i + 1) % 100; // loop i from 0 -> 100
		AsynCore.guiManager.redraw(this.display); // redraw the display every time i is updated
	}

	@Reader (2341234556789L)
	public final void read(PersistentInput input) throws IOException {
		this.i = input.readInt();
	}

	@Writer (2341234556789L)
	public final void write(PersistentOutput output) throws IOException {
		output.writeInt(this.i);
	}
}
