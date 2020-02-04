package net.devtech.asyncore;

import org.bukkit.configuration.file.FileConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;

public class AsynCoreConfig {
	@Option("asyncore.worldDir")
	public static String worldDir = "server-data";
	@Option("asyncore.ioThreads")
	public static int threads = 1;
	@Option("asyncore.randTicks")
	public static int rand = 3;
	@Option("asyncore.tickRate")
	public static int ticks = 8;


	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Option {
		String value();
	}

	public static void save(FileConfiguration configuration) {
		options((o, f) -> {
			try {
				configuration.set(o.value(), f.get(null));
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static void load(FileConfiguration configuration) {
		options((o, f) -> {
			try {
				f.set(null, configuration.get(o.value()));
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static void options(BiConsumer<Option, Field> cons) {
		for (Field field : AsynCoreConfig.class.getFields()) {
			Option option = field.getAnnotation(Option.class);
			if(option != null) {
				cons.accept(option, field);
			}
		}
	}
}
