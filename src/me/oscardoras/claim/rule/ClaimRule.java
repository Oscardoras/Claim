package me.oscardoras.claim.rule;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Team;

import me.oscardoras.claim.claimable.Claimable;
import me.oscardoras.claim.claimable.ProtectedClaim;
import me.oscardoras.claim.owner.EntityOwner;
import me.oscardoras.claim.owner.TeamOwner;

public enum ClaimRule {
    
    build,
    openChests,
    openDoors;
	
	
	public static boolean getClaimRuleValue(Claimable claimable, ClaimRule rule, Entity entity) {
		if (claimable instanceof ProtectedClaim) {
			ProtectedClaim protectedClaim = (ProtectedClaim) claimable;
			EntityOwner entityOwner = new EntityOwner(entity);
			Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(entity.getName());
			TeamOwner teamOwner = team != null ? new TeamOwner(team) : null;
			
			try {
				return protectedClaim.getClaimRuleValue(rule, entityOwner);
			} catch (ClaimRuleNotDefinedException ex1) {
				try {
					if (teamOwner != null) return protectedClaim.getClaimRuleValue(rule, entityOwner);
					else throw new ClaimRuleNotDefinedException();
				} catch (ClaimRuleNotDefinedException ex2) {
					try {
						return protectedClaim.getClaimRuleValue(rule, RuleTarget.NEUTRALS);
					} catch (ClaimRuleNotDefinedException ex5) {
						return false;
					}
				}
			}
		} else return true;
	}
	
	
	public static class ClaimRuleNotDefinedException extends Exception {
		
		private static final long serialVersionUID = -1937664540406592744L;
		
	}
	
}