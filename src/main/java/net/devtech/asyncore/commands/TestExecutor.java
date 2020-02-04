package net.devtech.asyncore.commands;

import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.testing.TestBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestExecutor implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			AsynCore.manager.set(((Player) sender).getLocation(), new TestBlock());
		}
		return true;
	}
}
