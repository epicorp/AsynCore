package net.devtech.asyncore.blocks.world;

import net.devtech.asyncore.blocks.events.TickEvent;
import net.devtech.asyncore.blocks.world.events.LocalEventManager;
import net.devtech.asyncore.world.server.ServerManager;
import org.bukkit.World;
import java.io.File;

/**
 * the default server manager for custom blocks
 */
public class CustomBlockServer extends ServerManager<Object> {
	public CustomBlockServer(File worldDir, Object _null) {
		super(worldDir, CustomBlockDataChunk::new, _null);
	}

	@Override
	public void invoke(Object event, World world, int x, int y, int z) {
		((LocalEventManager)this.getChunk(world, x >> 4, z >> 4).getTracker(0)).invoke(x & 15, y, z & 15, event);
	}

	@Override
	public void tick() {
		super.tick();
		this.forChunks((w, cx, cz, c) -> ((LocalEventManager)c.getTracker(0)).invokeAll((rx, y, rz) -> new TickEvent(cx*16+rx, y, cz*16+rz, w)));
	}
}
