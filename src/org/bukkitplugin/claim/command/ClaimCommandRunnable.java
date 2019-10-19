package org.bukkitplugin.claim.command;

import org.bukkitplugin.claim.owner.Owner;
import org.bukkitutils.command.v1_14_3_V1.CommandRegister.PerformedCommand;

@FunctionalInterface
public interface ClaimCommandRunnable {
	
	public int run(PerformedCommand cmd, Owner owner);
	
}