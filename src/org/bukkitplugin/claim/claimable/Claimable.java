package org.bukkitplugin.claim.claimable;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkitplugin.claim.ClaimPlugin;
import org.bukkitplugin.claim.owner.Owner;
import org.bukkitutils.io.DataFile;

public class Claimable {
	
	protected final DataFile file;
	protected final YamlConfiguration config;
	protected final Chunk chunk;
	
	protected Claimable(Chunk chunk, DataFile file, YamlConfiguration config) {
		this.chunk = chunk;
		this.file = file;
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
		file.save();
		return new Claim(chunk, file, config, owner);
	}
	
	public ProtectedClaim protect(Owner owner) {
		config.set(chunk.getX() + "." + chunk.getZ() + ".owner", owner.getId());
		config.set(chunk.getX() + "." + chunk.getZ() + ".protected", true);
		
		ProtectedClaim protectedClaim = new ProtectedClaim(chunk, file, config, owner);
		
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
			
		file.save();
		return protectedClaim;
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
	
	public boolean canOpenContainers(Entity entity) {
		return true;
	}
	
	public boolean canOpenDoors(Entity entity) {
		return true;
	}
	
	public boolean checkClaim(Owner owner) {
		int maxClaimDistance = ClaimPlugin.plugin.maxClaimDistance;
		World world = chunk.getWorld();
		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		int maxX = chunkX + maxClaimDistance;
		int maxZ = chunkZ + maxClaimDistance;
		
		for (int x = chunkX - maxClaimDistance; x < maxX; x++) {
			for (int z = chunkZ - maxClaimDistance; z < maxZ; z++) {
				Claimable claimable = Claimable.get(world.getChunkAt(x, z));
				if (claimable instanceof ProtectedClaim && ((ProtectedClaim) claimable).owner.equals(owner)) return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null) {
			if (object instanceof Claimable) return chunk.equals(((Claimable) object).chunk);
			if (object instanceof Chunk) return chunk.equals(((Chunk) object));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return chunk.hashCode();
	}
	
	
	public static Claimable get(Chunk chunk) {
		DataFile file = new DataFile(chunk.getWorld().getWorldFolder().getPath() + "/data/claims.yml");
		YamlConfiguration config = file.getYML();
		if (config.contains(chunk.getX() + "." + chunk.getZ() + ".owner")) {
			Owner owner = Owner.getOwner(config.getString(chunk.getX() + "." + chunk.getZ() + ".owner"));
			if (owner != null) {
				if (config.getBoolean(chunk.getX() + "." + chunk.getZ() + ".protected", false)) return new ProtectedClaim(chunk, file, config, owner);
				Claim claim = new Claim(chunk, file, config, owner);
				if (claim.checkClaim(owner)) return claim;
				else claim.unClaim();
			}
		}
		return new Claimable(chunk, file, config);
	}
	
}