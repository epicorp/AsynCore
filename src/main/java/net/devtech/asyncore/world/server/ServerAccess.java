package net.devtech.asyncore.world.server;

import net.devtech.asyncore.blocks.events.LocatedEvent;
import net.devtech.asyncore.blocks.events.PlaceEvent;
import net.devtech.asyncore.world.chunk.DataChunk;
import net.devtech.utilib.functions.QuadConsumer;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.function.Supplier;

/**
 * this class provides access to the custom world object system, you can set and destroy objects and get chunks
 */
public interface ServerAccess<T> {

	default void invoke(LocatedEvent event) {
		this.invoke(event.getLocation(), event);
	}

	default void invoke(Location location, Object event) {
		this.invoke(event, location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * add the object to the world if there is an open spot,
	 * and then invoke a place event on it
	 */
	default boolean addIfVacant(Location location, Supplier<T> object) {
		boolean set = this.setIfVacant(location, object);
		if(set) this.invoke(location, new PlaceEvent(location));
		return set;
	}

	/**
	 * add the object to the world and invoke a place event on it
	 */
	default T add(Location location, T object) {
		T set = this.getAndSet(location, object);
		this.invoke(location, new PlaceEvent(location));
		return set;
	}

	void invoke(Object event, World world, int x, int y, int z);

	/**
	 * queues a removal for a block, this should be executed at the end of a server access tick
	 * @implNote use {@link ServerAccess#remove(World, int, int, int)} or {@link ServerAccess#remove(Location)}
	 */
	void queueRemove(Location location);

	/**
	 * queues a set for a block, this should be executed at the end of a server access tick
	 * @implNote use {@link ServerAccess#getAndSet(Location, Object)} or {@link ServerAccess#getAndSet(World, int, int, int, Object)}
	 */
	void queueSet(Location location, T object);

	void tick();

	/**
	 * @see #getAndSet(World, int, int, int, T)
	 */
	default T getAndSet(Location location, T object) {
		return this.getAndSet(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), object);
	}

	/**
	 * get the object at the location and set the new one in it's place
	 * this will <b>NOT</b> handle events automatically for you, in
	 * if you want to call a blocks break and notify that it is being broken, call
	 * invoke, and make sure to remove the instance inside the destroy event
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

