package net.devtech.asyncore.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.ref.WorldRef;
import net.devtech.asyncore.threading.GenericLock;
import net.devtech.asyncore.threading.PointLock;
import net.devtech.yajslib.io.PersistentInputStream;
import net.devtech.yajslib.io.PersistentOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.bukkit.World;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldContainer {
	private final PointLock chunkLock = new PointLock();
	private final GenericLock worldLock = new GenericLock();
	private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();
	private static final Logger LOGGER = Logger.getLogger("WorldContainer");
	private static final String FILE_PATTERN = "%d-%d.dat";
	private final WorldRef world;
	private final File worldDir;

	public WorldContainer(File worldsDir, World world) {
		this.world = new WorldRef(world);
		this.worldDir = new File(worldsDir, world.getName());
	}

	public void loadChunk(final int x, final int z) {
		// TODO for when chunk have been loaded for the first time
		this.chunkLock.waitFor(x, z, () -> {
			File chunkFile = new File(this.worldDir, String.format(FILE_PATTERN, x, z));
			try (PersistentInputStream input = new PersistentInputStream(new LZMACompressorInputStream(new FileInputStream(chunkFile)), AsynCore.PERSISTENT_REGISTRY)) {
				Object chunk = input.readPersistent();
				if (!(chunk instanceof Chunk)) throw new IOException(chunk + " ohno");
				this.worldLock.wait(() -> this.chunks.put((long) x << 32 | z & 0xFFFFFFFFL, (Chunk) chunk));
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Chunk corruption in world: " + this.world.get().getName() + " at chunk " + x + ", " + z, e);
				e.printStackTrace();
			}
		});
	}

	public void unloadChunk(final int x, final int z) {
		// TODO delete files with empty data
		this.chunkLock.waitFor(x, z, () -> {
			File chunkFile = new File(this.worldDir, String.format(FILE_PATTERN, x, z));
			try (PersistentOutputStream output = new PersistentOutputStream(new LZMACompressorOutputStream(new FileOutputStream(chunkFile)), AsynCore.PERSISTENT_REGISTRY)) {
				Chunk chunk = this.chunks.get((long) x << 32 | z & 0xFFFFFFFFL);
				chunk.isLoaded = false;
				output.writePersistent(chunk); // this may need a world lock but I don't think it does
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Chunk write failure in world: " + this.world.get().getName() + " at chunk " + x + ", " + z, e);
				e.printStackTrace();
			}
		});
	}

	public void tick() {
		this.worldLock.waitFor();
		this.worldLock.lock();
		for (Long2ObjectMap.Entry<Chunk> entry : Long2ObjectMaps.fastIterable(this.chunks)) {
			Chunk chunk = entry.getValue();
			long key = entry.getLongKey();
			int x = (int) (key >> 32);
			int z = (int) key;
			// chunk ticking must be forced main thread
			this.chunkLock.waitAndLock(x, z);
			if(chunk.isLoaded) // prevent reloading unloaded chunks
				chunk.tick(x, z);
			this.chunkLock.unlock(x, z);
		}
		this.worldLock.unlock();
	}
}
