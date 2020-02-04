package net.devtech.asyncore;

import net.devtech.asyncore.blocks.world.Chunk;
import net.devtech.asyncore.blocks.world.ServerManager;
import net.devtech.asyncore.commands.TestExecutor;
import net.devtech.asyncore.testing.TestBlock;
import net.devtech.asyncore.util.ref.WorldRef;
import net.devtech.yajslib.persistent.AnnotatedPersistent;
import net.devtech.yajslib.persistent.PersistentRegistry;
import net.devtech.yajslib.persistent.SimplePersistentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

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
		File config = new File(this.getDataFolder(), "settings.yml");
		try {
			if (config.exists()) {
				FileConfiguration configuration = YamlConfiguration.loadConfiguration(config);
				AsynCoreConfig.load(configuration);
			} else {
				YamlConfiguration configuration = new YamlConfiguration();
				AsynCoreConfig.save(configuration);
				configuration.save(config);
			}
		} catch (IOException e) {
			this.getLogger().severe("error in loading config!");
			throw new RuntimeException(e);
		}
		
		File file = new File(this.getDataFolder(), AsynCoreConfig.worldDir);
		manager = new ServerManager(file);
		Bukkit.getPluginManager().registerEvents(manager, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			manager.tick(); // tick server
		}, 4, AsynCoreConfig.ticks);
		this.getLogger().info("AsynCore has been loaded!");
		this.getCommand("test").setExecutor(new TestExecutor());
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
		File config = new File(this.getDataFolder(), "settings.yml");
		YamlConfiguration configuration = new YamlConfiguration();
		AsynCoreConfig.save(configuration);
		try {
			configuration.save(config);
		} catch (IOException e) {
			this.getLogger().severe("error in saving config!");
			e.printStackTrace();
		}
		manager.shutdown();
	}
}
