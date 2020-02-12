package net.devtech.asyncore.world.chunk;

/**
 * this is something you can attach to data chunks to listen in on puts and removes, ticking and local events are implement in this way, a block tracker
 * <b>must</b> have a persistent, because they are serialized alongside the chunk (unless they aren't, but better safe than sorry :P) all of the coordinates
 * are relative to the origin of the chunk
 * @param <T> the datatyp
 * @see DataChunk#addTracker(BlockTracker)
 */
public interface BlockTracker<T> {
	/**
	 * called on chunk load, this is for any initialization you need to do with the chunk data
	 */
	void init(DataChunk<T> chunk);

	/**
	 * this is called when a new object is set in the location
	 */
	void set(int x, int y, int z, T object);

	/**
	 * this is called when an object is removed from a location
	 */
	void remove(int x, int y, int z, T object);
}
