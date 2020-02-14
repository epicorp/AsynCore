package net.devtech.asyncore.world.server;

import net.devtech.asyncore.world.chunk.DataChunk;
import net.devtech.utilib.functions.QuadConsumer;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.function.Supplier;

/**
 * this class provides access to the custom world object system, you can set and destroy objects and get chunks
 */
public interface ServerAccess<T> {

	/**
	 * @see #getAndSet(World, int, int, int, T)
	 */
	default T getAndSet(Location location, T object) {
		return this.getAndSet(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), object);
	}

	/**
	 * get the object at the location and set the new one in it's place
	 *
	 * @return null if there was no object there before
	 */
	T getAndSet(World world, int x, int y, int z, T object);

	/**
	 * @see #remove(World, int, int, int)
	 */
	default T remove(Location location) {
		return this.remove(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * remove the object at the given location and returns it
	 */
	T remove(World world, int x, int y, int z);

	/**
	 * @see #setIfVacant(World, int, int, int, Supplier)
	 */
	default boolean setIfVacant(Location location, Supplier<T> objectSupplier) {
		return this.setIfVacant(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), objectSupplier);
	}

	/**
	 * only sets the object at the location if there is no other object already there
	 *
	 * @return true if the object was placed (there was no object there already)
	 */
	boolean setIfVacant(World world, int x, int y, int z, Supplier<T> objectSupplier);

	/**
	 * @see #get(World, int, int, int)
	 */
	default T get(Location location) {
		return this.get(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * gets the object at the location
	 *
	 * @return null if none was there
	 */
	T get(World world, int x, int y, int z);

	/**
	 * get the chunk at the given location
	 *
	 * @param world the world the chunk is in
	 * @param cx the chunk's x coordinate
	 * @param cz the chunk's z coordinate
	 */
	DataChunk<T> getChunk(World world, int cx, int cz);

	/**
	 * iterate through all the chunks
	 * the consumer accepts the
	 *  world in which the chunk lies
	 *  it's x coordinate
	 *  it's z coordinate
	 *  and the chunk itself
	 */
	void forChunks(QuadConsumer<World, Integer, Integer, DataChunk<T>> chunk);
}

