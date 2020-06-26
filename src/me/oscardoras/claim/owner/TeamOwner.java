package me.oscardoras.claim.owner;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamOwner implements Owner {
	
	protected final Team team;
	
	public TeamOwner(Team team) {
		this.team = team;
	}
	
	public Team getTeam() {
		return team;
	}
	
	@Override
	public String getId() {
		return team.getName() + "@team";
	}
	
	@Override
	public String getDisplayName() {
		return team.getDisplayName();
	}
	
	@Override
	public int getPower() {
		int power = 0;
		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("power");
		if (objective != null) for (String entry : team.getEntries()) power += objective.getScore(entry).getScore();
		return power;
	}
	
	@Override
	public void reloadClaimLength() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective tProtectedClaims = scoreboard.getObjective("tProtectedClaims");
		if (tProtectedClaims == null) tProtectedClaims = scoreboard.registerNewObjective("tProtectedClaims", "dummy", "Team protected claims");
		
		int length = getProtectedClaimsLength();
		for (String entry : team.getEntries())
			tProtectedClaims.getScore(entry).setScore(length);
	}
	
	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof TeamOwner && team.equals(((TeamOwner) object).team);
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash *= 6 + team.hashCode();
		return hash;
	}
	
	
	public static TeamOwner getByEntity(Entity entity) {
		String entry;
		if (entity instanceof Player) entry = entity.getName();
		else entry = entity.getUniqueId().toString();
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(entry);
		if (team != null) return new TeamOwner(team);
		else return null;
	}
	
}