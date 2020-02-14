package net.devtech.asyncore;

import net.devtech.asyncore.blocks.CustomBlock;
import net.devtech.asyncore.blocks.world.*;
import net.devtech.asyncore.blocks.world.events.BukkitEventManager;
import net.devtech.asyncore.blocks.world.events.LocalEventManager;
import net.devtech.asyncore.commands.TestExecutor;
import net.devtech.asyncore.items.CanInteractWith;
import net.devtech.asyncore.items.CanPlace;
import net.devtech.asyncore.items.CustomItem;
import net.devtech.asyncore.items.ItemEventManager;
import net.devtech.asyncore.testing.TestBlock;
import net.devtech.asyncore.util.ref.WorldRef;
import net.devtech.asyncore.world.server.ServerAccess;
import net.devtech.yajslib.persistent.AnnotatedPersistent;
import net.devtech.yajslib.persistent.PersistentRegistry;
import net.devtech.yajslib.persistent.SimplePersistentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

public final class AsynCore extends JavaPlugin implements Listener {
	public static final PersistentRegistry PERSISTENT_REGISTRY = new SimplePersistentRegistry();
	public static AsynCore instance;
	public static CustomBlockServer mainAccess;
	public static ItemEventManager items;
	public static BukkitEventManager bukkitEventManager;
	static {
		PERSISTENT_REGISTRY.register(TestBlock.class, new AnnotatedPersistent<>(() -> new TestBlock(PERSISTENT_REGISTRY, mainAccess), TestBlock.class, 2341234556789L));
		PERSISTENT_REGISTRY.register(CustomBlockDataChunk.class, new AnnotatedPersistent<>(CustomBlockDataChunk::new, CustomBlockDataChunk.class, 9072059811478052715L));
		PERSISTENT_REGISTRY.register(WorldRef.class, new WorldRef.WorldRefPersistent());
		PERSISTENT_REGISTRY.register(LocalEventManager.class, new AnnotatedPersistent<>(LocalEventManager::new, LocalEventManager.class, 765435676545L));
		PERSISTENT_REGISTRY.register(ChunkTicker.class, new AnnotatedPersistent<>(ChunkTicker::new, ChunkTicker.class, 4734325434254L));
	}

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
		mainAccess = new CustomBlockServer(file, null /* maybe some special null block here?*/);

		Bukkit.getPluginManager().registerEvents(mainAccess, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			mainAccess.tick(); // tick server
		}, 0, AsynCoreConfig.ticks);
		this.getCommand("test").setExecutor(new TestExecutor());

		// blocks
		bukkitEventManager = new BukkitEventManager(mainAccess, this);
		bukkitEventManager.addBlockConverter(BlockBreakEvent.class, BlockBreakEvent::getBlock);
		// items

		items = new ItemEventManager(this, PERSISTENT_REGISTRY);
		items.register(BlockPlaceEvent.class, BlockPlaceEvent::getItemInHand, c -> c instanceof CanPlace, CanPlace::place, EventPriority.NORMAL, false);
		items.register(PlayerInteractEvent.class, PlayerInteractEvent::getItem, c -> c instanceof CanInteractWith, CanInteractWith::interact, EventPriority.NORMAL, false);
		mainAccess.init();
		this.getLogger().info("AsynCore has been loaded!");
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
		mainAccess.shutdown();
	}
}
