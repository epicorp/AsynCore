package net.devtech.asyncore.items;

import net.devtech.utilib.functions.TriConsumer;
import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * manages per-custom item events, all custom items must be registered for events to work
 */
public class ItemEventManager {
	private static final Listener DUMMY = new Listener() {};
	private final Plugin plugin;
	private final PersistentRegistry registry;

	public ItemEventManager(Plugin plugin, PersistentRegistry registry) {
		this.plugin = plugin;
		this.registry = registry;
	}

	/**
	 * register a per-item event handler
	 *
	 * @param eventType the bukkit event
	 * @param converter the method to get an item from a event
	 * @param predicate checks if the object is valid ex. instanceof ISomething
	 * @param executor the method to invoke the event on the object
	 * @param priority the priority of the bukkit listener
	 * @param ignoreCancelled whether cancelled events should be passed or not
	 * @param <E> the event type
	 * @param <I> the interface type if there is one
	 */
	@SuppressWarnings ("unchecked")
	public <E extends Event, I> void register(Class<E> eventType, Function<E, ItemStack> converter, Predicate<CustomItem> predicate, BiConsumer<I, E> executor, EventPriority priority, boolean ignoreCancelled) {
		Bukkit.getPluginManager().registerEvent(eventType, DUMMY, priority, (l, e) -> {
			E event = (E) e;
			ItemStack converted = converter.apply(event);
			CustomItem item = CustomItemFactory.from(converted, this.registry);
			if (predicate.test(item)) {
				executor.accept((I) item, event);
			}
		}, this.plugin, ignoreCancelled);
	}
}
