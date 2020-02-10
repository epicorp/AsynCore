package net.devtech.asyncore.blocks.world;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.devtech.asyncore.blocks.core.Tickable;
import net.devtech.asyncore.util.func.CoordConsumer;
import net.devtech.asyncore.world.chunk.BlockTracker;

public class ChunkTicker implements BlockTracker<Object> {
	private ShortSet tickables;

	@Override
	public void set(int x, int y, int z, Object object) {
		if(object instanceof Tickable) {
			this.tickables.add((short) (x | z << 4 | y << 8));
		}
	}

	@Override
	public void remove(int x, int y, int z, Object object) {
		if(object instanceof Tickable)
			this.tickables.remove((short) (x | z << 4 | y << 8));
	}

	public void forEach(CoordConsumer consumer) {
		this.tickables.iterator().forEachRemaining(pack -> consumer.accept(pack & 15, (pack & 0xffff) >> 8, (pack >> 4)));
	}
}
