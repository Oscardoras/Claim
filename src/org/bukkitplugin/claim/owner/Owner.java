package org.bukkitplugin.claim.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Team;
import org.bukkitplugin.claim.claimable.ProtectedClaim;
import org.bukkitplugin.claim.rule.RuleTarget;
import org.bukkitutils.io.DataFile;

public interface Owner extends RuleTarget {
	
	public String getName();
	
	public int getMaxPower();
	
	public int getPower();
	
	default List<ProtectedClaim> getProtectedClaims() {
		List<ProtectedClaim> claims = new ArrayList<ProtectedClaim>();
		for (ProtectedClaim claim : ProtectedClaim.getProtectedClaims()) if (this.equals(claim.getOwner())) claims.add(claim);
		return claims;
	}
	
	default float getCoef() {
		int claims = getProtectedClaims().size();
		int power = getPower();
		float coef = claims != 0f ? (float) power / claims : power;
		if (coef < 0f) coef = 0f;
		else if (coef > 1f) coef = 1f;
		return coef;
	}
	
	default List<Owner> getAllies() {
		List<Owner> allies = new ArrayList<Owner>();
		YamlConfiguration config = new DataFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/allies.yml").getYML();
		if (config.contains(getId() + ".allies")) {
			for (String ally : config.getStringList(getId() + ".allies")) {
				Owner owner = getOwner(ally);
				if (owner != null) allies.add(owner);
			}
		}
		return allies;
	}
	
	default List<Owner> getAllianceProposals() {
		List<Owner> allies = new ArrayList<Owner>();
		YamlConfiguration config = new DataFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/allies.yml").getYML();
		for (String o : config.getConfigurationSection("").getKeys(false))
			if (config.contains(o + ".allies"))
				for (String ally : config.getStringList(o + ".allies"))
					if (getId().equals(ally)) {
						Owner owner = Owner.getOwner(o);
						if (owner != null) allies.add(owner);
					}
		return allies;
	}
	
	default boolean sendAllianceProposal(Owner owner) {
		DataFile file = new DataFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/allies.yml");
		YamlConfiguration config = file.getYML();
		if (!config.contains(getId() + ".allies") || !config.getStringList(getId() + ".allies").contains(owner.getId())) {
			List<String> requests = config.contains(owner.getId() + ".requests") ? config.getStringList(owner.getId() + ".requests") : new ArrayList<String>();
			if (!requests.contains(getId())) {
				requests.add(getId());
				config.set(owner.getId() + ".requests", requests);
				file.save();
				return true;
			}
		}
		return false;
	}
	
	default boolean revokeAllianceProposal(Owner owner) {
		DataFile file = new DataFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/allies.yml");
		YamlConfiguration config = file.getYML();
		if (config.contains(owner.getId() + ".requests")) {
			List<String> requests = config.getStringList(owner.getId() + ".requests");
			if (requests.contains(getId())) {
				requests.remove(getId());
				config.set(owner.getId() + ".requests", requests);
				file.save();
				return true;
			}
		}
		return false;
	}
	
	default List<Owner> getAllianceRequests() {
		List<Owner> requests = new ArrayList<Owner>();
		DataFile file = new DataFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/allies.yml");
		YamlConfiguration config = file.getYML();
		if (config.contains(getId() + ".requests")) {
			List<String> allies = config.contains(getId() + ".allies") ? config.getStringList(getId() + ".allies") : new ArrayList<String>();
			boolean removed = false;
			for (String request : config.getStringList(getId() + ".requests")) {
				if (!allies.contains(request)) {
					Owner owner = getOwner(request);
					if (owner != null) requests.add(owner);
				} else {
					allies.remove(request);
					removed = true;
				}
			}
			if (removed) {
				config.set(getId() + ".allies", allies);
				file.save();
			}
		}
		return requests;
	}
	
	default boolean acceptAllianceRequest(Owner owner) {
		DataFile file = new DataFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/allies.yml");
		YamlConfiguration config = file.getYML();
		List<Owner> requests = getAllianceRequests();
		if (requests.contains(owner)) {
			List<String> allies = config.contains(getId() + ".allies") ? config.getStringList(getId() + ".allies") : new ArrayList<String>();
			allies.add(owner.getId());
			config.set(getId() + ".allies", allies);
			
			allies = config.contains(owner.getId() + ".allies") ? config.getStringList(owner.getId() + ".allies") : new ArrayList<String>();
			allies.add(getId());
			config.set(owner.getId() + ".allies", allies);
			file.save();
			return true;
		} else return false;
	}
	
	default boolean declineAllianceRequest(Owner owner) {
		DataFile file = new DataFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/allies.yml");
		YamlConfiguration config = file.getYML();
		if (config.contains(getId() + ".requests")) {
			List<String> requests = config.getStringList(getId() + ".requests");
			if (requests.contains(owner.getId())) {
				requests.remove(owner.getId());
				config.set(getId() + ".requests", requests);
				file.save();
				return true;
			}
		}
		return false;
	}
	
	default boolean removeAlly(Owner owner) {
		DataFile file = new DataFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/allies.yml");
		YamlConfiguration config = file.getYML();
		if (config.contains(getId() + ".allies")) {
			List<String> allies = config.getStringList(getId() + ".allies");
			if (allies.contains(owner.getId())) {
				allies.remove(owner.getId());
				config.set(getId() + ".allies", allies);
				file.save();
			}
			return true;
		} else return false;
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
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
					if (offlinePlayer.getName() != null) return new EntityOwner(offlinePlayer);
				} catch (IllegalArgumentException ex) {}
				return new EntityOwner(Bukkit.getOfflinePlayer(elements[0]));
			} else if (type.equals("team")) {
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(elements[0]);
				if (team != null) return new TeamOwner(team);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
}