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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * this class provides the utility methods and manages chunks inside a dimension
 * @param <T>
 */
public class WorldContainer<T> {
	/**
	 * the default empty chunk
	 */
	private final DataChunk<T> emptyChunk;
	/**
	 * this lock is for chunks, if the point is locked that means it is being operated on
	 */
	private final PointLock chunkLock = new PointLock();
	private final GenericLock worldLock = new GenericLock();
	private final Long2ObjectMap<DataChunk<T>> chunks = new Long2ObjectOpenHashMap<>();
	private static final Logger LOGGER = Logger.getLogger("WorldContainer");
	private static final String FILE_PATTERN = "%d,%d.dat";
	private final WorldRef world;
	private final File worldDir;
	private final Function<WorldRef, DataChunk<T>> function;

	public WorldContainer(File worldsDir, World world, T _null, Function<WorldRef, DataChunk<T>> function) {
		this.emptyChunk = new EmptyChunk<>(_null);
		this.world = new WorldRef(world);
		this.worldDir = new File(worldsDir, world.getName());
		this.function = function;
	}

	/**
	 * get the old object at the location and replace it
	 */
	public T getAndSet(int x, int y, int z, T object) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, this.emptyChunk).getAndSet(x, y, z, object);
	}

	/**
	 * get the object at the location
	 */
	public T get(int x, int y, int z) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, this.emptyChunk).get(x, y, z);
	}

	/**
	 * remove the object at the location
	 */
	public T remove(int x, int y, int z) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, this.emptyChunk).getAndRemove(x, y, z);
	}

	/**
	 * set the object at the location if and only if there is not already an object there
	 * @return true if the object was set successfully
	 */
	public boolean setIfVacant(int x, int y, int z, Supplier<T> objectSupplier) {
		int cx = x >> 4, cz = z >> 4;
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, this.emptyChunk).setOrAbort(x, y, z, objectSupplier);
	}

	/**
	 * get the chunk at the location
	 * @param cx the chunk's x coordinate
	 * @param cz the chunk's y coordinate
	 * @return the chunk at the location
	 */
	public DataChunk<T> getChunk(int cx, int cz) {
		return this.chunks.getOrDefault((long) cx << 32 | cz & 0xFFFFFFFFL, this.emptyChunk);
	}

	public void forChunks(TriConsumer<Integer, Integer, DataChunk<T>> forEach) {
		for (Long2ObjectMap.Entry<DataChunk<T>> entry : Long2ObjectMaps.fastIterable(this.chunks)) {
			long key = entry.getLongKey();
			forEach.accept((int) (key >> 32), (int) key, entry.getValue());
		}
	}


	/**
	 * serialize the chunk at the location to the disk
	 */
	public void unloadChunk(final int x, final int z) {
		this.chunkLock.waitFor(x, z, () -> {
			File chunkFile = new File(this.worldDir, String.format(FILE_PATTERN, x, z));
			DataChunk<T> chunk = this.chunks.remove((long) x << 32 | z & 0xFFFFFFFFL);
			if (chunk.isEmpty()) {
				if (chunkFile.exists() && !chunkFile.delete()) {
					LOGGER.severe("Unable to delete chunk file " + chunkFile);
				}
			} else {
				// TODO zstd compressor stream
				File parent = chunkFile.getParentFile();
				if(!parent.exists())
					parent.mkdirs();
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

	/**
	 * deserialize the chunk and load it into memory
	 */
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
				this.worldLock.wait(() -> this.chunks.put((long) x << 32 | z & 0xFFFFFFFFL, this.function.apply(this.world)));
			}
		});
	}
}
