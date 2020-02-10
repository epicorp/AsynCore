package net.devtech.asyncore.blocks.events;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.devtech.utilib.functions.ThrowingConsumer;
import net.devtech.utilib.structures.inheritance.InheritedMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LocalEventManager {
	private static final Map<Class<?>, BiConsumer<Object, Short>> REFLECTION_CACHE = new ConcurrentHashMap<>();
	private static final InheritedMap<Object, Method> METHODS = InheritedMap.getMethods(Object.class);
	// pog map
	private final Short2ObjectMap<Map<Class<?>, List<Consumer<?>>[]>> listenerMap = new Short2ObjectOpenHashMap<>();

	public void register(LocalListener object, short key) {
		REFLECTION_CACHE.computeIfAbsent(object.getClass(), this::compute).accept(object, key);
	}

	private BiConsumer<Object, Short> compute(Class<?> type) {
		List<BiConsumer<Object, Short>> consumers = new ArrayList<>();
		Set<String> registered = new HashSet<>(); // for inheritance
		for (Method attribute : METHODS.getAttributes(type)) { // ideally we cache this but I'm a lazy fuck so no.
			String string = attribute.getName() + ";" + Arrays.toString(attribute.getParameterTypes()); // good enough tbh
			if (!registered.contains(string)) {
				registered.add(string);
				int mod = attribute.getModifiers();
				LocalEvent events = attribute.getAnnotation(LocalEvent.class);
				if (events != null) {
					Class<?>[] types = attribute.getParameterTypes();
					if (types.length != 1)
						throw new IllegalArgumentException(attribute + " does not have the expected method signature!");
					if (Modifier.isStatic(mod)) throw new IllegalArgumentException(attribute + " is static!");
					consumers.add((object, key) -> this.register(types[0], attribute, object, events.value(), key));
					for (Class<?> sub : events.subs()) {
						consumers.add((object, key) -> this.register(sub, attribute, object, events.value(), key));
					}
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

	public void remove(short key) {
		this.listenerMap.remove(key);
	}

	private static final List[] EMPTY_ARR = new List[0];

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
}
