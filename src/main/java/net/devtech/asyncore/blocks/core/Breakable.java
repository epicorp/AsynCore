package net.devtech.asyncore.blocks.core;

import org.bukkit.World;

public interface Breakable {
	void destroy(World world, int x, int y, int z);
}
