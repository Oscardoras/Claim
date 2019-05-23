package org.bukkitplugin.claim.owner;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkitplugin.claim.ClaimPlugin;

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
		this.entry = entity.getName();
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
	public String getName() {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		String name = offlinePlayer.getName();
		if (name != null) return name;
		Entity entity = Bukkit.getEntity(uuid);
		if (entity != null) return entity.getCustomName();
		return uuid.toString();
	}
	
	@Override
	public int getMaxPower() {
		ClaimPlugin.plugin.recalculateScores();
		return Bukkit.getScoreboardManager().getMainScoreboard().getObjective("maxPower").getScore(entry).getScore();
	}
	
	@Override
	public int getPower() {
		ClaimPlugin.plugin.recalculateScores();
		return Bukkit.getScoreboardManager().getMainScoreboard().getObjective("power").getScore(entry).getScore();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null) {
			if (object instanceof EntityOwner) return uuid.equals(((EntityOwner) object).uuid);
			if (object instanceof Entity) return uuid.equals(((Entity) object).getUniqueId());
			if (object instanceof OfflinePlayer) return uuid.equals(((OfflinePlayer) object).getUniqueId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
	
}