package net.devtech.asyncore.items;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class CustomItemFactory {
	private CustomItemFactory() {}

	public static ItemStack createNew(PersistentRegistry registry, Class<? extends CustomItem> type) {
		return wrap(registry, registry.blank(type));
	}

	public static void save(PersistentRegistry registry, CustomItem obj, ItemStack stack) {
		try {
			NBTItem nbtItem = new NBTItem(stack);
			nbtItem.setByteArray("asyncore.object_data", registry.toByteArray(obj));
			ItemMeta meta = nbtItem.getItem().getItemMeta();
			stack.setItemMeta(meta);
			if (obj != null)
				obj.transform(stack);
		} catch (IOException e) {
			throw new RuntimeException("Fatal error in creating new item stack with " + obj, e);
		}
	}

	public static ItemStack wrap(PersistentRegistry registry, CustomItem obj) {
		try {
			NBTItem nbtItem = new NBTItem(obj.createBaseStack());
			nbtItem.setByteArray("asyncore.object_data", registry.toByteArray(obj));
			ItemStack stack = nbtItem.getItem();
			obj.transform(stack);
			return stack;
		} catch (IOException e) {
			throw new RuntimeException("Fatal error in creating new item stack with " + obj, e);
		}
	}

	public static boolean isCustomItem(ItemStack stack) {
		NBTItem nbt = new NBTItem(stack);
		byte[] data = nbt.getByteArray("asyncore.object_data");
		return data.length != 0;
	}

	public static CustomItem from(PersistentRegistry registry, ItemStack stack) {
		try {
			NBTItem nbt = new NBTItem(stack);
			byte[] data = nbt.getByteArray("asyncore.object_data");
			if (data.length != 0) return (CustomItem) registry.fromByteArray(data);
			else return null;
		} catch (IOException e) {
			throw new RuntimeException("Fatal error in parsing custom object!", e);
		}
	}

	public static <T> T from(ItemStack stack, PersistentRegistry registry) {
		try {
			NBTItem nbt = new NBTItem(stack);
			byte[] data = nbt.getByteArray("asyncore.object_data");
			if (data.length != 0) return (T) registry.fromByteArray(data);
			else return null;
		} catch (IOException e) {
			throw new RuntimeException("Fatal error in parsing custom object!", e);
		}
	}

	public static <T> void modify(ItemStack stack, PersistentRegistry registry, Consumer<T> editor) {
		T obj = from(stack, registry);
		if (obj != null) {
			editor.accept(obj);
			save(registry, (CustomItem) obj, stack);
		}
	}
}
