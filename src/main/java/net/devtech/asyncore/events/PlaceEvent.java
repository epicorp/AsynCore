package net.devtech.asyncore.events;

import org.bukkit.Location;

public class PlaceEvent extends LocationEvent {
	public PlaceEvent(Location location) {
		super(location);
	}
}
