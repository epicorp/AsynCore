package net.devtech.asyncore;

import net.devtech.asyncore.world.Chunk;
import net.devtech.yajslib.persistent.AnnotatedPersistent;
import net.devtech.yajslib.persistent.PersistentRegistry;
import net.devtech.yajslib.persistent.SimplePersistentRegistry;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class AsynCore extends JavaPlugin implements Listener {
	public static final PersistentRegistry PERSISTENT_REGISTRY = new SimplePersistentRegistry();
	static {
		PERSISTENT_REGISTRY.register(Chunk.class, new AnnotatedPersistent<>(Chunk.class, 9072059811478052715L));
	}

	@Override
	public void onEnable() {
		// Plugin startup logic
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
