package me.oscardoras.claim.command;

import me.oscardoras.claim.owner.Owner;
import me.oscardoras.spigotutils.command.v1_16_1_V1.CommandRegister.PerformedCommand;

@FunctionalInterface
public interface ClaimCommandRunnable {
	
	public int run(PerformedCommand cmd, Owner owner);
	
}