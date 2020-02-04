package net.devtech.asyncore.ref;

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
		if (this == object) return true;
		if (!(object instanceof BukkitRef)) return false;

		BukkitRef<?, ?> ref = (BukkitRef<?, ?>) object;

		return this.internal.equals(ref.internal);
	}

	@Override
	public int hashCode() {
		return this.internal.hashCode();
	}

	@Override
	public String toString() {
		return String.format("BukkitRef: %s (%s)", this.from(this.internal), internal);
	}
}
