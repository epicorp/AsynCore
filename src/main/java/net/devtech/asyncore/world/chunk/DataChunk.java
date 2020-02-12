package net.devtech.asyncore.world.chunk;

import java.util.function.Supplier;

/**
 * A chunk in a minecraft world that stores some data
 * all coordinates are absolute! Not relative to the chunk's origin!
 */
public interface DataChunk<T> {
	/**
	 * get the block at the given location
	 * @return the object at the location, or null
	 */
	T get(int x, int y, int z);

	/**
	 * get the object at the given location, and replace it with a new one
	 * @param _new the object
	 * @return the old object at the location, or null
	 */
	T getAndSet(int x, int y, int z, T _new);

	/**
	 * gets the object at the given location and removes it from the chunk
	 * @return the old object, or null
	 */
	T getAndRemove(int x, int y, int z);

	/**
	 * set the object in the location, if any only if there is not already a block there
	 * @param object the supplier that provides the object if there is a block there
	 * @return true if the object was successfully placed
	 */
	boolean setOrAbort(int x, int y, int z, Supplier<T> object);

	/**
	 * @return true if there is no objects in the chunk
	 */
	boolean isEmpty();

	/**
	 * @return true if the chunk is loaded
	 */
	boolean loaded();

	/**
	 * sets if the chunk is loaded or not
	 */
	void setLoaded(boolean loaded);

	/**
	 * adds a block tracker to the chunk
	 * @param tracker the tracker
	 * @return the index the tracker was added to
	 */
	int addTracker(BlockTracker<T> tracker);

	/**
	 * gets the tracker at the given index
	 * @param key the index of the tracker
	 * @return the tracker
	 */
	BlockTracker<T> getTracker(int key);
}
