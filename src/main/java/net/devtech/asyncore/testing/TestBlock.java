package net.devtech.asyncore.testing;

import net.devtech.asyncore.blocks.CustomBlock;
import net.devtech.asyncore.blocks.Tickable;
import net.devtech.asyncore.blocks.world.events.LocalListener;
import net.devtech.asyncore.items.blocks.BlockItem;
import net.devtech.asyncore.world.server.ServerAccess;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import java.io.IOException;

public class TestBlock extends BlockItem implements Tickable, LocalListener {
	private int i;
	public TestBlock(PersistentRegistry registry, ServerAccess<CustomBlock> access) {
		super(registry, access);
	}

	@Override
	public void tick(World world, int x, int y, int z) {
		//Bukkit.broadcastMessage(String.format("I'm at %d %d %d in %s and my 'i' is %d\n", x, y, z, world, this.i++));
	}

	@Override
	public ItemStack createBaseStack() {
		ItemStack stack = new ItemStack(Material.DIAMOND);
		stack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
		return stack;
	}

	@Reader(2341234556789L)
	public final void read(PersistentInput input) throws IOException {
		this.i = input.readInt();
	}

	@Writer(2341234556789L)
	public final void write(PersistentOutput output) throws IOException {
		output.writeInt(this.i);
	}

	@Override
	public void place(World world, int x, int y, int z) {
		Bukkit.broadcastMessage(String.format("I'm at %d %d %d in %s and my 'i' is %d\n", x, y, z, world, this.i++));
		world.getBlockAt(x, y, z).setType(Material.STONE);
	}

	@Override
	public void destroy(World world, int x, int y, int z) {
		System.out.println("ohno");
	}
}
