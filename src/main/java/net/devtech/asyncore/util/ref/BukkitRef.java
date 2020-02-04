package net.devtech.asyncore.util.ref;

/**
 * for holding safe references to bukkit objects
 * @param <T> the type that's being references
 * @param <I> an internal type that is safe to reference
 */
public abstract class BukkitRef<T, I> {
	protected I internal;
	public BukkitRef(T object) {
		this.internal = this.to(object);
	}

	public BukkitRef(I object, boolean conflictingParam /*so constructor sigs do not conflic*/) {
		this.internal = object;
	}

	public T get() {
		return this.from(this.internal);
	}

	protected abstract T from(I internal);
	protected abstract I to(T object);

	public boolean isEqual(T object) {
		return this.to(object).equals(this.internal);
	}

	@Override
	public boolean equals(Object object) {
		return this == object || object instanceof BukkitRef && this.internal.equals(((BukkitRef) object).internal);
	}

	@Override
	public int hashCode() {
		return this.internal.hashCode();
	}

	@Override
	public String toString() {
		return String.format("BukkitRef: %s (%s)", this.from(this.internal), this.internal);
	}
}
