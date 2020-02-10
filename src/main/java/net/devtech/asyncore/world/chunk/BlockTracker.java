package net.devtech.asyncore.world.chunk;

public interface BlockTracker<T> {
	void set(int x, int y, int z, T object);
	void remove(int x, int y, int z, T object);
}
