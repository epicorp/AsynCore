package net.devtech.asyncore.blocks.world;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.devtech.asyncore.world.chunk.AbstractDataChunk;
import net.devtech.asyncore.world.chunk.BlockTracker;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import java.io.IOException;
import java.util.function.Supplier;

public class CustomBlockDataChunk extends AbstractDataChunk<Object> {
	private final Short2ObjectMap<Object> data = new Short2ObjectOpenHashMap<>();
	private boolean isLoaded = true;

	@Override
	protected Object put(Object _new, int x, int y, int z) {
		return this.data.put((short) (x | z << 4 | y << 8), _new);
	}

	@Override
	protected Object computeIfAbsent(Supplier<Object> obj, int x, int y, int z) {
		return this.data.computeIfAbsent((short) (x | z << 4 | y << 8), s -> obj.get());
	}

	@Override
	public Object get(int x, int y, int z) {
		return this.data.get((short) (x | z << 4 | y << 8)); // index compaction
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
			Object _new = input.readPersistent();
			this.data.put(index, _new);
		}

		int trackers = input.readInt();
		for (int i = 0; i < trackers; i++) {
			BlockTracker<Object> tracker = (BlockTracker<Object>) input.readPersistent();
			tracker.init(this);
			this.trackers.add(tracker);
		}

		this.isLoaded = input.readBoolean();
	}

	@Writer (9072059811478052715L)
	protected final void writer(PersistentOutput output) throws IOException {
		// save data
		output.writeInt(this.data.size());
		for (Short2ObjectMap.Entry<Object> entry : Short2ObjectMaps.fastIterable(this.data)) {
			output.writeShort(entry.getShortKey());
			output.writePersistent(entry.getValue());
		}

		output.writeInt(this.trackers.size());
		for (BlockTracker<Object> tracker : this.trackers) {
			output.writePersistent(tracker);
		}

		output.writeBoolean(this.isLoaded);
	}
}
