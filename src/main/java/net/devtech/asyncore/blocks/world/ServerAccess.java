package net.devtech.asyncore.blocks.world;

import org.bukkit.World;
import java.util.function.Supplier;

public interface ServerAccess {
	/**
	 * update the block/object at the location, block must be {@link net.devtech.asyncore.blocks.core.Updateable}
	 * @return true if the block at the location was Updateable
	 */
	boolean update(World world, int x, int y, int z);

	/**
	 * get the object at the location and set the new one in it's place
	 * @return null if there was no object there before
	 */
	Object getAndPlace(World world, int x, int y, int z, Object object);

	/**
	 * removes the object at the given location and returns it
	 */
	Object remove(World world, int x, int y, int z);


	/**
	 * only sets the object at the location if there is no other object already there
	 * @return true if the object was placed (there was no object there already)
	 */
	boolean setIfVacant(World world, int x, int y, int z, Supplier<Object> objectSupplier);
}

