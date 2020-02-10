package net.devtech.asyncore.world.chunk;

import java.util.function.Supplier;

public interface DataChunk<T> {
	T get(int x, int y, int z);
	T getAndSet(int x, int y, int z, T _new);
	T getAndRemove(int x, int y, int z);
	boolean setOrAbort(int x, int y, int z, Supplier<T> object);
	boolean isEmpty();
	boolean loaded();
	void setLoaded(boolean loaded);
	int addTracker(BlockTracker<T> tracker);
	BlockTracker<T> getTracker(int key);
}
