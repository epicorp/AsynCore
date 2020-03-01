package net.devtech.asyncore.util.ref;

import net.devtech.yajslib.annotations.DependsOn;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentInputStream;
import net.devtech.yajslib.io.PersistentOutput;
import net.devtech.yajslib.io.PersistentOutputStream;
import net.devtech.yajslib.persistent.Persistent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import java.io.IOException;
import java.util.UUID;


public class WorldRef extends BukkitRef<World, String> {
	public WorldRef(World object) {
		super(object);
	}

	public WorldRef(String object, boolean conflictingParam) {
		super(object, conflictingParam);
	}

	@Override
	protected World from(String internal) {
		return Bukkit.getWorld(internal);
	}

	@Override
	protected String to(World object) {
		return object.getName();
	}

	@DependsOn(String.class)
	public static class WorldRefPersistent implements Persistent<WorldRef> {

		@Override
		public long versionHash() {
			return 2345678654321L;
		}

		@Override
		public void write(WorldRef ref, PersistentOutput output) throws IOException {
			output.writePersistent(ref.internal);
		}

		@Override
		public WorldRef read(PersistentInput input) throws IOException {
			return new WorldRef((String) input.readPersistent(), false);
		}

		@Override
		public WorldRef blank() {
			return new WorldRef(Bukkit.getWorlds().get(0));
		}
	}
}
