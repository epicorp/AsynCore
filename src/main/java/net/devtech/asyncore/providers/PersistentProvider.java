package net.devtech.asyncore.providers;

import net.devtech.yajslib.persistent.Persistent;
import net.devtech.yajslib.persistent.PersistentRegistry;
import java.io.IOException;

public class PersistentProvider {
	private final PersistentRegistry registry;

	public PersistentProvider(PersistentRegistry registry) {
		this.registry = registry;
	}

	public PersistentRegistry getRegistry() {
		return this.registry;
	}

	public Persistent<?> fromId(long l) {return registry.fromId(l);}

	public <T> Persistent<T> forClass(Class<T> aClass, boolean b) {return registry.forClass(aClass, b);}

	public <T> void register(Class<T> aClass, Persistent<T> persistent) {registry.register(aClass, persistent);}

	public byte[] toByteArray(Object object) throws IOException {return registry.toByteArray(object);}

	public Object fromByteArray(byte[] data) throws IOException {return registry.fromByteArray(data);}

	public <T> T blank(Class<T> type) {return registry.blank(type);}
}
