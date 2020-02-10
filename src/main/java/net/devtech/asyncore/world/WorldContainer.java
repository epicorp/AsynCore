package net.devtech.asyncore.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.util.ref.WorldRef;
import net.devtech.asyncore.util.threading.GenericLock;
import net.devtech.asyncore.util.threading.PointLock;
import net.devtech.asyncore.world.chunk.DataChunk;
import net.devtech.asyncore.world.chunk.EmptyChunk;
import net.devtech.utilib.functions.TriConsumer;
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

public class WorldContainer<T> {
	private final DataChunk<T> emptyChunk;
	private final PointLock chunkLock = new PointLock();
	private final GenericLock worldLock = new GenericLock();
	private final Long2ObjectMap<DataChunk<T>> chunks = new Long2ObjectOpenHashMap<>();
	private static final Logger LOGGER = Logger.getLogger("WorldContainer");
	private static final String FILE_PATTERN = "%d,%d.dat";
	private final WorldRef world;
	private final File worldDir;
	private final Supplier<DataChunk<T>> supplier;

	public WorldContainer(File worldsDir, World world, T _null, Supplier<DataChunk<T>> supplier) {
		this.emptyChunk = new EmptyChunk<>(_null);
		this.world = new WorldRef(world);
		this.worldDir = new File(worldsDir, world.getName());
		this.supplier = supplier;
	}

	public void loadChunk(final int x, final int z) {
		this.chunkLock.waitFor(x, z, () -> {
			File chunkFile = new File(this.worldDir, String.format(FILE_PATTERN, x, z));
			if (chunkFile.exists()) {
				try (PersistentInputStream input = new PersistentInputStream(new GZIPInputStream(new FileInputStream(chunkFile)), AsynCore.PERSISTENT_REGISTRY)) {
					Object chunk = input.readPersistent();
					if (!(chunk instanceof DataChunk)) throw new IOException(chunk + " ohno");
					this.worldLock.wait(() -> this.chunks.put((long) x << 32 | z & 0xFFFFFFFFL, (DataChunk<T>) chunk));
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Chunk corruption in world: " + this.world.get().getName() + " at chunk " + x + ", " + z, e);
					e.printStackTrace();
				}
			} else { // new chunk
				this.worldLock.wait(() -> this.chunks.put((long) x << 32 | z & 0xFFFFFFFFL, this.supplier.get()));
			}
		});
	}

	public T getAndSet(int x, int y, int z, T object) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, this.emptyChunk).getAndSet(x & 15, y, z & 15, object);
	}

	public T get(int x, int y, int z) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, this.emptyChunk).get(x & 15, y, z & 15);
	}

	public T remove(int x, int y, int z) {
		return this.getAndSet(x & 15, y, z & 15, null);
	}

	public boolean setIfVacant(int x, int y, int z, Supplier<T> objectSupplier) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, this.emptyChunk).setOrAbort(x & 15, y, z & 15, objectSupplier);
	}

	/**
	 * chunk coords
	 */
	public DataChunk<T> getChunk(int cx, int cz) {
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, this.emptyChunk);
	}


	public void unloadChunk(final int x, final int z) {
		this.chunkLock.waitFor(x, z, () -> {
			File chunkFile = new File(this.worldDir, String.format(FILE_PATTERN, x, z));
			DataChunk<T> chunk = this.chunks.remove((long) x << 32 | z & 0xFFFFFFFFL);
			if (chunk.isEmpty()) {
				if (!chunkFile.delete() && chunkFile.exists()) {
					LOGGER.severe("Unable to delete chunk file " + chunkFile);
				}
			} else {
				if (!chunkFile.exists()) {
					if (!chunkFile.getParentFile().mkdirs()) {
						LOGGER.severe("oh god oh fuck critical failure with " + chunkFile);
						return;
					}
				}
				// TODO zstd compressor stream
				try (PersistentOutputStream output = new PersistentOutputStream(new GZIPOutputStream(new FileOutputStream(chunkFile)), AsynCore.PERSISTENT_REGISTRY)) {
					chunk.setLoaded(false); // invalidate any old references
					output.writePersistent(chunk); // this may need a world lock but I don't think it does
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Chunk write failure in world: " + this.world.get().getName() + " at chunk " + x + ", " + z, e);
					e.printStackTrace();
				}
			}
		});
	}

	public void forChunks(TriConsumer<Integer, Integer, DataChunk<T>> forEach) {
		for (Long2ObjectMap.Entry<DataChunk<T>> entry : Long2ObjectMaps.fastIterable(this.chunks)) {
			long key = entry.getLongKey();
			forEach.accept((int) (key >> 32), (int) key, entry.getValue());
		}
	}
}
