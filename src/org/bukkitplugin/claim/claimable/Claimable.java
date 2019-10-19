package org.bukkitplugin.claim.claimable;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkitplugin.claim.ClaimPlugin;
import org.bukkitplugin.claim.owner.Owner;
import org.bukkitutils.io.ConfigurationFile;

public class Claimable {
	
	protected final Chunk chunk;
	protected final ConfigurationFile config;
	
	protected Claimable(Chunk chunk, ConfigurationFile config) {
		this.chunk = chunk;
		this.config = config;
	}
	
	public Chunk getChunk() {
		return chunk;
	}
	
	public boolean isOwner(Entity entity) {
		return false;
	}
	
	public Claim claim(Owner owner) {
		config.set(chunk.getX() + "." + chunk.getZ() + ".owner", owner.getId());
		config.set(chunk.getX() + "." + chunk.getZ() + ".protected", false);
		config.set(chunk.getX() + "." + chunk.getZ() + ".claim_rules", null);
		config.save();
		return new Claim(chunk, config, owner);
	}
	
	public ProtectedClaim protect(Owner owner) {
		config.set(chunk.getX() + "." + chunk.getZ() + ".owner", owner.getId());
		config.set(chunk.getX() + "." + chunk.getZ() + ".protected", true);
		
		ProtectedClaim protectedClaim = new ProtectedClaim(chunk, config, owner);
		
		String name = null;
		ConfigurationSection section = null;
		World world = chunk.getWorld();
		int x = chunk.getX();
		int z = chunk.getZ();
		Chunk[] chunks = new Chunk[4];
		chunks[0] = world.getChunkAt(x + 1, z);
		chunks[1] = world.getChunkAt(x - 1, z);
		chunks[2] = world.getChunkAt(x, z + 1);
		chunks[3] = world.getChunkAt(x, z - 1);
		for (Chunk c : chunks) {
			Claimable claimable = Claimable.get(c);
			if (claimable instanceof ProtectedClaim && ((ProtectedClaim) claimable).owner.equals(owner)) {
				name = config.getString(c.getX() + "." + c.getZ() + ".name");
				section = config.getConfigurationSection(c.getX() + "." + c.getZ() + ".claim_rules");
				break;
			}
		}
		if (!config.contains(chunk.getX() + "." + chunk.getZ() + ".name")) {
			if (name != null) protectedClaim.setName(name);
			else protectedClaim.setName(owner.getName());
		}
		config.set(chunk.getX() + "." + chunk.getZ() + ".claim_rules", section);
			
		config.save();
		return protectedClaim;
	}
	
	protected void unClaim() {
		config.set(chunk.getX() + "." + chunk.getZ(), null);
		if (config.getConfigurationSection("" + chunk.getX()).getKeys(false).isEmpty()) config.set("" + chunk.getX(), null);
		config.save();
	}
	
	public float getCoef() {
		return 0;
	}
	
	public boolean canExplode() {
		return true;
	}
	
	public boolean canBurn() {
		return true;
	}
	
	public boolean canUseBuckets(Entity entity) {
		return true;
	}
	
	public boolean canBuild(Entity entity) {
		return true;
	}
	
	public boolean canOpenChests(Entity entity) {
		return true;
	}
	
	public boolean canOpenDoors(Entity entity) {
		return true;
	}
	
	public boolean checkClaim(Owner owner) {
		String id = owner.getId();
		ConfigurationFile config = new ConfigurationFile(chunk.getWorld().getWorldFolder().getPath() + "/data/claims.yml");
		
		int maxClaimDistance = ClaimPlugin.plugin.maxClaimDistance;
		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		int maxX = chunkX + maxClaimDistance;
		int maxZ = chunkZ + maxClaimDistance;
		
		for (int x = chunkX - maxClaimDistance; x < maxX; x++)
			for (int z = chunkZ - maxClaimDistance; z < maxZ; z++)
				if (config.getBoolean(x + "." + z + ".protected", false))
					if (config.contains(x + "." + z + ".owner") && id.equals(config.getString(x + "." + z + ".owner")))
						return true;
		return false;
	}
	
	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof Claimable && chunk.equals(((Claimable) object).chunk);
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash *= 14 + chunk.hashCode();
		return hash;
	}
	
	
	public static Claimable get(Chunk chunk) {
		ConfigurationFile config = new ConfigurationFile(chunk.getWorld().getWorldFolder().getPath() + "/data/claims.yml");
		if (config.contains(chunk.getX() + "." + chunk.getZ() + ".owner")) {
			Owner owner = Owner.getOwner(config.getString(chunk.getX() + "." + chunk.getZ() + ".owner"));
			if (owner != null) {
				if (config.getBoolean(chunk.getX() + "." + chunk.getZ() + ".protected", false)) return new ProtectedClaim(chunk, config, owner);
				Claim claim = new Claim(chunk, config, owner);
				if (claim.checkClaim(owner)) return claim;
				else claim.unClaim();
			} else {
				Claimable claimable = new Claimable(chunk, config);
				claimable.unClaim();
				return claimable;
			}
		}
		return new Claimable(chunk, config);
	}
	
}