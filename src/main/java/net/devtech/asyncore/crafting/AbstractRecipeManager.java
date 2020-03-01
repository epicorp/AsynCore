package net.devtech.asyncore.crafting;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRecipeManager<R> implements Listener {
	protected final Set<UUID> restricted = Collections.newSetFromMap(new ConcurrentHashMap<>());
	protected final List<R> recipes = new Vector<>();
	protected Plugin plugin;

	public void register(R recipe) {
		this.recipes.add(recipe);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void quit(PlayerQuitEvent event) {
		this.restricted.remove(event.getPlayer().getUniqueId());
	}

	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}
}
