package net.devtech.asyncore.world.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * must implement your own serializer, and it must serialize the trackers as well!
 *
 * @param <T>
 */
public abstract class AbstractDataChunk<T> implements DataChunk<T> {
	protected final List<BlockTracker<T>> trackers = new ArrayList<>();
	private boolean isLoaded = true;
	protected abstract T put(T _new, int x, int y, int z);
	protected abstract T computeIfAbsent(Supplier<T> obj, int x, int y, int z);

	@Override
	public T getAndSet(int x, int y, int z, T _new) {
		this.updateTrackersSet(_new, x, y, z);
		return this.updateTrackersRemove(this.put(_new, x, y, z), x, y, z);
	}

	@Override
	public T getAndRemove(int x, int y, int z) {
		return this.updateTrackersRemove(this.put(null, x, y, z), x, y, z);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public boolean setOrAbort(int x, int y, int z, Supplier<T> object) {
		Object[] ref = {null};
		T returned = this.computeIfAbsent(() -> (T) (ref[0] = object.get()), x, y, z);
		boolean set = ref[0] == returned;
		if (set) this.updateTrackersSet(returned, x, y, z);
		return set; // hacc
	}

	private T updateTrackersRemove(T object, int x, int y, int z) {
		for (BlockTracker<T> tracker : this.trackers) {
			tracker.remove(x, y, z, object);
		}
		return object;
	}

	private void updateTrackersSet(T object, int x, int y, int z) {
		for (BlockTracker<T> tracker : this.trackers) {
			tracker.set(x, y, z, object);
		}
	}

	@Override
	public boolean loaded() {
		return this.isLoaded;
	}

	@Override
	public void setLoaded(boolean loaded) {
		this.isLoaded = loaded;
	}

	@Override
	public BlockTracker<T> getTracker(int key) {
		return this.trackers.get(key);
	}

	@Override
	public int addTracker(BlockTracker<T> tracker) {
		this.trackers.add(tracker);
		return this.trackers.size() - 1;
	}
}
