package net.devtech.asyncore.blocks.handlers;

import net.devtech.asyncore.blocks.events.DestroyEvent;
import net.devtech.asyncore.blocks.events.PistonEvent;
import net.devtech.asyncore.world.server.ServerAccess;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import java.util.List;

/**
 * a handler class for dealing with pistons and custom blocks
 */
@SuppressWarnings ({"rawtypes", "unchecked"})
public class PistonHandler implements Listener {
	protected final ServerAccess access;

	public PistonHandler(ServerAccess access) {
		this.access = access;
	}
	@EventHandler
	public void pull(BlockPistonRetractEvent event) {
		this.piston(event);
	}
	@EventHandler
	public void push(BlockPistonExtendEvent event) {
		this.piston(event);
	}
	public void piston(BlockPistonEvent event) {
		List<Block> blocks = event instanceof BlockPistonExtendEvent ? ((BlockPistonExtendEvent) event).getBlocks() : ((BlockPistonRetractEvent)event).getBlocks();
		for (Block block : blocks) {
			PistonEvent piston = new PistonEvent(block.getLocation(), event);
			this.access.invoke(piston);
			this.move(piston);
		}
	}

	public void move(PistonEvent event) {
		Location location = event.getLocation();
		if (event.isDestroyed()) {
			// call real break method
			this.access.invoke(new DestroyEvent(location));
		} else {
			// just move the block
			BlockFace face = event.getDirection();
			this.access.queueRemove(location); // remove old
			Location newLocation = location.clone().add(face.getModX(), face.getModY(), face.getModZ());
			this.access.queueSet(newLocation, this.access.get(location)); // add new
		}
	}
}
