package net.devtech.asyncore.testing.commands;

import net.devtech.asyncore.AsynCore;
import net.devtech.asyncore.items.CustomItemFactory;
import net.devtech.asyncore.testing.SuperFurnace;
import net.devtech.asyncore.testing.TestBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1 && args[0].equals("a")) {
			AsynCore.mainWorldAccess.forChunks((w, x, z, c) -> {
				if(x == -7 && z == 53)
					System.out.println("ohno");
				if(c.loaded() && !c.isEmpty())
					System.out.printf("%s %d %d\n",w, x, z);
			});
			if (sender instanceof Player)
				AsynCore.mainWorldAccess.invoke(((Player) sender).getLocation(), "hello!");
			else
				return false;
		} else {
			if (sender instanceof Player) {
				AsynCore.mainWorldAccess.add(((Player) sender).getLocation(), new TestBlock(AsynCore.persistentRegistry, AsynCore.mainWorldAccess));
				((Player) sender).getInventory().addItem(CustomItemFactory.createNew(AsynCore.persistentRegistry, SuperFurnace.class));
			}
		}
		return true;
	}
}
