package net.devtech.asyncore.commands;

import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.testing.TestBlock;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestExecutor implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			AsynCore.manager.set(((Player) sender).getLocation(), new TestBlock());
		} else {
			AsynCore.manager.set(Bukkit.getWorlds().get(0), 0, 0, 0, new TestBlock());
		}
		return true;
	}
}
