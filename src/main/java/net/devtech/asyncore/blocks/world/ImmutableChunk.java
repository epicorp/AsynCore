package net.devtech.asyncore.blocks.world;

import org.bukkit.World;
import java.util.function.Supplier;

public class ImmutableChunk extends DataChunk {
	public ImmutableChunk() {
	}

	public ImmutableChunk(World world) {
		super(world);
	}

	@Override
	public <T> T getAndRemove(int x, int y, int z) {
		return super.get(x, y, z);
	}

	@Override
	public <T> T getAndSet(int x, int y, int z, Object _new) {
		return super.get(x, y, z);
	}

	@Override
	public boolean setOrAbort(int x, int y, int z, Supplier<Object> object) {
		return false;
	}

	@Override
	public void tick(int cx, int cz) {
		// no
	}

	@Override
	public void randTick(int cx, int cz, int ticks) {
		// no
	}
}
