package net.devtech.asyncore.events;

import org.bukkit.Location;

public class UpdateEvent extends LocationEvent {
	public UpdateEvent(Location location) {
		super(location);
	}
}
