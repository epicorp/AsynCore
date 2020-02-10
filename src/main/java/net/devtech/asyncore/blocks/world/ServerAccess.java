package net.devtech.asyncore.blocks.world;

import org.bukkit.Location;
import org.bukkit.World;
import java.util.function.Supplier;

public interface ServerAccess {
	/**
	 * @see #update(World, int, int, int)
	 */
	default void update(Location location) {
		this.update(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * update the block/object at the location
	 */
	void update(World world, int x, int y, int z);

	/**
	 * @see #getAndSet(World, int, int, int, Object)
	 */
	default Object getAndSet(Location location, Object object) {
		return this.getAndSet(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), object);
	}

	/**
	 * get the object at the location and set the new one in it's place
	 * @return null if there was no object there before
	 */
	Object getAndSet(World world, int x, int y, int z, Object object);

	/**
	 * @see #remove(World, int, int, int)
	 */
	default Object remove(Location location) {
		return this.remove(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * removes the object at the given location and returns it
	 */
	default Object remove(World world, int x, int y, int z) {
		return this.getAndSet(world, x, y, z, null);
	}

	/**
	 * @see #setIfVacant(World, int, int, int, Supplier)
	 */
	default boolean setIVacant(Location location, Supplier<Object> objectSupplier) {
		return this.setIfVacant(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), objectSupplier);
	}

	/**
	 * only sets the object at the location if there is no other object already there
	 * @return true if the object was placed (there was no object there already)
	 */
	boolean setIfVacant(World world, int x, int y, int z, Supplier<Object> objectSupplier);

	/**
	 * @see #get(World, int, int, int)
	 */
	default Object get(Location location) {
		return this.get(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * gets the object at the location
	 * @return null if none was there
	 */
	Object get(World world, int x, int y, int z);
}

