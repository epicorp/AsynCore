package net.devtech.asyncore.blocks.world;

import net.devtech.asyncore.AsynCoreConfig;
import net.devtech.asyncore.events.BreakEvent;
import net.devtech.asyncore.events.PlaceEvent;
import net.devtech.asyncore.events.UpdateEvent;
import net.devtech.asyncore.util.ref.WorldRef;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
	public void update(World world, int x, int y, int z) {
		WorldRef ref = new WorldRef(world);
		DataChunk object = this.worlds.get(ref).getChunk(x >> 4, z >> 4);
		object.handleEvent(new UpdateEvent(new Location(world, x, y, z)), x, y, z);
	}

	@Override
	public Object getAndSet(World world, int x, int y, int z, Object object) {
		WorldRef ref = new WorldRef(world);
		DataChunk chunk = this.worlds.get(ref).getChunk(x >> 4, z >> 4);
		chunk.handleEvent(new BreakEvent(new Location(world, x, y, z)), x, y, z);
		Object old = this.worlds.get(ref).getAndSet(x, y, z, object);
		chunk.handleEvent(new PlaceEvent(new Location(world, x, y, z)), x, y, z);
		return old;
	}

	@Override
	public boolean setIfVacant(World world, int x, int y, int z, Supplier<Object> objectSupplier) {
		WorldRef ref = new WorldRef(world);
		return this.worlds.get(ref).setIfVacant(x, y, z, () -> {
			Object get = objectSupplier.get();
			DataChunk chunk = this.worlds.get(ref).getChunk(x >> 4, z >> 4);
			chunk.handleEvent(new PlaceEvent(new Location(world, x, y, z)), x, y, z);
			return get;
		});
	}

	@Override
	public Object get(World world, int x, int y, int z) {
		return this.worlds.get(new WorldRef(world)).get(x, y, z);
	}

}
