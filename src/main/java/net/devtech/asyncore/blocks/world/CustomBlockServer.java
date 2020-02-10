package net.devtech.asyncore.blocks.world;

import net.devtech.asyncore.blocks.events.LocalEventManager;
import net.devtech.asyncore.world.server.ServerManager;
import org.bukkit.World;
import java.io.File;

public class CustomBlockServer extends ServerManager<Object> implements CustomServerAccess {
	public CustomBlockServer(File worldDir, Object _null) {
		super(worldDir, () -> {
			CustomBlockDataChunk chunk = new CustomBlockDataChunk();
			chunk.addTracker(new ChunkTicker());
			chunk.addTracker(new LocalEventManager());
			return chunk;
		}, _null);
	}

	@Override
	public void invoke(Object event, World world, int x, int y, int z) {

	}

	@Override
	public void tick() {

	}

}
