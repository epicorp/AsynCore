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
		if(sender instanceof Player) {
			AsynCore.access.getAndPlace(((Player) sender).getLocation(), new TestBlock());
		} else {
			AsynCore.access.getAndPlace(Bukkit.getWorlds().get(0), RANDOM.nextInt(64), RANDOM.nextInt(256), RANDOM.nextInt(64), new TestBlock());
		}
		return true;
	}
}
