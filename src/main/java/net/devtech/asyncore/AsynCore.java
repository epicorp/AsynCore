package net.devtech.asyncore;

import net.devtech.asyncore.blocks.world.ServerManager;
import net.devtech.asyncore.blocks.world.Chunk;
import net.devtech.asyncore.commands.TestExecutor;
import net.devtech.asyncore.testing.TestBlock;
import net.devtech.asyncore.util.ref.WorldRef;
import net.devtech.yajslib.persistent.AnnotatedPersistent;
import net.devtech.yajslib.persistent.PersistentRegistry;
import net.devtech.yajslib.persistent.SimplePersistentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public final class AsynCore extends JavaPlugin implements Listener {
	public static final PersistentRegistry PERSISTENT_REGISTRY = new SimplePersistentRegistry();
	public static AsynCore instance;
	static {
		// TODO YAJSLib should use default constructor, not unsafe alloc
		PERSISTENT_REGISTRY.register(Chunk.class, new AnnotatedPersistent<>(Chunk::new, Chunk.class, 9072059811478052715L));
		PERSISTENT_REGISTRY.register(WorldRef.class, new WorldRef.WorldRefPersistent());
		PERSISTENT_REGISTRY.register(TestBlock.class, new AnnotatedPersistent<>(TestBlock.class, 2341234556789L));
	}

	public static ServerManager manager;

	@Override
	public void onEnable() {
		instance = this;
		// Plugin startup logic
		// TODO config for file location
		// TODO add config for threads
		// TODO add random tick config
		// TODO add tick rate config
		File file = new File(this.getDataFolder(), "server-data");
		manager = new ServerManager(file);
		Bukkit.getPluginManager().registerEvents(manager, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			manager.tick(); // tick server
		}, 4, 8 /*add config*/);
		this.getLogger().info("AsynCore has been loaded!");
		this.getCommand("test").setExecutor(new TestExecutor());
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
		manager.shutdown();
	}
}
