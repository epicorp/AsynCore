package net.devtech.asyncore.blocks.world;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.devtech.asyncore.blocks.CustomBlock;
import net.devtech.asyncore.util.ref.WorldRef;
import net.devtech.asyncore.world.chunk.AbstractDataChunk;
import net.devtech.asyncore.world.chunk.BlockTracker;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import java.io.IOException;
import java.util.function.Supplier;

public class CustomBlockDataChunk extends AbstractDataChunk<CustomBlock> {
	private final Short2ObjectMap<CustomBlock> data = new Short2ObjectOpenHashMap<>();
	private boolean isLoaded = true;
	private WorldRef ref;

	public CustomBlockDataChunk(WorldRef ref) {
		this.ref = ref;
	}

	@Deprecated
	public CustomBlockDataChunk() {}

	@Override
	protected CustomBlock getAndPut(CustomBlock _new, int x, int y, int z) {
		if(_new != null)
			_new.place(this.ref.get(), x, y, z);
		CustomBlock old = this.data.put((short) ((x & 15) | (z & 15) << 4 | y << 8), _new);
		if(old != null)
			old.destroy(this.ref.get(), x, y, z);
		return old;
	}

	@Override
	protected CustomBlock remove(int x, int y, int z) {
		CustomBlock old = this.data.remove((short) ((x & 15) | (z & 15) << 4 | y << 8));
		if(old != null)
			old.destroy(this.ref.get(), x, y, z);
		return old;
	}

	@Override
	protected CustomBlock computeIfAbsent(Supplier<CustomBlock> obj, int x, int y, int z) {
		return this.data.computeIfAbsent((short) ((x & 15) | (z & 15) << 4 | y << 8), s -> {
			CustomBlock block = obj.get();
			block.place(this.ref.get(), x, y, z);
			return block;
		});
	}

	@Override
	public CustomBlock get(int x, int y, int z) {
		return this.data.get((short) ((x & 15) | (z & 15) << 4 | y << 8)); // index compaction
	}

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	@SuppressWarnings ("unchecked")
	@Reader (9072059811478052715L)
	protected final void reader(PersistentInput input) throws IOException {
		int objects = input.readInt();
		for (int i = 0; i < objects; i++) {
			short index = input.readShort();
			CustomBlock _new = (CustomBlock) input.readPersistent();
			this.data.put(index, _new);
		}

		int trackers = input.readInt();
		for (int i = 0; i < trackers; i++) {
			BlockTracker<CustomBlock> tracker = (BlockTracker<CustomBlock>) input.readPersistent();
			tracker.init(this);
			this.trackers.add(tracker);
		}

		this.isLoaded = input.readBoolean();
		this.ref = (WorldRef) input.readPersistent();
	}

	@Writer (9072059811478052715L)
	protected final void writer(PersistentOutput output) throws IOException {
		// save data
		output.writeInt(this.data.size());
		for (Short2ObjectMap.Entry<CustomBlock> entry : Short2ObjectMaps.fastIterable(this.data)) {
			output.writeShort(entry.getShortKey());
			output.writePersistent(entry.getValue());
		}

		output.writeInt(this.trackers.size());
		for (BlockTracker<CustomBlock> tracker : this.trackers) {
			output.writePersistent(tracker);
		}

		output.writeBoolean(this.isLoaded);
		output.writePersistent(this.ref);
	}
}
