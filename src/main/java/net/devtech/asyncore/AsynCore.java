package net.devtech.asyncore;

import net.devtech.asyncore.blocks.handlers.ExplosionHandler;
import net.devtech.asyncore.blocks.handlers.PistonHandler;
import net.devtech.asyncore.gui.graphics.GuiManager;
import net.devtech.asyncore.blocks.world.CustomBlockDataChunk;
import net.devtech.asyncore.blocks.world.CustomBlockServer;
import net.devtech.asyncore.blocks.world.events.BlockEventManager;
import net.devtech.asyncore.blocks.world.events.LocalEventManager;
import net.devtech.asyncore.items.CanInteractWith;
import net.devtech.asyncore.items.CanPlace;
import net.devtech.asyncore.items.ItemEventManager;
import net.devtech.asyncore.providers.EventProvider;
import net.devtech.asyncore.providers.PersistentProvider;
import net.devtech.asyncore.testing.SuperFurnace;
import net.devtech.asyncore.testing.TestBlock;
import net.devtech.asyncore.testing.commands.TestExecutor;
import net.devtech.asyncore.util.persistents.BukkitPersistent;
import net.devtech.asyncore.util.ref.WorldRef;
import net.devtech.yajslib.persistent.AnnotatedPersistent;
import net.devtech.yajslib.persistent.PersistentRegistry;
import net.devtech.yajslib.persistent.SimplePersistentRegistry;
import net.devtech.yajslib.persistent.util.MapPersistent;
import net.devtech.yajslib.persistent.util.StringPersistent;
import net.devtech.yajslib.persistent.util.primitives.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO @DependsOn persistent verification
public final class AsynCore extends JavaPlugin implements Listener {
	public static PersistentRegistry persistentRegistry;
	public static GuiManager guiManager;
	public static AsynCore instance;
	public static CustomBlockServer mainWorldAccess;
	public static ItemEventManager itemEventManager;
	public static BlockEventManager blockEventManager;

	private void registerDefaultPersistents() {
		//persistentRegistry.register(TestBlock.class, new AnnotatedPersistent<>(() -> new TestBlock(persistentRegistry, mainWorldAccess), TestBlock.class, 2341234556789L));
		persistentRegistry.register(CustomBlockDataChunk.class, new AnnotatedPersistent<>(CustomBlockDataChunk::new, CustomBlockDataChunk.class, 9072059811478052715L));
		persistentRegistry.register(WorldRef.class, new WorldRef.WorldRefPersistent());
		persistentRegistry.register(LocalEventManager.class, new AnnotatedPersistent<>(LocalEventManager::new, LocalEventManager.class, 765435676545L));
		persistentRegistry.register(String.class, new StringPersistent(2893488382892L));
		// order matters!
		//persistentRegistry.register(SuperFurnace.class, new AnnotatedPersistent<>(() -> new SuperFurnace(persistentRegistry, mainWorldAccess), SuperFurnace.class, 43092091209L));
		//persistentRegistry.register(SuperFurnace.class, new AnnotatedPersistent<>(() -> new SuperFurnace(persistentRegistry, mainWorldAccess), SuperFurnace.class, 638954032L));
		persistentRegistry.register((Class)Map.class, new MapPersistent(3289329829L));
		persistentRegistry.register(Integer.class, new IntegerPersistent(4390230909L));
		persistentRegistry.register(Float.class, new FloatPersistent(3292093290L));
		persistentRegistry.register(Double.class, new DoublePersistent(48923909L));
		persistentRegistry.register(Long.class, new LongPersistent(490920909L));
		persistentRegistry.register(Character.class, new CharPersistent(382094309L));
		persistentRegistry.register(Short.class, new ShortPersistent(4820940920932L));
		persistentRegistry.register(Byte.class, new BytePersistent(5493892894389L));
		persistentRegistry.register(Boolean.class, new BooleanPersistent(438390923091209L));
		persistentRegistry.register(Location.class, new BukkitPersistent<>(4893893483442L, Location::deserialize));
	}


