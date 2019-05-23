package org.bukkitplugin.claim.command;

import org.bukkit.command.CommandSender;
import org.bukkitplugin.claim.owner.Owner;

public interface ClaimCommandExecutor {
	
	public int run(CommandSender sender, Owner owner, Object[] args);
	
}