package net.devtech.asyncore.blocks.world;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.devtech.asyncore.blocks.CustomBlock;
import net.devtech.asyncore.blocks.Tickable;
import net.devtech.asyncore.util.func.CoordConsumer;
import net.devtech.asyncore.world.chunk.BlockTracker;
import net.devtech.asyncore.world.chunk.DataChunk;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import java.io.IOException;

public class ChunkTicker implements BlockTracker<CustomBlock> {
	private ShortSet tickables = new ShortOpenHashSet();

	@Override
	public void init(DataChunk<CustomBlock> chunk) {
		// nothing to do
	}

	@Override
	public void set(int x, int y, int z, CustomBlock object) {
		if(object instanceof Tickable) {
			this.tickables.add((short) (x | z << 4 | y << 8));
		}
	}

	@Override
	public void remove(int x, int y, int z, CustomBlock object) {
		if(object instanceof Tickable)
			this.tickables.remove((short) (x | z << 4 | y << 8));
	}

	public void forEach(CoordConsumer consumer) {
		this.tickables.iterator().forEachRemaining(pack -> consumer.accept(pack & 15, (pack & 0xffff) >> 8, (pack >> 4) & 15));
	}

	@Writer(4734325434254L)
	public void write(PersistentOutput output) throws IOException {
		output.writeInt(this.tickables.size());
		for (short tickable : this.tickables) {
			output.writeShort(tickable);
		}
	}

	@Reader(4734325434254L)
	public void read(PersistentInput input) throws IOException {
		int size = input.readInt();
		for (int i = 0; i < size; i++) {
			this.tickables.add(input.readShort());
		}
	}
}
