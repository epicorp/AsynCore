package net.devtech.asyncore.world.server;

import net.devtech.asyncore.AsynCoreConfig;
import net.devtech.asyncore.util.ref.WorldRef;
import net.devtech.asyncore.world.WorldContainer;
import net.devtech.asyncore.world.chunk.DataChunk;
import net.devtech.utilib.duples.Pair;
import net.devtech.utilib.functions.QuadConsumer;
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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * this is the standard server manager, the default implementation of {@link ServerAccess}
 * @param <T>
 */
public abstract class ServerManager<T> implements Listener, ServerAccess<T> {
	private final ExecutorService service = Executors.newFixedThreadPool(AsynCoreConfig.threads);
	private Map<WorldRef, WorldContainer<T>> worlds = new ConcurrentHashMap<>();
	private final File worldDir;
	private final Function<WorldRef, DataChunk<T>>  dataChunkFunction;
	private final T _null;
	public ServerManager(File worldDir, Function<WorldRef, DataChunk<T>> function, T _null) {
		this.worldDir = worldDir;
		this.dataChunkFunction = function;
		this._null = _null;
	}

	public void init() {
		for (World world : Bukkit.getWorlds()) {
			this.onWorldLoad(new WorldLoadEvent(world));
		}
	}

	public void shutdown() {
		this.worlds.forEach((w, c) -> this.onWorldUnload(new WorldUnloadEvent(w.get())));
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
		WorldContainer<T> container = this.worlds.computeIfAbsent(new WorldRef(world), r -> new WorldContainer<>(this.worldDir, r.get(), this._null, this.dataChunkFunction));
		for (Chunk chunk : world.getLoadedChunks()) {
			container.loadChunk(chunk.getX(), chunk.getZ());
		}
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent event) {
		World world = event.getWorld();
		WorldContainer<T> container = this.worlds.get(new WorldRef(world));
		if (container != null) {
			for (Chunk chunk : world.getLoadedChunks()) {
				container.unloadChunk(chunk.getX(), chunk.getZ());
			}
		}
	}

	private Queue<Location> removals = new LinkedList<>();

	@Override
	public void queueRemove(Location location) {
		this.removals.add(location);
	}

	private Queue<Pair<Location, T>> sets = new LinkedList<>();

	@Override
	public void queueSet(Location location, T object) {
		this.sets.add(new Pair<>(location, object));
	}

	@Override
	public void tick() {
		while (!this.removals.isEmpty())
			this.remove(this.removals.poll());
		while (!this.sets.isEmpty()) {
			Pair<Location, T> set = this.sets.poll();
			this.getAndSet(set.getA(), set.getB());
		}
	}

	@Override
	public T getAndSet(World world, int x, int y, int z, T object) {
		return this.worlds.get(new WorldRef(world)).getAndSet(x, y, z, object);
	}

	@Override
	public T remove(World world, int x, int y, int z) {
		return this.worlds.get(new WorldRef(world)).remove(x, y, z);
	}

	@Override
	public boolean setIfVacant(World world, int x, int y, int z, Supplier<T> objectSupplier) {
		return this.worlds.get(new WorldRef(world)).setIfVacant(x, y, z, objectSupplier);
	}

	@Override
	public T get(World world, int x, int y, int z) {
		return this.worlds.get(new WorldRef(world)).get(x, y, z);
	}

	@Override
	public DataChunk<T> getChunk(World world, int cx, int cz) {
		return this.worlds.get(new WorldRef(world)).getChunk(cx, cz);
	}

	@Override
	public void forChunks(QuadConsumer<World, Integer, Integer, DataChunk<T>> chunk) {
		this.worlds.forEach((r, w) -> w.forChunks((x, z, c) -> chunk.accept(r.get(), x, z, c)));
	}

}
