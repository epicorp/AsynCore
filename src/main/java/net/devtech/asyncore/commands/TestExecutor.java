package net.devtech.asyncore.commands;

import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.testing.TestBlock;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Random;

public class TestExecutor implements CommandExecutor {
	private static final Random RANDOM = new Random();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1 && args[0].equals("a")) {
			if (sender instanceof Player) AsynCore.mainAccess.update(((Player) sender).getLocation());
			else
				return false;
		} else {
			if (sender instanceof Player) {
				AsynCore.mainAccess.getAndSet(((Player) sender).getLocation(), new TestBlock());
			} else {
				AsynCore.mainAccess.getAndSet(Bukkit.getWorlds().get(0), RANDOM.nextInt(64), RANDOM.nextInt(256), RANDOM.nextInt(64), new TestBlock());
			}
		}
		return true;
	}
}
