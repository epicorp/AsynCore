package net.devtech.asyncore.testing;

import net.devtech.asyncore.blocks.ticking.Ticking;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import org.bukkit.World;
import java.io.IOException;

public class TestBlock implements Ticking {
	private int i;
	@Override
	public void tick(World world, int x, int y, int z) {
		world.getPlayers().forEach(p -> p.sendMessage(String.valueOf(i++)));
	}

	@Reader(2341234556789L)
	public final void read(PersistentInput input) throws IOException {
		this.i = input.readInt();
	}

	@Writer(2341234556789L)
	public final void write(PersistentOutput output) throws IOException {
		output.writeInt(this.i);
	}
}
