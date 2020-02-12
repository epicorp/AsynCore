package net.devtech.asyncore.items;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.devtech.yajslib.persistent.PersistentRegistry;
import org.bukkit.inventory.ItemStack;
import java.io.IOException;

public class CustomItemFactory {
	private CustomItemFactory() {}
	public static ItemStack createNew(PersistentRegistry registry, Class<? extends CustomItem> type) {
		return wrap(registry, registry.blank(type));
	}

	public static ItemStack wrap(PersistentRegistry registry, CustomItem obj) {
		try {
			NBTItem stack = new NBTItem(obj.createBaseStack());
			stack.setByteArray("asyncore.object_data", registry.toByteArray(obj));
			return stack.getItem();
		} catch (IOException e) {
			throw new RuntimeException("Fatal error in creating new item stack with " + obj, e);
		}
	}

	public static CustomItem from(PersistentRegistry registry, ItemStack stack) {
		try {
			NBTItem nbt = new NBTItem(stack);
			return (CustomItem) registry.fromByteArray(nbt.getByteArray("asyncore.object_data"));
		} catch (IOException e) {
			throw new RuntimeException("Fatal error in parsing custom object!", e);
		}
	}

	public static <T> T from(ItemStack stack, PersistentRegistry registry) {
		try {
			NBTItem nbt = new NBTItem(stack);
			return (T) registry.fromByteArray(nbt.getByteArray("asyncore.object_data"));
		} catch (IOException e) {
			throw new RuntimeException("Fatal error in parsing custom object!", e);
		}
	}
}
