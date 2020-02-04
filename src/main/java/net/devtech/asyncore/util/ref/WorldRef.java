package net.devtech.asyncore.util.ref;

import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentInputStream;
import net.devtech.yajslib.io.PersistentOutput;
import net.devtech.yajslib.io.PersistentOutputStream;
import net.devtech.yajslib.persistent.Persistent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import java.io.IOException;
import java.util.UUID;

public class WorldRef extends BukkitRef<World, UUID> {
	public WorldRef(World object) {
		super(object);
	}

	public WorldRef(UUID object, boolean conflictingParam) {
		super(object, conflictingParam);
	}

	@Override
	protected World from(UUID internal) {
		return Bukkit.getWorld(internal);
	}

	@Override
	protected UUID to(World object) {
		return object.getUID();
	}


	public static class WorldRefPersistent implements Persistent<WorldRef> {

		@Override
		public long versionHash() {
			return 2345678654321L;
		}

		@Override
		public void write(WorldRef ref, PersistentOutput output) throws IOException {
			output.writeUUID(ref.internal);
		}

		@Override
		public WorldRef read(PersistentInput input) throws IOException {
			return new WorldRef(input.readUUID(), false);
		}

		@Override
		public WorldRef blank() {
			return new WorldRef(Bukkit.getWorlds().get(0));
		}
	}
}
