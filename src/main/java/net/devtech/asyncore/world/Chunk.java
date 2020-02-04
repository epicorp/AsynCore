package net.devtech.asyncore.world;

import it.unimi.dsi.fastutil.shorts.Short2BooleanMaps;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.devtech.asyncore.api.ticking.Ticking;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInputStream;
import net.devtech.yajslib.io.PersistentOutputStream;
import java.io.IOException;
import java.util.function.IntConsumer;

/**
 * there's alot of code duplication here, mostly micro optimizations but eh
 * al the methods here do not have to be relativized (they can be absolute coordinates to 0, 0, they will be auto adjusted)
 */
public class Chunk {
	private final ShortSet tickables = new ShortOpenHashSet();
	private final Object[] data = new Object[65534];
	boolean isLoaded = true;

	/**
	 * get the object stored at the given position
	 */
	public <T> T get(int x, int y, int z) {
		return (T) this.data[x & 15 | (z & 15) << 4 | y << 8]; // index compaction
	}

	/**
	 * get the old object at the location and replace it with the new one
	 *
	 * @return the old object
	 */
	public <T> T getAndSet(int x, int y, int z, Object _new) {
		int index = x & 15 | (z & 15) << 4 | y << 8; // index compaction
		T old = (T) this.data[index];
		this.data[index] = _new;
		if(_new instanceof Ticking)
			this.tickables.add((short) index);
		return old;
	}

	/**
	 * get and remove the object at the given location
	 *
	 * @return the old object
	 */
	public <T> T getAndRemove(int x, int y, int z) {
		int index = x & 15 | (z & 15) << 4 | y << 8; // index compaction
		T old = (T) this.data[index];
		this.data[index] = null;
		if(old instanceof Ticking) this.tickables.remove((short) index);
		return old;
	}

	/**
	 * set the object at the given location, or abort if there's already one there
	 *
	 * @return true if the object was set
	 */
	public boolean setOrAbort(int x, int y, int z, Object object) {
		int index = x & 15 | (z & 15) << 4 | y << 8; // index compaction
		Object old = this.data[index];
		if (old == null) {
			this.data[index] = object;
			if(object instanceof Ticking)
				this.tickables.add((short) index);
			return true;
		} else return false;
	}

	public void tick(int cx, int cz) {
		final int offx = cx*16;
		final int offz = cz*16;
		this.tickables.forEach((IntConsumer) pack -> {
			int ux = pack & 15, uz = (pack >> 4) & 15, uy = ((pack & 0xffff) >> 8);
			((Ticking)(this.data[pack])).tick(offx + ux, uy, offz + uz);
		});
	}

	@Reader (9072059811478052715L)
	protected final void reader(PersistentInputStream input) throws IOException {
		input.readArray(this.data);
	}

	@Writer (9072059811478052715L)
	protected final void writer(PersistentOutputStream output) throws IOException {
		output.writeArray(this.data);
	}
}
