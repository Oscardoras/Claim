package org.bukkitplugin.claim.owner;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Team;
import org.bukkitplugin.claim.ClaimPlugin;

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
	public String getName() {
		return team.getDisplayName();
	}
	
	@Override
	public int getMaxPower() {
		int power = 0;
		ClaimPlugin.plugin.recalculateScores();
		for (String entry : team.getEntries()) power += Bukkit.getScoreboardManager().getMainScoreboard().getObjective("maxPower").getScore(entry).getScore();
		return power;
	}
	
	@Override
	public int getPower() {
		int power = 0;
		ClaimPlugin.plugin.recalculateScores();
		for (String entry : team.getEntries()) power += Bukkit.getScoreboardManager().getMainScoreboard().getObjective("power").getScore(entry).getScore();
		return power;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null) {
			if (object instanceof TeamOwner) return team.equals(((TeamOwner) object).team);
			if (object instanceof Team) return team.equals((Team) object);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return team.hashCode();
	}
	
	
	public static TeamOwner getByEntity(Entity entity) {
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(entity.getName());
		if (team == null) return new TeamOwner(team);
		else return null;
	}
	
}