package net.devtech.asyncore.blocks.world.events;

import java.lang.annotation.*;

/**
 * annotates a method that listens to an event
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LocalEvent {
	/**
	 * the priority of the listener
	 * lowest number executes first
	 * @return 0 - 15
	 */
	int value() default 7;

	/**
	 * any additional subclasses you wish to listen to
	 */
	Class<?>[] subs() default {};
}
