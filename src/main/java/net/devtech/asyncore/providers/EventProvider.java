package net.devtech.asyncore.providers;

import net.devtech.asyncore.blocks.world.events.BlockEventManager;
import net.devtech.asyncore.items.CustomItem;
import net.devtech.asyncore.items.ItemEventManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EventProvider {
	private final BlockEventManager eventManager;
	private final ItemEventManager itemManager;

	public EventProvider(BlockEventManager eventManager, ItemEventManager itemManager) {
		this.eventManager = eventManager;
		this.itemManager = itemManager;
	}

	public BlockEventManager getEventManager() {
		return this.eventManager;
	}

	public ItemEventManager getItemManager() {
		return this.itemManager;
	}

	public <E extends Event, I> void register(Class<E> eventType, Function<E, ItemStack> converter, Predicate<CustomItem> predicate, BiConsumer<I, E> executor, EventPriority priority, boolean ignoreCancelled) {itemManager.register(eventType, converter, predicate, executor, priority, ignoreCancelled);}

	public <E extends Event> void addMultiLocationConverter(Class<E> eventType, Function<E, Collection<Location>> converter, EventPriority priority, boolean ignoreCancelled) {eventManager.addMultiLocationConverter(eventType, converter, priority, ignoreCancelled);}

	public <E extends Event> void addMultiBlockConverter(Class<E> eventType, Function<E, Collection<Block>> converter, EventPriority priority, boolean ignoreCancelled) {eventManager.addMultiBlockConverter(eventType, converter, priority, ignoreCancelled);}

	public <E extends Event> void addMultiLocationConverter(Class<E> event, Function<E, Collection<Location>> converter) {eventManager.addMultiLocationConverter(event, converter);}

	public <E extends Event> void addMultiBlockConverter(Class<E> event, Function<E, Collection<Block>> converter) {eventManager.addMultiBlockConverter(event, converter);}

	public <E extends Event> void addLocationConverter(Class<E> eventType, Function<E, Location> converter, EventPriority priority, boolean ignoreCancelled) {eventManager.addLocationConverter(eventType, converter, priority, ignoreCancelled);}

	public <E extends Event> void addBlockConverter(Class<E> eventType, Function<E, Block> converter, EventPriority priority, boolean ignoreCancelled) {eventManager.addBlockConverter(eventType, converter, priority, ignoreCancelled);}

	public <E extends Event> void addLocationConverter(Class<E> event, Function<E, Location> converter) {eventManager.addLocationConverter(event, converter);}

	public <E extends Event> void addBlockConverter(Class<E> event, Function<E, Block> converter) {eventManager.addBlockConverter(event, converter);}
}
