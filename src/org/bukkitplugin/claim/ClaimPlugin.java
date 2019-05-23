package org.bukkitplugin.claim;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkitplugin.claim.claimable.Claim;
import org.bukkitplugin.claim.claimable.Claimable;
import org.bukkitplugin.claim.claimable.ProtectedClaim;
import org.bukkitplugin.claim.command.ClaimCommand;
import org.bukkitplugin.claim.owner.EntityOwner;
import org.bukkitplugin.claim.owner.Owner;
import org.bukkitplugin.claim.owner.TeamOwner;
import org.bukkitutils.BukkitPlugin;
import org.bukkitutils.Notification;
import org.bukkitutils.event.PlayerReload;
import org.bukkitutils.io.Translate;

public class ClaimPlugin extends BukkitPlugin {
	
	public static ClaimPlugin plugin;
	
	public ClaimPlugin() {
		plugin = this;
	}
	
	
	protected final Map<Player, Chunk> playersChunks = new HashMap<Player, Chunk>();
	
	public Map<String, Integer> maxPower = new HashMap<String, Integer>();
	public class Coefs {
		public float burn;
		public float useBuckets;
		public float openContainers;
		public float openDoors;
		public float build;
		public float steal;
	}
	public Coefs coefs;
	public int maxClaimDistance;
	
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
		if (!getConfig().contains("max_power.org_bukkit_entity_Player")) getConfig().set("max_power.org_bukkit_entity_Player", 10);
		if (!getConfig().contains("max_power.org_bukkit_entity_Villager")) getConfig().set("max_power.org_bukkit_entity_Villager", 5);
		if (!getConfig().contains("max_power.org_bukkit_entity_Monster")) getConfig().set("max_power.org_bukkit_entity_Monster", 2);
		if (!getConfig().contains("max_power.org_bukkit_entity_Wolf")) getConfig().set("max_power.org_bukkit_entity_Wolf", 5);
		if (!getConfig().contains("max_power.org_bukkit_entity_IronGolem")) getConfig().set("max_power.org_bukkit_entity_IronGolem", 10);
		
		if (!getConfig().contains("coefs.burn")) getConfig().set("coefs.burn", 0.8f);
		if (!getConfig().contains("coefs.use_buckets")) getConfig().set("coefs.use_buckets", 0.8f);
		if (!getConfig().contains("coefs.open_containers")) getConfig().set("coefs.open_containers", 0.7f);
		if (!getConfig().contains("coefs.open_doors")) getConfig().set("coefs.open_doors", 0.7f);
		if (!getConfig().contains("coefs.build")) getConfig().set("coefs.build", 0.6f);
		if (!getConfig().contains("coefs.steal")) getConfig().set("coefs.steal", 0.5f);
		
		if (!getConfig().contains("max_claim_distance")) getConfig().set("max_claim_distance", 16);
		saveConfig();
		
		
		for (String type : getConfig().getConfigurationSection("max_power").getKeys(false)) {
			maxPower.put(type.replaceAll("_", "."), getConfig().getInt("max_power." + type));
		}
		if (!maxPower.containsKey("org.bukkit.entity.Player")) maxPower.put("org.bukkit.entity.Player", 10);
		
		coefs = new Coefs();
		coefs.burn = (float) getConfig().getDouble("coefs.burn");
		coefs.useBuckets = (float) getConfig().getDouble("coefs.use_buckets");
		coefs.openContainers = (float) getConfig().getDouble("coefs.open_containers");
		coefs.openDoors = (float) getConfig().getDouble("coefs.open_doors");
		coefs.build = (float) getConfig().getDouble("coefs.build");
		coefs.steal = (float) getConfig().getDouble("coefs.steal");
		
		maxClaimDistance = getConfig().getInt("max_claim_distance");
		
		
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new Events(), this);
		Notification.registerPlugin(this);
		
		new PlayerReload(this) {
			public void onReload(Player player, Type type) {
				Claimable claimable = Claimable.get(player.getLocation().getChunk());
				playersChunks.putIfAbsent(player, player.getLocation().getChunk());
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
							player.sendTitle(name, Translate.getPluginMessage(player, new Message("territory.owner"), owner.getName()), 1, 60, 10);
						}
					} else if (oldClaimable instanceof Claim) player.sendTitle("", Translate.getPluginMessage(player, new Message("territory.free")), 1, 20, 10);
				}
			}
		};
		
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				recalculateScores();
			}
		}, 0l, 1l);
	}
	
	public void recalculateScores() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		
		Objective power = scoreboard.getObjective("power");
		Objective protectedClaims = scoreboard.getObjective("protectedClaims");
		Objective maxPower = scoreboard.getObjective("maxPower");
		if (power == null) power = scoreboard.registerNewObjective("power", "dummy", "Power");
		if (protectedClaims == null) protectedClaims = scoreboard.registerNewObjective("protectedClaims", "dummy", "Protected claims");
		if (maxPower == null) maxPower = scoreboard.registerNewObjective("maxPower", "dummy", "Max power");
		
		for (String entry : scoreboard.getEntries()) {
			EntityOwner entityOwner = new EntityOwner(entry);
			protectedClaims.getScore(entry).setScore(entityOwner.getProtectedClaims().size());
			
			Score maxScore = maxPower.getScore(entry);
			if (!maxScore.isScoreSet()) {
				try {
					Entity entity = Bukkit.getEntity(entityOwner.getUUID());
					if (entity != null) {
						for (String type : ClaimPlugin.plugin.maxPower.keySet()) {
							try {
								if (Class.forName(type).isInstance(entity)) maxScore.setScore(ClaimPlugin.plugin.maxPower.get(type));
							} catch (ClassNotFoundException ex) {}
						}
					}
				} catch (IllegalArgumentException ex) {}
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entityOwner.getUUID());
				if (offlinePlayer.isOnline() || offlinePlayer.hasPlayedBefore() || !maxScore.isScoreSet())
					maxScore.setScore(ClaimPlugin.plugin.maxPower.get("org.bukkit.entity.Player"));
			}
			
			Score score = power.getScore(entry);
			if (!score.isScoreSet()) score.setScore(maxScore.getScore());
		}
	}
	
	public void sendNotification(Owner owner, Message message, String... args) {
		if (owner instanceof EntityOwner) sendNotification((EntityOwner) owner, message, args);
		else if (owner instanceof TeamOwner) Notification.send(((TeamOwner) owner).getTeam(), message, args);
	}
	
	private void sendNotification(EntityOwner owner, Message message, String... args) {
		UUID uuid = ((EntityOwner) owner).getUUID();
		Entity entity = Bukkit.getEntity(uuid);
		if (entity != null) entity.sendMessage(Translate.getPluginMessage(entity, message, args));
		else Notification.send(Bukkit.getOfflinePlayer(uuid), message, args);
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