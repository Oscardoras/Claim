package org.bukkitplugin.claim.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Team;
import org.bukkitplugin.claim.claimable.ProtectedClaim;
import org.bukkitplugin.claim.rule.RuleTarget;

public interface Owner extends RuleTarget {
	
	public String getDisplayName();
	
	public int getPower();
	
	default List<ProtectedClaim> getProtectedClaims() {
		List<ProtectedClaim> claims = new ArrayList<ProtectedClaim>();
		for (ProtectedClaim claim : ProtectedClaim.getProtectedClaims()) if (this.equals(claim.getOwner())) claims.add(claim);
		return claims;
	}
	
	default int getProtectedClaimsLength() {
		return ProtectedClaim.getProtectedClaimsLength(this);
	}
	
	default float getCoef() {
		int claims = getProtectedClaims().size();
		int power = getPower();
		float coef = claims != 0f ? (float) power / claims : power;
		if (coef < 0f) coef = 0f;
		else if (coef > 1f) coef = 1f;
		return coef;
	}
	
	
	@SuppressWarnings("deprecation")
	public static Owner getOwner(String id) {
		try {
			String[] elements = id.split("@");
			String type = elements[1];
			if (type.equals("entity")) {
				try {
					UUID uuid = UUID.fromString(elements[0]);
					Entity entity = Bukkit.getEntity(uuid);
					if (entity != null) return new EntityOwner(entity);
				} catch (IllegalArgumentException ex) {}
				return new EntityOwner(Bukkit.getOfflinePlayer(elements[0]));
			} else if (type.equals("team")) {
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(elements[0]);
				if (team != null) return new TeamOwner(team);
			}
		} catch (Exception e) {}
		return null;
	}
	
}