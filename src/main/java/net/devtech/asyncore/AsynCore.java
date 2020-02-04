package net.devtech.asyncore;

import net.devtech.asyncore.blocks.world.ServerManager;
import net.devtech.asyncore.blocks.world.Chunk;
import net.devtech.yajslib.persistent.AnnotatedPersistent;
import net.devtech.yajslib.persistent.PersistentRegistry;
import net.devtech.yajslib.persistent.SimplePersistentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class AsynCore extends JavaPlugin implements Listener {
	public static final PersistentRegistry PERSISTENT_REGISTRY = new SimplePersistentRegistry();
	static {
		PERSISTENT_REGISTRY.register(Chunk.class, new AnnotatedPersistent<>(Chunk.class, 9072059811478052715L));
	}

	private ServerManager manager;

	@Override
	public void onEnable() {
		// Plugin startup logic
		// TODO config for file location
		// TODO add config for threads
		File file = new File(this.getDataFolder(), "server-data");
		this.manager = new ServerManager(file);
		Bukkit.getPluginManager().registerEvents(this.manager, this);
		this.getLogger().info("AsynCore has been loaded!");
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
		this.manager.shutdown();
	}
}
