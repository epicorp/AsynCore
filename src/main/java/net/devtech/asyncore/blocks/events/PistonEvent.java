package net.devtech.asyncore.blocks.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

/**
 * a per block piston event
 */
public class PistonEvent extends BlockEvent {
	private final BlockFace direction;
	private final boolean pull;
	private boolean destroy;

	public PistonEvent(Location location, BlockPistonEvent event) {
		this(location, event.getDirection(), event instanceof BlockPistonRetractEvent);
	}

	public PistonEvent(Location location, BlockFace direction, boolean pull) {
		super(location);
		this.direction = direction;
		this.pull = pull;
	}

	public PistonEvent(int x, int y, int z, World world, BlockPistonEvent event) {
		super(x, y, z, world);
		this.direction = event.getDirection();
		this.pull = event instanceof BlockPistonRetractEvent;
	}

	public BlockFace getDirection() {
		return this.direction;
	}

	public boolean isPull() {
		return this.pull;
	}

	public boolean isDestroyed() {
		return this.destroy;
	}

	public void setDestroyed(boolean processed) {
		this.destroy = processed;
	}
}
