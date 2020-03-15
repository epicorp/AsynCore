package net.devtech.asyncore.blocks.handlers;

import net.devtech.asyncore.blocks.events.ExplodeEvent;
import net.devtech.asyncore.world.server.ServerAccess;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionHandler implements Listener {
	protected final ServerAccess<Object> access;

	public ExplosionHandler(ServerAccess<Object> access) {this.access = access;}

	@EventHandler
	public void blockExplode(BlockExplodeEvent event) {
		for (Block block : event.blockList()) {
			Location location = block.getLocation();
			this.access.invoke(location, new ExplodeEvent(location));
		}
	}

	@EventHandler
	public void entityExplode(EntityExplodeEvent event) {
		for (Block block : event.blockList()) {
			Location location = block.getLocation();
			this.access.invoke(location, new ExplodeEvent(location));
		}
	}
}
