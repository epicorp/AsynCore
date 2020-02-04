package net.devtech.asyncore.blocks.world;

import net.devtech.asyncore.AsynCoreConfig;
import net.devtech.asyncore.blocks.core.Breakable;
import net.devtech.asyncore.blocks.core.Placeable;
import net.devtech.asyncore.blocks.core.Updateable;
import net.devtech.asyncore.util.ref.WorldRef;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ServerManager implements Listener, ServerAccess {
	private final ExecutorService service = Executors.newFixedThreadPool(AsynCoreConfig.threads);
	private Map<WorldRef, WorldContainer> worlds = new ConcurrentHashMap<>();
	private final File worldDir;

	public ServerManager(File worldDir) {
		this.worldDir = worldDir;
		for (World world : Bukkit.getWorlds()) {
			this.onWorldLoad(new WorldLoadEvent(world));
		}
	}

	public void shutdown() {
		this.worlds.forEach((w, c) -> this.onWorldUnload(new WorldUnloadEvent(w.get())));
	}

	public void tick() {
		this.worlds.forEach((w, c) -> c.tick());
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		this.service.submit(() -> {
			WorldRef ref = new WorldRef(event.getWorld());
			Chunk chunk = event.getChunk();
			this.worlds.get(ref).loadChunk(chunk.getX(), chunk.getZ());
		});
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		this.service.submit(() -> {
			WorldRef ref = new WorldRef(event.getWorld());
			Chunk chunk = event.getChunk();
			this.worlds.get(ref).unloadChunk(chunk.getX(), chunk.getZ());
		});
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		World world = event.getWorld();
		WorldContainer container = this.worlds.computeIfAbsent(new WorldRef(world), r -> new WorldContainer(this.worldDir, r.get()));
		for (Chunk chunk : world.getLoadedChunks()) {
			container.loadChunk(chunk.getX(), chunk.getZ());
		}
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent event) {
		World world = event.getWorld();
		WorldContainer container = this.worlds.get(new WorldRef(world));
		if (container != null) {
			for (Chunk chunk : world.getLoadedChunks()) {
				container.unloadChunk(chunk.getX(), chunk.getZ());
			}
		}
	}

	@Override
	public boolean update(World world, int x, int y, int z) {
		WorldRef ref = new WorldRef(world);
		Object object = this.worlds.get(ref).get(x, y, z);
		if (object instanceof Updateable) {
			((Updateable) object).update(world, x, y, z);
			return true;
		}
		return false;
	}

	@Override
	public Object getAndPlace(World world, int x, int y, int z, Object object) {
		WorldRef ref = new WorldRef(world);
		if (object instanceof Placeable) {
			((Placeable) object).place(world, x, y, z);
		}

		Object old = this.worlds.get(ref).getAndSet(x, y, z, object);
		if(old instanceof Breakable)
			((Breakable) old).destroy(world, x, y, z);
		return old;
	}

	@Override
	public Object remove(World world, int x, int y, int z) {
		return this.getAndPlace(world, x, y, z, null);
	}

	@Override
	public boolean setIfVacant(World world, int x, int y, int z, Supplier<Object> objectSupplier) {
		return this.worlds.get(new WorldRef(world)).setIfVacant(x, y, z, () -> {
			Object get = objectSupplier.get();
			if(get instanceof Placeable)
				((Placeable) get).place(world, x, y, z);
			return get;
		});
	}

}
