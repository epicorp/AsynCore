package net.devtech.asyncore.blocks.world;

import net.devtech.asyncore.util.ListUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import java.util.Collection;
import java.util.function.Function;

/**
 * for using bukkit events with custom blocks
 */
@SuppressWarnings ("unchecked")
public class BukkitEventManager {
	private static final Listener DUMMY = new Listener() {};
	private final CustomServerAccess access;
	private final Plugin plugin;

	public BukkitEventManager(CustomServerAccess access, Plugin plugin) {
		this.access = access;
		this.plugin = plugin;
	}

	public <E extends Event> void addMultiLocationConverter(Class<E> eventType, Function<E, Collection<Location>> converter, EventPriority priority, boolean ignoreCancelled) {
		Bukkit.getPluginManager().registerEvent(eventType, DUMMY, priority, (listener, event) -> {
			for (Location location : converter.apply((E) event)) {
				this.access.invoke(location, event);
			}
		}, this.plugin, ignoreCancelled);
	}

	public <E extends Event> void addMultiBlockConverter(Class<E> eventType, Function<E, Collection<Block>> converter, EventPriority priority, boolean ignoreCancelled) {
		this.addMultiLocationConverter(eventType, e -> ListUtil.mapToList(converter.apply(e), Block::getLocation), priority, ignoreCancelled);
	}

	public  <E extends Event> void addMultiLocationConverter(Class<E> event, Function<E, Collection<Location>> converter) {
		this.addMultiLocationConverter(event, converter, EventPriority.NORMAL, false);
	}

	public <E extends Event> void addMultiBlockConverter(Class<E> event, Function<E, Collection<Block>> converter) {
		this.addMultiLocationConverter(event, e -> ListUtil.mapToList(converter.apply(e), Block::getLocation));
	}

	public <E extends Event> void addLocationConverter(Class<E> eventType, Function<E, Location> converter, EventPriority priority, boolean ignoreCancelled) {
		Bukkit.getPluginManager().registerEvent(eventType, DUMMY, priority, (listener, event) -> this.access.invoke(converter.apply((E) event), event), this.plugin, ignoreCancelled);
	}

	public <E extends Event> void addBlockConverter(Class<E> eventType, Function<E, Block> converter, EventPriority priority, boolean ignoreCancelled) {
		this.addLocationConverter(eventType, e -> converter.apply(e).getLocation(), priority, ignoreCancelled);
	}

	public  <E extends Event> void addLocationConverter(Class<E> event, Function<E, Location> converter) {
		this.addLocationConverter(event, converter, EventPriority.NORMAL, false);
	}

	public <E extends Event> void addBlockConverter(Class<E> event, Function<E, Block> converter) {
		this.addLocationConverter(event, e -> converter.apply(e).getLocation());
	}
}
