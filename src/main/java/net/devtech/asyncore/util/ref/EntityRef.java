package net.devtech.asyncore.util.ref;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import java.util.UUID;

public class EntityRef extends BukkitRef<Entity, UUID> {
	public EntityRef(Entity object) {
		super(object);
	}

	@Override
	protected Entity from(UUID internal) {
		return Bukkit.getEntity(internal);
	}

	@Override
	protected UUID to(Entity object) {
		return object.getUniqueId();
	}
}
