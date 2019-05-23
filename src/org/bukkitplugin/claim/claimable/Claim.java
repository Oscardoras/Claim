package org.bukkitplugin.claim.claimable;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkitplugin.claim.ClaimPlugin;
import org.bukkitplugin.claim.owner.EntityOwner;
import org.bukkitplugin.claim.owner.Owner;
import org.bukkitplugin.claim.owner.TeamOwner;
import org.bukkitutils.io.DataFile;

public class Claim extends Claimable {
	
	protected final Owner owner;
	
	protected Claim(Chunk chunk, DataFile file, YamlConfiguration config, Owner owner) {
		super(chunk, file, config);
		this.owner = owner;
	}
	
	public Owner getOwner() {
		return owner;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public boolean isOwner(Entity entity) {
		if (owner instanceof EntityOwner) return owner.equals(entity);
		if (owner instanceof TeamOwner) return owner.equals(Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(new EntityOwner(entity).getEntry()));
		return false;
	}
	
	public void unClaim() {
		config.set(chunk.getX() + "." + chunk.getZ(), null);
		if (config.getConfigurationSection("" + chunk.getX()).getKeys(false).isEmpty()) config.set("" + chunk.getX(), null);
		file.save();
	}
	
	@Override
	public float getCoef() {
		return owner.getCoef();
	}
	
	public boolean canBeStolen() {
		return owner.getPower() <= ClaimPlugin.plugin.coefs.steal;
	}
	
	
	public static List<Claim> getClaims(World world) {
		List<Claim> claims = new ArrayList<Claim>();
		YamlConfiguration config = new DataFile(world.getWorldFolder().getPath() + "/data/claims.yml").getYML();
		for (String x : config.getConfigurationSection("").getKeys(false)) {
			for (String z : config.getConfigurationSection(x).getKeys(false)) {
				Claimable claimable = Claimable.get((world.getChunkAt(Integer.parseInt(x), Integer.parseInt(z))));
				if (claimable instanceof Claim) claims.add((Claim) claimable);
			}
		}
		return claims;
	}
	
	public static List<Claim> getClaims() {
		List<Claim> claims = new ArrayList<Claim>();
		for (World world : Bukkit.getWorlds()) claims.addAll(getClaims(world));
		return claims;
	}
	
}