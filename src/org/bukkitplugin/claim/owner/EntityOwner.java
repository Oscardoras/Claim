package org.bukkitplugin.claim.owner;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class EntityOwner implements Owner {
	
	protected final UUID uuid;
	protected final String entry;
	
	@SuppressWarnings("deprecation")
	public EntityOwner(String entry) {
		UUID uuid = null;
		try {
			Entity entity = Bukkit.getEntity(UUID.fromString(entry));
			if (entity != null) uuid = entity.getUniqueId();
		} catch (IllegalArgumentException ex) {}
		if (uuid == null) uuid = Bukkit.getOfflinePlayer(entry).getUniqueId();
		this.uuid = uuid;
		this.entry = entry;
	}
	
	public EntityOwner(Entity entity) {
		this.uuid = entity.getUniqueId();
		if (entity instanceof Player) this.entry = entity.getName();
		else this.entry = uuid.toString();
	}
	
	public EntityOwner(OfflinePlayer offlinePlayer) {
		this.uuid = offlinePlayer.getUniqueId();
		this.entry = offlinePlayer.getName();
	}
	
	public String getEntry() {
		return entry;
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public boolean isPlayer() {
		return Bukkit.getOfflinePlayer(uuid).getName() != null;
	}
	
	@Override
	public String getId() {
		return uuid.toString() + "@entity";
	}
	
	@Override
	public String getDisplayName() {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		String name = offlinePlayer.getName();
		if (name != null) return name;
		Entity entity = Bukkit.getEntity(uuid);
		if (entity != null) return entity.getCustomName();
		return entry;
	}
	
	@Override
	public int getPower() {
		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("power");
		return objective != null ? objective.getScore(entry).getScore() : 0;
	}
	
	@Override
	public void reloadClaimLength() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective protectedClaims = scoreboard.getObjective("protectedClaims");
		if (protectedClaims == null) protectedClaims = scoreboard.registerNewObjective("protectedClaims", "dummy", "Protected claims");
		
		protectedClaims.getScore(entry).setScore(getProtectedClaimsLength());
	}
	
	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof EntityOwner && uuid.equals(((EntityOwner) object).uuid);
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash *= 26 + uuid.hashCode();
		return hash;
	}
	
}