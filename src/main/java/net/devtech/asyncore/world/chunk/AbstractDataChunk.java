package net.devtech.asyncore.world.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * must implement your own serializer, and it must serialize the trackers as well!
 *
 * @param <T> the data type
 */
public abstract class AbstractDataChunk<T> implements DataChunk<T> {
	private static final Object DUMMY_OBJECT = new Object();
	protected final List<BlockTracker<T>> trackers = new ArrayList<>();
	protected boolean isLoaded = true;

	/**
	 * set the object at the location and return the old one
	 */
	protected abstract T getAndReplace(T _new, int x, int y, int z);

	/**
	 * remove the object at the location and return the old one
	 */
	protected abstract T remove(int x, int y, int z);

	/**
	 * set the object if any only if there is not already an object there
	 */
	protected abstract T computeIfAbsent(Supplier<T> obj, int x, int y, int z);

	@Override
	public T getAndSet(int x, int y, int z, T _new) {
		T old = this.updateTrackersRemove(this.getAndReplace(_new, x, y, z), x, y, z);
		this.updateTrackersSet(_new, x, y, z);
		return old;
	}

	@Override
	public T getAndRemove(int x, int y, int z) {
		return this.updateTrackersRemove(this.remove(x, y, z), x, y, z);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public boolean setOrAbort(int x, int y, int z, Supplier<T> object) {
		Object[] ref = {DUMMY_OBJECT};
		T returned = this.computeIfAbsent(() -> (T) (ref[0] = object.get()), x, y, z);
		boolean set = ref[0] == returned;
		if (set) this.updateTrackersSet(returned, x, y, z);
		return set; // hacc
	}

	private T updateTrackersRemove(T object, int x, int y, int z) {
		if (object == null) return null;
		x &= 15;
		z &= 15;
		for (BlockTracker<T> tracker : this.trackers) {
			tracker.remove(object, x, y, z);
		}
		return object;
	}

	private void updateTrackersSet(T object, int x, int y, int z) {
		if (object == null) return;
		for (BlockTracker<T> tracker : this.trackers) {
			tracker.set(object, x, y, z);
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
		tracker.init(this);
		return this.trackers.size() - 1;
	}
}
