package net.devtech.asyncore.blocks.world;

import net.devtech.asyncore.blocks.CustomBlock;
import net.devtech.asyncore.blocks.Tickable;
import net.devtech.asyncore.blocks.world.events.LocalEventManager;
import net.devtech.asyncore.world.server.ServerManager;
import org.bukkit.World;
import java.io.File;

public class CustomBlockServer extends ServerManager<CustomBlock> implements CustomServerAccess {
	public CustomBlockServer(File worldDir, CustomBlock _null) {
		super(worldDir, w -> {
			CustomBlockDataChunk chunk = new CustomBlockDataChunk(w);
			chunk.addTracker(new ChunkTicker());
			chunk.addTracker(new LocalEventManager());
			return chunk;
		}, _null);
	}

	@Override
	public void invoke(Object event, World world, int x, int y, int z) {
		((LocalEventManager)this.getChunk(world, x >> 4, z >> 4).getTracker(1)).invoke(x & 15, y, z & 15, event);
	}

	@Override
	public void tick() {
		this.forChunks((w, cx, cz, c) -> ((ChunkTicker)c.getTracker(0)).forEach(((rx, y, rz) -> ((Tickable)c.get(rx, y, rz)).tick(w, cx*16+rx, y, cz*16+rz))));
	}

}
