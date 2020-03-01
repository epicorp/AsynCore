package net.devtech.asyncore.util.persistents;

import net.devtech.yajslib.annotations.DependsOn;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import net.devtech.yajslib.persistent.Persistent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@DependsOn(Map.class)
public class BukkitPersistent<B extends ConfigurationSerializable> implements Persistent<B> {
	private final long versionHash;
	private final Function<Map<String, Object>, B> deserializer;

	public BukkitPersistent(long versionHash, Function<Map<String, Object>, B> deserializer) {
		this.versionHash = versionHash;
		this.deserializer = deserializer;
	}

	@Override
	public long versionHash() {
		return this.versionHash;
	}

	@Override
	public void write(B b, PersistentOutput output) throws IOException {
		output.writePersistent(b.serialize(), true); // maps should search for super classes
	}

	@Override
	public B read(PersistentInput input) throws IOException {
		Map<String, Object> data = (Map<String, Object>) input.readPersistent();
		return this.deserializer.apply(data);
	}

	@Override
	public B blank() {
		return this.deserializer.apply(new HashMap<>());
	}
}
