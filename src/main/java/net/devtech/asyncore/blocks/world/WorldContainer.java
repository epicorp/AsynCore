package net.devtech.asyncore.blocks.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.AsynCoreConfig;
import net.devtech.asyncore.util.ref.WorldRef;
import net.devtech.asyncore.util.threading.GenericLock;
import net.devtech.asyncore.util.threading.PointLock;
import net.devtech.yajslib.io.PersistentInputStream;
import net.devtech.yajslib.io.PersistentOutputStream;
import org.bukkit.World;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class WorldContainer {
	private static final DataChunk EMPTY_CHUNK = new ImmutableChunk();
	private final PointLock chunkLock = new PointLock();
	private final GenericLock worldLock = new GenericLock();
	private final Long2ObjectMap<DataChunk> chunks = new Long2ObjectOpenHashMap<>();
	private static final Logger LOGGER = Logger.getLogger("WorldContainer");
	private static final String FILE_PATTERN = "%d,%d.dat";
	private final WorldRef world;
	private final File worldDir;

	public WorldContainer(File worldsDir, World world) {
		this.world = new WorldRef(world);
		this.worldDir = new File(worldsDir, world.getName());
	}

	public void loadChunk(final int x, final int z) {
		this.chunkLock.waitFor(x, z, () -> {
			File chunkFile = new File(this.worldDir, String.format(FILE_PATTERN, x, z));
			if (chunkFile.exists()) {
				try (PersistentInputStream input = new PersistentInputStream(new GZIPInputStream(new FileInputStream(chunkFile)), AsynCore.PERSISTENT_REGISTRY)) {
					Object chunk = input.readPersistent();
					if (!(chunk instanceof DataChunk)) throw new IOException(chunk + " ohno");
					this.worldLock.wait(() -> this.chunks.put((long) x << 32 | z & 0xFFFFFFFFL, (DataChunk) chunk));
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Chunk corruption in world: " + this.world.get().getName() + " at chunk " + x + ", " + z, e);
					e.printStackTrace();
				}
			} else { // new chunk
				this.worldLock.wait(() -> this.chunks.put((long) x << 32 | z & 0xFFFFFFFFL, new DataChunk(this.world.get())));
			}
		});
	}

	public Object getAndSet(int x, int y, int z, Object object) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, EMPTY_CHUNK).getAndSet(x, y, z, object);
	}

	public Object get(int x, int y, int z) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, EMPTY_CHUNK).get(x, y, z);
	}

	public Object remove(int x, int y, int z) {
		return this.getAndSet(x, y, z, null);
	}

	public boolean setIfVacant(int x, int y, int z, Supplier<Object> objectSupplier) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, EMPTY_CHUNK).setOrAbort(x, y, z, objectSupplier);
	}

	/**
	 * chunk coords
	 */
	public DataChunk getChunk(int cx, int cz) {
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, EMPTY_CHUNK);
	}


	public void unloadChunk(final int x, final int z) {
		this.chunkLock.waitFor(x, z, () -> {
			File chunkFile = new File(this.worldDir, String.format(FILE_PATTERN, x, z));
			DataChunk chunk = this.chunks.remove((long) x << 32 | z & 0xFFFFFFFFL);
			if (chunk.data.isEmpty()) {
				if (!chunkFile.delete() && chunkFile.exists()) {
					LOGGER.severe("Unable to delete chunk file " + chunkFile);
				}
			} else {
				if (!chunkFile.exists()) {
					chunkFile.getParentFile().mkdirs();
				}
				// TODO zstd compressor stream
				try (PersistentOutputStream output = new PersistentOutputStream(new GZIPOutputStream(new FileOutputStream(chunkFile)), AsynCore.PERSISTENT_REGISTRY)) {
					chunk.isLoaded = false; // invalidate any old references
					output.writePersistent(chunk); // this may need a world lock but I don't think it does
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Chunk write failure in world: " + this.world.get().getName() + " at chunk " + x + ", " + z, e);
					e.printStackTrace();
				}
			}
		});
	}

	public void tick() {
		this.worldLock.waitFor();
		this.worldLock.lock();
		try {
			this.chunks.forEach((key, chunk) -> { // replace with fast for each
				int x = (int) (key >> 32);
				int z = key.intValue();
				// chunk ticking must be forced main thread
				this.chunkLock.waitAndLock(x, z);
				if (chunk.isLoaded) {// prevent reloading unloaded chunks
					chunk.tick(x, z);
					chunk.randTick(x, z, AsynCoreConfig.rand);
				}
				this.chunkLock.unlock(x, z);
			});
		} catch (Throwable throwable) {
			LOGGER.severe("Ticking error!");
			throwable.printStackTrace();
		}
		this.worldLock.unlock();
	}
}
