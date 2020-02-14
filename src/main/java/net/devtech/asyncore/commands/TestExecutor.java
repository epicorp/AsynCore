package net.devtech.asyncore.commands;

import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.items.CustomItemFactory;
import net.devtech.asyncore.testing.TestBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1 && args[0].equals("a")) {
			if (sender instanceof Player)
				AsynCore.mainAccess.invoke(((Player) sender).getLocation(), "hello!");
			else
				return false;
		} else {
			if (sender instanceof Player) {
				AsynCore.mainAccess.getAndSet(((Player) sender).getLocation(), new TestBlock(AsynCore.PERSISTENT_REGISTRY, AsynCore.mainAccess));
				((Player) sender).getInventory().addItem(CustomItemFactory.createNew(AsynCore.PERSISTENT_REGISTRY, TestBlock.class));
			}
		}
		return true;
	}
}
