package net.devtech.asyncore.events;

import org.bukkit.Location;

public class LocationEvent {
	private Location location;

	public LocationEvent(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return this.location;
	}
}