	@Override
	public void onEnable() {
		instance = this;
		guiManager = new GuiManager();
		persistentRegistry = new SimplePersistentRegistry();
		// register persistents
		this.registerDefaultPersistents();
		Bukkit.getServer().getServicesManager().register(PersistentProvider.class, new PersistentProvider(persistentRegistry), this, ServicePriority.Normal);

		// load config
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

		// create server
		File file = new File(this.getDataFolder(), AsynCoreConfig.worldDir);
		mainWorldAccess = new CustomBlockServer(file, null /* maybe some special null block here?*/);

		// block events
		blockEventManager = new BlockEventManager(mainWorldAccess, this);
		blockEventManager.addBlockConverter(BlockBreakEvent.class, BlockBreakEvent::getBlock);
		blockEventManager.addBlockConverter(PlayerInteractEvent.class, PlayerInteractEvent::getClickedBlock);
		blockEventManager.addBlockConverter(BlockBurnEvent.class, BlockEvent::getBlock);
		blockEventManager.addBlockConverter(BlockDamageEvent.class, BlockEvent::getBlock);
		blockEventManager.addBlockConverter(BlockDispenseEvent.class, BlockEvent::getBlock);
		blockEventManager.addBlockConverter(BlockExpEvent.class, BlockEvent::getBlock);
		blockEventManager.addBlockConverter(BlockFadeEvent.class, BlockEvent::getBlock);
		blockEventManager.addBlockConverter(EntityChangeBlockEvent.class, EntityChangeBlockEvent::getBlock);
		blockEventManager.addBlockConverter(BlockFormEvent.class, BlockEvent::getBlock);
		blockEventManager.addBlockConverter(BlockPhysicsEvent.class, BlockEvent::getBlock);
		blockEventManager.addMultiBlockConverter(BlockPistonExtendEvent.class, BlockPistonExtendEvent::getBlocks);
		blockEventManager.addMultiBlockConverter(BlockPistonRetractEvent.class, BlockPistonRetractEvent::getBlocks);
		blockEventManager.addMultiBlockConverter(EntityExplodeEvent.class, EntityExplodeEvent::blockList);

		// hmmm...
		blockEventManager.addMultiBlockConverter(BlockExplodeEvent.class, BlockExplodeEvent::blockList);

		// item events
		itemEventManager = new ItemEventManager(this, persistentRegistry);
		itemEventManager.register(BlockPlaceEvent.class, BlockPlaceEvent::getItemInHand, c -> c instanceof CanPlace, CanPlace::place, EventPriority.NORMAL, false);
		itemEventManager.register(PlayerInteractEvent.class, PlayerInteractEvent::getItem, c -> c instanceof CanInteractWith, CanInteractWith::interact, EventPriority.NORMAL, false);

		// custom transformers
		Bukkit.getServer().getServicesManager().register(EventProvider.class, new EventProvider(blockEventManager, itemEventManager), this, ServicePriority.Normal);

		// event listeners
		Bukkit.getPluginManager().registerEvents(guiManager, this);
		Bukkit.getPluginManager().registerEvents(new PistonHandler(mainWorldAccess), this);
		Bukkit.getPluginManager().registerEvents(new ExplosionHandler(mainWorldAccess), this);
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(mainWorldAccess, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mainWorldAccess::tick, 2, AsynCoreConfig.ticks);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, mainWorldAccess::init, 0);
		this.getCommand("test").setExecutor(new TestExecutor());
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
		mainWorldAccess.shutdown();
	}

	@EventHandler
	public void breakEvent(BlockBreakEvent event) {
		Player player = event.getPlayer();
		ItemStack stack = player.getInventory().getItemInOffHand();
		if(stack != null && stack.getType() == Material.SPONGE) {
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR, false);
		}
	}
}
