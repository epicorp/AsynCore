package net.devtech.asyncore.blocks.world;

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

public class ServerManager implements Listener {
	private final ExecutorService service = Executors.newFixedThreadPool(1); // can be increased if using SSD
	private Map<WorldRef, WorldContainer> worlds = new ConcurrentHashMap<>();
	private final File worldDir;
	public ServerManager(File worldDir) {
		this.worldDir = worldDir;
		for (World world : Bukkit.getWorlds()) {
			this.onWorldLoad(new WorldLoadEvent(world));
		}
	}

	/**
	 * @deprecated will add a proper set block method for this later
	 */
	@Deprecated
	public void set(World world, int x, int y, int z, Object object) {
		WorldRef ref = new WorldRef(world);
		this.worlds.get(ref).set(x, y, z, object);
	}

	public void set(Location location, Object object) {
		this.set(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), object);
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
		if(container != null) {
			for (Chunk chunk : world.getLoadedChunks()) {
				container.unloadChunk(chunk.getX(), chunk.getZ());
			}
		}
	}
}
