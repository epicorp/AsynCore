package net.devtech.asyncore.threading;

import net.devtech.utilib.functions.ThrowingRunnable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class GenericLock {
	private AtomicBoolean locked = new AtomicBoolean(false);
	private Queue<Runnable> listeners = new LinkedBlockingQueue<>();
	public void waitFor() {
		while (this.isLocked());
	}

	public void lock() {
		this.locked.set(true);
	}

	public void unlock() {
		while (!this.listeners.isEmpty()) this.listeners.poll().run();
		this.locked.set(false);
	}

	public boolean isLocked() {
		return this.locked.get();
	}

	public void wait(ThrowingRunnable action) {
		if(this.isLocked())
			this.listeners.add(action);
		else {
			this.lock();
			action.run();
			this.unlock();
		}
	}
}
