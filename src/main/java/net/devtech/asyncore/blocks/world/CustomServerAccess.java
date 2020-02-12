package net.devtech.asyncore.blocks.world;

import net.devtech.asyncore.blocks.CustomBlock;
import net.devtech.asyncore.world.server.ServerAccess;
import org.bukkit.Location;
import org.bukkit.World;

public interface CustomServerAccess extends ServerAccess<CustomBlock> {
	default void invoke(Location location, Object event) {
		this.invoke(event, location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	void invoke(Object event, World world, int x, int y, int z);
	void tick();
}
