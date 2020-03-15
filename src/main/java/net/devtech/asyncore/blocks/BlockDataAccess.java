package net.devtech.asyncore.blocks;

import net.devtech.asyncore.world.server.ServerAccess;
import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.Location;

/**
 * an interface that blocks should implement to give access for certain utility interfaces
 */
public interface BlockDataAccess {
	ServerAccess<Object> getAccess();
	PersistentRegistry getRegistry();
	Location getLocation();
	boolean isInvalid();
	void invalidate();
}
