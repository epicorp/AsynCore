package net.devtech.asyncore.blocks.world.events;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.devtech.asyncore.blocks.events.DestroyEvent;
import net.devtech.asyncore.blocks.events.PlaceEvent;
import net.devtech.asyncore.world.chunk.BlockTracker;
import net.devtech.asyncore.world.chunk.DataChunk;
import net.devtech.utilib.functions.ThrowingConsumer;
import net.devtech.utilib.functions.TriConsumer;
import net.devtech.utilib.functions.TriFunction;
import net.devtech.utilib.structures.inheritance.InheritedMap;
import net.devtech.yajslib.annotations.Reader;
import net.devtech.yajslib.annotations.Writer;
import net.devtech.yajslib.io.PersistentInput;
import net.devtech.yajslib.io.PersistentOutput;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * manages per block events for a chunk
 */
public class LocalEventManager implements BlockTracker<Object> {
	private static final Map<Class<?>, TriConsumer<LocalEventManager, Object, Short>> REFLECTION_CACHE = new ConcurrentHashMap<>();
	private static final Map<Class<?>, List<Consumer<?>>[]> DUMMY_MAP = Collections.emptyMap();
	private static final Logger LOGGER = Logger.getLogger("LocalEventManager");
	private static final InheritedMap<Object, Method> METHODS = InheritedMap.getMethods(Object.class);
	private final Short2ObjectMap<Map<Class<?>, List<Consumer<?>>[]>> listenerMap = new Short2ObjectOpenHashMap<>();

	@Override
	public void init(DataChunk<Object> chunk) {
		for (short pack : this.listenerMap.keySet()) { // filled with dummy maps
			int x = pack & 15, z = (pack >> 4) & 15, y = ((pack & 0xffff) >> 8);
			this.listenerMap.remove(pack); // remove dummy
			Object object = chunk.get(x, y, z);
			// set without notifying listeners
			REFLECTION_CACHE.computeIfAbsent(object.getClass(), LocalEventManager::compute).accept(this, object, pack);
		}
	}

	@Override
	public void set(Object object, int x, int y, int z) {
		short key = (short) ((x & 15) | (z & 15) << 4 | y << 8);
		REFLECTION_CACHE.computeIfAbsent(object.getClass(), LocalEventManager::compute).accept(this, object, key);
	}

	@Override
	public void remove(Object object, int x, int y, int z) {
		short key = (short) ((x & 15) | (z & 15) << 4 | y << 8);
		this.listenerMap.remove(key);
	}

	/**
	 * invoke an event at the given location
	 *
	 * @param key the packed coordinates
	 * @param event the event
	 */
	@SuppressWarnings ({"unchecked", "rawtypes"})
	public void invoke(short key, Object event) {
		Map<Class<?>, List<Consumer<?>>[]> arr = this.listenerMap.get(key);
		if (arr != null) {
			List<Consumer<?>>[] listeners = arr.get(event.getClass());
			if (listeners != null) {
				for (List<Consumer<?>> listener : listeners) {
					if (listener != null) {
						for (Consumer consumer : listener) {
							consumer.accept(event);
							if(event instanceof Cancellable && ((Cancellable) event).isCancelled())
								return; // exit if cancellable
						}
					}
				}
			}
		}
	}

	public void invoke(int x, int y, int z, Object object) {
		this.invoke((short) ((x & 15) | (z & 15) << 4 | y << 8), object);
	}

	public void invokeAll(TriFunction<Integer, Integer, Integer, Object> eventFunction) {
		for (Short2ObjectMap.Entry<Map<Class<?>, List<Consumer<?>>[]>> entry : this.listenerMap.short2ObjectEntrySet()) {
			short pack = entry.getShortKey();
			int x = pack & 15, z = (pack >> 4) & 15, y = ((pack & 0xffff) >> 8);
			Object event = eventFunction.apply(x, y, z);
			this.invoke(pack, event);
		}
	}

	private static TriConsumer<LocalEventManager, Object, Short> compute(Class<?> type) {
		List<TriConsumer<LocalEventManager, Object, Short>> consumers = new ArrayList<>();
		for (Method attribute : METHODS.getAttributes(type)) { // ideally we cache this but I'm a lazy fuck so no.
			int mod = attribute.getModifiers();
			LocalEvent events = attribute.getAnnotation(LocalEvent.class);
			if (events != null) {
				Class<?>[] types = attribute.getParameterTypes();
				if (types.length != 1)
					throw new IllegalArgumentException(attribute + " does not have the expected method signature!");
				if (Modifier.isStatic(mod)) throw new IllegalArgumentException(attribute + " is static!");

				consumers.add((manager, object, key) -> manager.register(types[0], attribute, object, events.value(), key));
				for (Class<?> sub : events.subs()) {
					consumers.add((manager, object, key) -> manager.register(sub, attribute, object, events.value(), key));
				}
			}
		}

		return (m, o, s) -> {
			for (TriConsumer<LocalEventManager, Object, Short> consumer : consumers) {
				consumer.accept(m, o, s);
			}
		};
	}

	private <T> void register(Class<T> eventType, Method method, Object object, int priority, short key) {
		List<Consumer<?>>[] listeners = this.listenerMap.computeIfAbsent(key, k -> new HashMap<>()).computeIfAbsent(eventType, c -> new List[16]);
		if (listeners[priority] == null) listeners[priority] = new ArrayList<>(); // ohyes
		listeners[priority].add((ThrowingConsumer<T>) e -> method.invoke(object, e));
	}

	@Reader (765435676545L)
	public void read(PersistentInput input) throws IOException {
		int size = input.readInt();
		for (int i = 0; i < size; i++) {
			this.listenerMap.put(input.readShort(), DUMMY_MAP);
		}
	}

	@Writer (765435676545L)
	public void write(PersistentOutput output) throws IOException {
		output.writeInt(this.listenerMap.size());
		for (short val : this.listenerMap.keySet()) {
			output.writeShort(val);
		}
	}
}
