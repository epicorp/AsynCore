package net.devtech.asyncore.blocks.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * an event that relates to a block in the world
 */
public class BlockEvent implements LocatedEvent {
	private final int x;
	private final int y;
	private final int z;
	private final World world;

	public BlockEvent(int x, int y, int z, World world) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
	}

	public BlockEvent(Location location) {
		this(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld());
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getZ() {
		return this.z;
	}

	public World getWorld() {
		return this.world;
	}

	@Override
	public Location getLocation() {
		return new Location(this.world, this.x, this.y, this.z);
	}

	public Block getBlock() {
		return this.world.getBlockAt(this.x, this.y, this.z);
	}

}
