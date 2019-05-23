package org.bukkitplugin.claim.claimable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkitplugin.claim.ClaimPlugin;
import org.bukkitplugin.claim.owner.Owner;
import org.bukkitplugin.claim.rule.ClaimRule;
import org.bukkitplugin.claim.rule.ClaimRuleNotDefinedException;
import org.bukkitplugin.claim.rule.RuleTarget;
import org.bukkitutils.io.DataFile;

public class ProtectedClaim extends Claim {
	
	protected ProtectedClaim(Chunk chunk, DataFile file, YamlConfiguration config, Owner owner) {
		super(chunk, file, config, owner);
	}
	
	public void unProtect() {
		config.set(chunk.getX() + "." + chunk.getZ() + ".protected", false);
		config.set(chunk.getX() + "." + chunk.getZ() + ".name", null);
		config.set(chunk.getX() + "." + chunk.getZ() + ".claim_rules", null);
		file.save();
	}
	
	public String getName() {
		return config.getString(chunk.getX() + "." + chunk.getZ() + ".name");
	}
	
	public void setName(String name) {
		dispatchName(name);
		file.save();
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
			if (claimable instanceof ProtectedClaim && ((ProtectedClaim) claimable).owner.equals(owner)) {
				if (!((ProtectedClaim) claimable).getName().equals(name)) ((ProtectedClaim) claimable).dispatchName(name);
			}
		}
	}
	
	public boolean getClaimRuleValue(ClaimRule rule, RuleTarget target) throws ClaimRuleNotDefinedException {
		if (config.contains(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule + "." + target.getId())) {
			return config.getBoolean(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule + "." + target.getId());
		} else throw new ClaimRuleNotDefinedException();
	}
	
	public Map<RuleTarget, Boolean> getClaimRuleValues(ClaimRule rule) {
		Map<RuleTarget, Boolean> claimRulesValues = new HashMap<RuleTarget, Boolean>();
		if (config.contains(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule)) {
			for (String id : config.getConfigurationSection(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule).getKeys(false)) {
				RuleTarget ruleTarget = RuleTarget.getRuleTarget(id);
				if (ruleTarget != null) claimRulesValues.put(ruleTarget, config.getBoolean(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule + "." + id));
			}
		}
		return claimRulesValues;
	}
	
	public void setClaimRuleValue(ClaimRule rule, RuleTarget target, boolean value) {
		config.set(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule + "." + target.getId(), value);
		file.save();
	}
	
	public void removeClaimRuleValue(ClaimRule rule, RuleTarget target) {
		config.set(chunk.getX() + "." + chunk.getZ() + ".claim_rules." + rule + "." + target.getId(), null);
		file.save();
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
			return getClaimRuleValue(ClaimRule.build, RuleTarget.NEUTRALS) || owner.getCoef() <= ClaimPlugin.plugin.coefs.useBuckets;
		} catch (ClaimRuleNotDefinedException ex) {
			return false;
		}
	}
	
	@Override
	public boolean canUseBuckets(Entity entity) {
		if (isOwner(entity)) return true;
		else if (entity.hasPermission("claim.ignore")) return true;
		else if (ClaimRule.getClaimRuleValue(this, ClaimRule.build, entity)) return true;
		else if (owner.getCoef() <= ClaimPlugin.plugin.coefs.useBuckets) return true;
		return false;
	}
	
	@Override
	public boolean canBuild(Entity entity) {
		if (isOwner(entity)) return true;
		else if (entity.hasPermission("claim.ignore")) return true;
		else if (ClaimRule.getClaimRuleValue(this, ClaimRule.build, entity)) return true;
		else if (owner.getCoef() <= ClaimPlugin.plugin.coefs.build) return true;
		return false;
	}
	
	@Override
	public boolean canOpenContainers(Entity entity) {
		if (isOwner(entity)) return true;
		else if (entity.hasPermission("claim.ignore")) return true;
		else if (ClaimRule.getClaimRuleValue(this, ClaimRule.openContainers, entity)) return true;
		else if (owner.getCoef() <= ClaimPlugin.plugin.coefs.openContainers) return true;
		return false;
	}
	
	@Override
	public boolean canOpenDoors(Entity entity) {
		if (isOwner(entity)) return true;
		else if (entity.hasPermission("claim.ignore")) return true;
		else if (ClaimRule.getClaimRuleValue(this, ClaimRule.openDoors, entity)) return true;
		else if (owner.getCoef() <= ClaimPlugin.plugin.coefs.openDoors) return true;
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
		YamlConfiguration config = new DataFile(world.getWorldFolder().getPath() + "/data/claims.yml").getYML();
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
	
}