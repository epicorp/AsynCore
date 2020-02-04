package net.devtech.asyncore.blocks.core;

import org.bukkit.World;

public interface Updateable {
	void update(World world, int x, int y, int z);
}
