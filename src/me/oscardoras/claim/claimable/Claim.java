package me.oscardoras.claim.claimable;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import me.oscardoras.claim.ClaimPlugin;
import me.oscardoras.claim.owner.EntityOwner;
import me.oscardoras.claim.owner.Owner;
import me.oscardoras.claim.owner.TeamOwner;
import me.oscardoras.spigotutils.io.ConfigurationFile;

public class Claim extends Claimable {
	
	protected final Owner owner;
	
	protected Claim(Chunk chunk, ConfigurationFile config, Owner owner) {
		super(chunk, config);
		this.owner = owner;
	}
	
	public Owner getOwner() {
		return owner;
	}
	
	@Override
	public boolean isOwner(Entity entity) {
		if (owner instanceof EntityOwner) return owner.equals(new EntityOwner(entity));
		if (owner instanceof TeamOwner) return owner.equals(new TeamOwner(Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(new EntityOwner(entity).getEntry())));
		return false;
	}
	
	public void unClaim() {
		super.unClaim();
		
		owner.reloadClaimLength();
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
		ConfigurationFile config = getConfig(world);
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