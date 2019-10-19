package org.bukkitplugin.claim.claimable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkitplugin.claim.ClaimPlugin;
import org.bukkitplugin.claim.owner.Owner;
import org.bukkitplugin.claim.rule.ClaimRule;
import org.bukkitplugin.claim.rule.ClaimRule.ClaimRuleNotDefinedException;
import org.bukkitplugin.claim.rule.RuleTarget;
import org.bukkitutils.io.ConfigurationFile;

public class ProtectedClaim extends Claim {
	
	protected ProtectedClaim(Chunk chunk, ConfigurationFile config, Owner owner) {
		super(chunk, config, owner);
	}
	
	public void unProtect() {
		config.set(chunk.getX() + "." + chunk.getZ() + ".protected", false);
		config.set(chunk.getX() + "." + chunk.getZ() + ".name", null);
		config.set(chunk.getX() + "." + chunk.getZ() + ".claim_rules", null);
		config.save();
	}
	
	public String getName() {
		return config.getString(chunk.getX() + "." + chunk.getZ() + ".name");
	}
	
	public void setName(String name) {
		dispatchName(name);
		config.save();
	}
	
	protected void dispatchName(String name) {
		config.set(chunk.getX() + "." + chunk.getZ() + ".name", name);
		
		World world = chunk.getWorld();
		int x = chunk.getX();
		int z = chunk.getZ();
		Chunk[] chunks = new Chunk[4];
		chunks[0] = world.getChunkAt(x + 1, z);
		chunks[1] = world.getChunkAt(x - 1, z);
		chunks[2] = world.getChunkAt(x, z + 1);
		chunks[3] = world.getChunkAt(x, z - 1);
		for (Chunk chunk : chunks) {
			Claimable claimable = Claimable.get(chunk);
			if (claimable instanceof ProtectedClaim && ((ProtectedClaim) claimable).owner.equals(owner))
				if (!((ProtectedClaim) claimable).getName().equals(name)) ((ProtectedClaim) claimable).dispatchName(name);
		}
	}
	
	public boolean getClaimRuleValue(ClaimRule rule, RuleTarget target) throws ClaimRuleNotDefinedException {
		if (config.contains(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule.name() + "." + target.getId()))
			return config.getBoolean(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule.name() + "." + target.getId());
		else throw new ClaimRuleNotDefinedException();
	}
	
	public Map<RuleTarget, Boolean> getClaimRuleValues(ClaimRule rule) {
		Map<RuleTarget, Boolean> claimRulesValues = new HashMap<RuleTarget, Boolean>();
		if (config.contains(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule.name()))
			for (String id : config.getConfigurationSection(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule.name()).getKeys(false)) {
				RuleTarget ruleTarget = RuleTarget.getRuleTarget(id);
				if (ruleTarget != null) claimRulesValues.put(ruleTarget, config.getBoolean(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule.name() + "." + id));
			}
		return claimRulesValues;
	}
	
	public void setClaimRuleValue(ClaimRule rule, RuleTarget target, boolean value) {
		config.set(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule.name() + "." + target.getId(), value);
		config.save();
	}
	
	public void removeClaimRuleValue(ClaimRule rule, RuleTarget target) {
		config.set(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule.name() + "." + target.getId(), null);
		config.save();
	}
	
	@Override
	public boolean canExplode() {
		try {
			return getClaimRuleValue(ClaimRule.build, RuleTarget.NEUTRALS);
		} catch (ClaimRuleNotDefinedException ex) {
			return false;
		}
	}
	
	@Override
	public boolean canBurn() {
		try {
			return owner.getCoef() < ClaimPlugin.plugin.coefs.burn || getClaimRuleValue(ClaimRule.build, RuleTarget.NEUTRALS);
		} catch (ClaimRuleNotDefinedException ex) {
			return false;
		}
	}
	
	@Override
	public boolean canUseBuckets(Entity entity) {
		if (isOwner(entity)) return true;
		else if (entity.hasPermission("claim.ignore")) return true;
		else if (ClaimRule.getClaimRuleValue(this, ClaimRule.build, entity)) return true;
		else if (owner.getCoef() < ClaimPlugin.plugin.coefs.useBuckets) return true;
		return false;
	}
	
	@Override
	public boolean canBuild(Entity entity) {
		if (isOwner(entity)) return true;
		else if (entity.hasPermission("claim.ignore")) return true;
		else if (ClaimRule.getClaimRuleValue(this, ClaimRule.build, entity)) return true;
		else if (owner.getCoef() < ClaimPlugin.plugin.coefs.build) return true;
		return false;
	}
	
	@Override
	public boolean canOpenChests(Entity entity) {
		if (isOwner(entity)) return true;
		else if (entity.hasPermission("claim.ignore")) return true;
		else if (ClaimRule.getClaimRuleValue(this, ClaimRule.openChests, entity)) return true;
		else if (owner.getCoef() < ClaimPlugin.plugin.coefs.openChests) return true;
		return false;
	}
	
	@Override
	public boolean canOpenDoors(Entity entity) {
		if (isOwner(entity)) return true;
		else if (entity.hasPermission("claim.ignore")) return true;
		else if (ClaimRule.getClaimRuleValue(this, ClaimRule.openDoors, entity)) return true;
		else if (owner.getCoef() < ClaimPlugin.plugin.coefs.openDoors) return true;
		return false;
	}
	
	
	public static List<ProtectedClaim> getProtectedClaims(Owner owner, String name) {
		List<ProtectedClaim> protectedClaims = new ArrayList<ProtectedClaim>();
		for (ProtectedClaim protectedClaim : getProtectedClaims()) {
			if (owner.equals(protectedClaim.getOwner()) && protectedClaim.getName().equals(name)) protectedClaims.add(protectedClaim);
		}
		return protectedClaims;
	}
	
	public static List<ProtectedClaim> getProtectedClaims(World world) {
		List<ProtectedClaim> protectedClaims = new ArrayList<ProtectedClaim>();
		ConfigurationFile config = new ConfigurationFile(world.getWorldFolder().getPath() + "/data/claims.yml");
		for (String x : config.getConfigurationSection("").getKeys(false)) {
			for (String z : config.getConfigurationSection(x).getKeys(false)) {
				Claimable claimable = Claimable.get((world.getChunkAt(Integer.parseInt(x), Integer.parseInt(z))));
				if (claimable instanceof ProtectedClaim) protectedClaims.add((ProtectedClaim) claimable);
			}
		}
		return protectedClaims;
	}
	
	public static List<ProtectedClaim> getProtectedClaims() {
		List<ProtectedClaim> protectedClaims = new ArrayList<ProtectedClaim>();
		for (World world : Bukkit.getWorlds()) protectedClaims.addAll(getProtectedClaims(world));
		return protectedClaims;
	}
	
	public static int getProtectedClaimsLength(Owner owner) {
		String id = owner.getId();
		int length = 0;
		for (World world : Bukkit.getWorlds()) {
			ConfigurationFile config = new ConfigurationFile(world.getWorldFolder().getPath() + "/data/claims.yml");
			for (String x : config.getConfigurationSection("").getKeys(false))
				for (String z : config.getConfigurationSection(x).getKeys(false)) {
					ConfigurationSection section = config.getConfigurationSection(x + "." + z);
					if (section.getBoolean("protected", false) && section.contains("owner") && section.getString("owner").equals(id)) length++;
				}
		}
		return length;
	}
	
}