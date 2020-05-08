package org.bukkitplugin.claim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkitplugin.claim.claimable.Claim;
import org.bukkitplugin.claim.claimable.Claimable;
import org.bukkitplugin.claim.claimable.ProtectedClaim;
import org.bukkitplugin.claim.command.ClaimCommand;
import org.bukkitplugin.claim.owner.EntityOwner;
import org.bukkitplugin.claim.owner.Owner;
import org.bukkitplugin.claim.owner.TeamOwner;
import org.bukkitutils.BukkitPlugin;
import org.bukkitutils.PlayerReloader;
import org.bukkitutils.PlayerReloader.Type;

public class ClaimPlugin extends BukkitPlugin implements Listener {
	
	public static ClaimPlugin plugin;
	
	public ClaimPlugin() {
		plugin = this;
	}
	
	
	public static class Coefs {
		public float burn;
		public float useBuckets;
		public float openChests;
		public float openDoors;
		public float build;
		public float steal;
	}
	public Coefs coefs;
	public int maxClaimDistance;
	public int offlineTime;
	protected final Map<Player, Chunk> playersChunks = new HashMap<Player, Chunk>();
	private final Map<OfflinePlayer, Integer> toRemove = new HashMap<OfflinePlayer, Integer>();
	
	public void onLoad() {
		ClaimCommand.list();
		ClaimCommand.claim();
		ClaimCommand.unclaim();
		ClaimCommand.protect();
		ClaimCommand.unprotect();
		ClaimCommand.name();
		ClaimCommand.rule();
	}
	
	public void onEnable() {
		saveDefaultConfig();
		
		coefs = new Coefs();
		coefs.burn = (float) getConfig().getDouble("coefs.burn");
		coefs.useBuckets = (float) getConfig().getDouble("coefs.use_buckets");
		coefs.openChests = (float) getConfig().getDouble("coefs.open_chests");
		coefs.openDoors = (float) getConfig().getDouble("coefs.open_doors");
		coefs.build = (float) getConfig().getDouble("coefs.build");
		coefs.steal = (float) getConfig().getDouble("coefs.steal");
		
		maxClaimDistance = getConfig().getInt("max_claim_distance");
		offlineTime = getConfig().getInt("offline_time");
		
		
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new Listeners(), this);
		
		PlayerReloader.register(this, (player, location, type) -> {
			if (type != Type.QUIT) {
				Chunk chunk = location.getChunk();
				Claimable claimable = Claimable.get(chunk);
				playersChunks.putIfAbsent(player, chunk);
				Claimable oldClaimable = Claimable.get(playersChunks.get(player));
				if (!claimable.equals(oldClaimable)) {
					playersChunks.put(player, claimable.getChunk());
					if (claimable instanceof Claim) {
						Owner owner = ((Claim) claimable).getOwner();
						String name;
						if (claimable instanceof ProtectedClaim) name = ((ProtectedClaim) claimable).getName();
						else name = "";
						
						Owner oldOwner;
						String oldName;
						if (oldClaimable != null && oldClaimable instanceof Claim) {
							oldOwner = ((Claim) oldClaimable).getOwner();
							if (oldClaimable instanceof ProtectedClaim) oldName = ((ProtectedClaim) oldClaimable).getName();
							else oldName = "";
						} else {
							oldOwner = null;
							oldName = "";
						}
						
						if (!owner.equals(oldOwner) || !name.equals(oldName)) {
							player.sendTitle(name, new Message("territory.owner", owner.getDisplayName()).getMessage(player), 1, 60, 10);
						}
					} else if (oldClaimable instanceof Claim) player.sendTitle("", new Message("territory.free").getMessage(player), 1, 20, 10);
				}
			} else playersChunks.remove(player);
		}, 0L);
		
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
			
			Objective power = scoreboard.getObjective("power");
			Objective protectedClaims = scoreboard.getObjective("protectedClaims");
			if (power == null) power = scoreboard.registerNewObjective("power", "dummy", "Power");
			if (protectedClaims == null) protectedClaims = scoreboard.registerNewObjective("protectedClaims", "dummy", "Protected claims");
			
			Objective tPower = scoreboard.getObjective("tPower");
			Objective tProtectedClaims = scoreboard.getObjective("tProtectedClaims");
			if (tPower == null) tPower = scoreboard.registerNewObjective("tPower", "dummy", "Team power");
			if (tProtectedClaims == null) tProtectedClaims = scoreboard.registerNewObjective("tProtectedClaims", "dummy", "Team protected claims");
			
			List<String> e = new ArrayList<String>();
			for (Team team : scoreboard.getTeams()) {
				int p = 0;
				Set<String> entries = team.getEntries();
				if (!entries.isEmpty()) {
					int l = new TeamOwner(team).getProtectedClaimsLength();
					for (String entry : entries) {
						int s = power.getScore(entry).getScore();
						if (s > 0) p += s;
					}
					for (String entry : entries) {
						tPower.getScore(entry).setScore(p);
						tProtectedClaims.getScore(entry).setScore(l);
					}
				}
				e.addAll(entries);
			}
			for (String entry : scoreboard.getEntries()) {
				if (!e.contains(entry)) {
					Score tPowerScore = tPower.getScore(entry);
					if (tPowerScore.isScoreSet()) tPowerScore.setScore(0);
					Score tProtectedClaimsScore = tProtectedClaims.getScore(entry);
					if (tProtectedClaimsScore.isScoreSet()) tProtectedClaimsScore.setScore(0);
				}
			}
			
			if (offlineTime > 0) {
				Objective claimOfflineTime = scoreboard.getObjective("claimOfflineTime");
				if (claimOfflineTime == null) claimOfflineTime = scoreboard.registerNewObjective("claimOfflineTime", "dummy", "claimOfflineTime");
				
				synchronized(toRemove) {
					for (Entry<OfflinePlayer, Integer> entry : toRemove.entrySet()) {
						String name = entry.getKey().getName();
						int value = entry.getValue();
						Score score = claimOfflineTime.getScore(name);
						
						if (value == -1) {
							if (score.getScore() != 0) score.setScore(0);
						} else {
							int s = score.getScore() + value;
							if (s >= offlineTime) {
								score.setScore(s - offlineTime);
								Score powerScore = power.getScore(name);
								int powerValue = powerScore.getScore();
								if (powerValue > 0) powerScore.setScore(powerValue - 1);
							} else score.setScore(s);
							entry.setValue(0);
						}
					}
				}
			}
		}, 0l, 20l);
		
		new Timer().schedule(new TimerTask() {
			public void run() {
				synchronized(toRemove) {
					for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
						if (!offlinePlayer.isOnline()) {
							int value = toRemove.containsKey(offlinePlayer) ? toRemove.get(offlinePlayer) : 0;
							toRemove.put(offlinePlayer, value + 1);
						} else toRemove.put(offlinePlayer, -1);
					}
			    }
			}
		}, 0L, 50L);
		
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		for (String entry : scoreboard.getEntries()) new EntityOwner(entry).reloadClaimLength();
		for (Team team : scoreboard.getTeams()) new TeamOwner(team).reloadClaimLength();
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		for (ProtectedClaim protectedClaim : ProtectedClaim.getProtectedClaims(e.getWorld())) protectedClaim.getChunk().load(false);
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		Chunk chunk = e.getChunk();
		if (Claimable.get(chunk) instanceof ProtectedClaim) chunk.load(false);
	}
	
}