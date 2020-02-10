package net.devtech.asyncore.events;

import org.bukkit.Location;

public class BreakEvent extends LocationEvent {
	public BreakEvent(Location location) {
		super(location);
	}
}
