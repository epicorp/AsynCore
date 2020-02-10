package net.devtech.asyncore.testing;

import net.devtech.asyncore.blocks.core.Tickable;
import net.devtech.asyncore.blocks.events.LocalEvent;
import net.devtech.asyncore.blocks.events.LocalListener;
import net.devtech.asyncore.events.UpdateEvent;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import org.bukkit.World;
import java.io.IOException;

public class TestBlock implements Tickable, LocalListener {
	private int i;
	@Override
	public void tick(World world, int x, int y, int z) {
		System.out.printf("I'm at %d %d %d in %s and my 'i' is %d\n", x, y, z, world, this.i++);
	}

	@Reader(2341234556789L)
	public final void read(PersistentInput input) throws IOException {
		this.i = input.readInt();
	}

	@Writer(2341234556789L)
	public final void write(PersistentOutput output) throws IOException {
		output.writeInt(this.i);
	}

	@LocalEvent
	private void update(UpdateEvent event) {
		System.out.println(event);
	}
}
