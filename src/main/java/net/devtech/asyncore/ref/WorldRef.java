package net.devtech.asyncore.ref;

import org.bukkit.Bukkit;
import org.bukkit.World;
import java.util.UUID;

public class WorldRef extends BukkitRef<World, UUID> {
	public WorldRef(World object) {
		super(object);
	}

	@Override
	protected World from(UUID internal) {
		return Bukkit.getWorld(internal);
	}

	@Override
	protected UUID to(World object) {
		return object.getUID();
	}
}
