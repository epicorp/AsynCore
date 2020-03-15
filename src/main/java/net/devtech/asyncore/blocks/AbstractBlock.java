package net.devtech.asyncore.blocks;

import net.devtech.asyncore.blocks.events.PistonEvent;
import net.devtech.asyncore.blocks.events.PlaceEvent;
import net.devtech.asyncore.blocks.world.events.LocalEvent;
import net.devtech.asyncore.world.server.ServerAccess;
import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * an abstract class for implementing the block data access classes
 */
public abstract class AbstractBlock implements BlockDataAccess {
	/**
	 * must be serialized!
	 */
	private final Location internal = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
	private final ServerAccess<Object> access;
	private final PersistentRegistry registry;
	private boolean valid = true;

	@Override
	public boolean isInvalid() {
		return !this.valid;
	}

	@Override
	public void invalidate() {
		this.valid = false;
	}

	public AbstractBlock(PersistentRegistry registry, ServerAccess<Object> access) {
		this.access = access;
		this.registry = registry;
	}

	@LocalEvent
	private void place(PlaceEvent event) {
		this.valid = true;
		this.setLocation(event.getLocation());
	}

	@LocalEvent
	private void piston(PistonEvent event) {
		Location location = event.toLocation();
		this.setLocation(location);
	}

	@Override
	public Location getLocation() {
		return this.internal.clone();
	}

	public void setLocation(Location location) {
		this.internal.setWorld(location.getWorld());
		this.internal.setY(location.getY());
		this.internal.setX(location.getX());
		this.internal.setZ(location.getZ());
	}

	@Override
	public ServerAccess<Object> getAccess() {
		return this.access;
	}

	@Override
	public PersistentRegistry getRegistry() {
		return this.registry;
	}
}
