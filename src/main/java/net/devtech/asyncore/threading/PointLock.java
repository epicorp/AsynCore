package net.devtech.asyncore.threading;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * a fancy lock for locking things on a coordinate plane, yes.
 */
public class PointLock {
	// when v == null, location is not locked
	// when v != null, location is locked
	private final Long2ObjectMap<Queue<Runnable>> listeners = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

	/**
	 * locks the given coordinates
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void lock(int x, int y) {
		synchronized (this.listeners) {
			this.listeners.put((long) x << 32 | y & 0xFFFFFFFFL, new LinkedBlockingQueue<>());
		}
	}

	/**
	 * unlocks the given coordinates
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void unlock(int x, int y) {
		synchronized (this.listeners) {
			long key = (long) x << 32 | y & 0xFFFFFFFFL;
			this.run(key);
			this.listeners.remove(key);
		}
	}

	/**
	 * executes the listener when the lock becomes available, or immediately if it's already available
	 * if the lock is already available, it is run on the same thread it is called on, if it's unavailable
	 * the runnable is called on the same thread that {@link #unlock(int, int)} is called on
	 * @param x the x coordinate of the pos
	 * @param y the y coordinate of the pos
	 * @param when the action to run when competed
	 */
	public void waitFor(int x, int y, Runnable when) {
		synchronized (this.listeners) {
			Queue<Runnable> queue = this.listeners.get((long) x << 32 | y & 0xFFFFFFFFL);
			if (queue == null) {
				this.lock(x, y);
				when.run();
				this.unlock(x, y);
			} else queue.add(when);
		}
	}



	/**
	 * waits on the current thread for the coordinates to become available
	 * this is inherently unsafe and should never be done
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void wait(int x, int y) {
		AtomicBoolean wait;
		synchronized (this.listeners) {
			Queue<Runnable> queue = this.listeners.get((long) x << 32 | y & 0xFFFFFFFFL);
			if (queue == null)
				return;
			else {
				wait = new AtomicBoolean(true);
				queue.add(() -> wait.set(false));
			}
		}

		while (wait.get());
	}

	/**
	 * waits on the current thread for the coordinates to become available and immediately locks the coordinates
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void waitAndLock(int x, int y) {
		this.wait(x, y);
		this.lock(x, y);
	}

	private void run(long key) {
		Queue<Runnable> queue = this.listeners.get(key);
		while (!queue.isEmpty()) queue.poll().run();
	}
}
