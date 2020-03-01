package net.devtech.asyncore.world.chunk;

import org.bukkit.Bukkit;
import org.bukkit.World;
import java.util.function.Supplier;

/**
 * a null chunk
 */
public class EmptyChunk<T> implements DataChunk<T> {
	private final T _null;
	public EmptyChunk(T _null) {
		this._null = _null;
	}

	@Override
	public T get(int x, int y, int z) {
		return this._null;
	}

	@Override
	public T getAndSet(int x, int y, int z, T _new) {
		return this._null;
	}

	@Override
	public T getAndRemove(int x, int y, int z) {
		return this._null;
	}

	@Override
	public boolean setOrAbort(int x, int y, int z, Supplier<T> object) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean loaded() {
		return false;
	}

	@Override
	public void setLoaded(boolean loaded) {}

	@Override
	public int addTracker(BlockTracker<T> tracker) {
		return 0;
	}

	@Override
	public BlockTracker<T> getTracker(int key) {
		return null;
	}

	@Override
	public World getWorld() {
		return Bukkit.getWorlds().get(0);
	}
}
