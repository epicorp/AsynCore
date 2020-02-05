package net.devtech.asyncore.blocks.world;

import it.unimi.dsi.fastutil.shorts.*;
import net.devtech.asyncore.blocks.core.RandTickable;
import net.devtech.asyncore.blocks.core.Tickable;
import net.devtech.asyncore.util.ref.WorldRef;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import org.bukkit.World;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * there's alot of code duplication here, mostly micro optimizations but eh
 * al the methods here do not have to be relativized (they can be absolute coordinates to 0, 0, they will be auto adjusted)
 */
public class Chunk {
	private static final int CHUNK_SIZE = 16 * 256 * 16;
	private static final Random CHUNK_RANDOM = new Random();
	private final ShortSet tickables = new ShortOpenHashSet();
	final Short2ObjectMap<Object> data = new Short2ObjectOpenHashMap<>();
	WorldRef world;
	boolean isLoaded = true;

	// for serialization
	@Deprecated
	public Chunk() {}

	// for world stuff
	public Chunk(World world) {
		this.world = new WorldRef(world);
	}

	/**
	 * get the object stored at the given position
	 */
	public <T> T get(int x, int y, int z) {
		return (T) this.data.get((short) (x & 15 | (z & 15) << 4 | y << 8)); // index compaction
	}

	/**
	 * get the old object at the location and replace it with the new one
	 *
	 * @return the old object
	 */
	public <T> T getAndSet(int x, int y, int z, Object _new) {
		short index = (short) (x & 15 | (z & 15) << 4 | y << 8); // index compaction
		T old = (T) this.data.put(index, _new);
		if (_new instanceof Tickable)
			this.tickables.add(index);
		return old;
	}

	/**
	 * get and remove the object at the given location
	 *
	 * @return the old object
	 */
	public <T> T getAndRemove(int x, int y, int z) {
		short index = (short) (x & 15 | (z & 15) << 4 | y << 8); // index compaction
		T old = (T) this.data.remove(index);
		if (old instanceof Tickable) this.tickables.remove(index);
		return old;
	}

	/**
	 * set the object at the given location, or abort if there's already one there
	 *
	 * @return true if the object was set
	 */
	public boolean setOrAbort(int x, int y, int z, Supplier<Object> object) {
		short index = (short) (x & 15 | (z & 15) << 4 | y << 8); // index compaction
		Object[] ref = {null};
		Object returned = this.data.computeIfAbsent(index, i -> ref[0] = object.get());
		return ref[0] == returned; // hacc
	}
	/**
	 * tick all the tickable blocks inside the chunk
	 */
	public void tick(int cx, int cz) {
		final int offx = cx * 16;
		final int offz = cz * 16;
		this.tickables.forEach((IntConsumer) pack -> {
			int ux = pack & 15, uz = (pack >> 4) & 15, uy = ((pack & 0xffff) >> 8);
			((Tickable) (this.data.get((short) pack))).tick(this.world.get(), offx + ux, uy, offz + uz);
		});
	}

	public void randTick(int cx, int cz, int ticks) {
		final int offx = cx * 16;
		final int offz = cz * 16;
		// we can technically keep track of the random ticking blocks and tick them that way, but meh.
		for (int i = 0; i < ticks; i++) {
			short pack = (short) CHUNK_RANDOM.nextInt(CHUNK_SIZE);
			Object object = this.data.get(pack);
			if (object instanceof RandTickable) {
				int ux = pack & 15, uz = (pack >> 4) & 15, uy = ((pack & 0xffff) >> 8);
				((RandTickable) object).randTick(offx + ux, uy, offz + uz);
			}
		}
	}

	public boolean isLoaded() {
		return this.isLoaded;
	}

	@Reader (9072059811478052715L)
	protected final void reader(PersistentInput input) throws IOException {
		int objects = input.readInt();
		for (int i = 0; i < objects; i++) {
			this.data.put(input.readShort(), input.readPersistent());
		}
		this.world = (WorldRef) input.readPersistent();
		int i = input.readInt();
		for (int i1 = 0; i1 < i; i1++) {
			this.tickables.add(input.readShort());
		}
	}

	@Writer (9072059811478052715L)
	protected final void writer(PersistentOutput output) throws IOException {
		// save data
		output.writeInt(this.data.size());
		for (Short2ObjectMap.Entry<Object> entry : Short2ObjectMaps.fastIterable(this.data)) {
			output.writeShort(entry.getShortKey());
			output.writePersistent(entry.getValue());
		}
		// write world ref
		output.writePersistent(this.world);
		// write tickables
		output.writeInt(this.tickables.size());
		for (Short tickable : this.tickables) {
			output.writeShort(tickable);
		}
	}
}
