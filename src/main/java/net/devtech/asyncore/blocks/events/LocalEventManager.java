package net.devtech.asyncore.blocks.events;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.devtech.asyncore.world.chunk.BlockTracker;
import net.devtech.utilib.functions.ThrowingConsumer;
import net.devtech.utilib.structures.inheritance.InheritedMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalEventManager implements BlockTracker<Object> {
	private static final Logger LOGGER = Logger.getLogger("LocalEventManager");
	private static final Map<Class<?>, BiConsumer<Object, Short>> REFLECTION_CACHE = new ConcurrentHashMap<>();
	private static final InheritedMap<Object, Method> METHODS = InheritedMap.getMethods(Object.class);
	// pog map
	private final Short2ObjectMap<Map<Class<?>, List<Consumer<?>>[]>> listenerMap = new Short2ObjectOpenHashMap<>();

	@Override
	public void set(int x, int y, int z, Object object) {
		REFLECTION_CACHE.computeIfAbsent(object.getClass(), this::compute).accept(object, (short) (x | z << 4 | y << 8));
	}

	@Override
	public void remove(int x, int y, int z, Object object) {
		this.listenerMap.remove((short) (x | z << 4 | y << 8));
	}

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
						}
					}
				}
			}
		}
	}

	private BiConsumer<Object, Short> compute(Class<?> type) {
		List<BiConsumer<Object, Short>> consumers = new ArrayList<>();
		for (Method attribute : METHODS.getAttributes(type)) { // ideally we cache this but I'm a lazy fuck so no.
			int mod = attribute.getModifiers();
			LocalEvent events = attribute.getAnnotation(LocalEvent.class);
			if (events != null) {
				Class<?>[] types = attribute.getParameterTypes();
				if (types.length != 1)
					throw new IllegalArgumentException(attribute + " does not have the expected method signature!");
				if (Modifier.isStatic(mod)) throw new IllegalArgumentException(attribute + " is static!");

				if(!Modifier.isFinal(mod)) {
					LOGGER.log(Level.WARNING,"{} is not final! Method overriding is *not* supported by the LocalEventManger", attribute);
				}
				consumers.add((object, key) -> this.register(types[0], attribute, object, events.value(), key));
				for (Class<?> sub : events.subs()) {
					consumers.add((object, key) -> this.register(sub, attribute, object, events.value(), key));
				}
			}
		}

		return (o, s) -> {
			for (BiConsumer<Object, Short> consumer : consumers) {
				consumer.accept(o, s);
			}
		};
	}

	private <T> void register(Class<T> eventType, Method method, Object object, int priority, short key) {
		List<Consumer<?>>[] listeners = this.listenerMap.computeIfAbsent(key, k -> new HashMap<>()).computeIfAbsent(eventType, c -> new List[16]);
		if (listeners[priority] == null) listeners[priority] = new ArrayList<>(); // ohyes
		listeners[priority].add((ThrowingConsumer<T>) e -> method.invoke(object, e));
	}


}
