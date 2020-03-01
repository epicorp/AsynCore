package net.devtech.asyncore.blocks.events;

import org.bukkit.Location;

/**
 * an event that is marked by a specific location
 */
public interface LocatedEvent {
	Location getLocation();
}
