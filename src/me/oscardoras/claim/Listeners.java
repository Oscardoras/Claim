package me.oscardoras.claim;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.oscardoras.claim.claimable.Claimable;

public class Listeners implements Listener {
	
	public static boolean isChest(Material material) {
		return material == Material.CHEST;
	}
	
	public static boolean isDoor(Material material) {
		return material.name().toLowerCase().endsWith("_door");
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(BlockExplodeEvent e) {
		Location center = e.getBlock().getLocation();
		Claimable claimable = Claimable.get(center.getChunk());
		if (!claimable.canExplode()) {
			List<Block> list = e.blockList();
			List<Block> blocks = new ArrayList<Block>();
			blocks.addAll(list);
			double distance = 0;
			for (Block block : blocks) {
				double d = center.distance(block.getLocation());
				if (d > distance) distance = d;
			}
			distance *= (1 - claimable.getCoef());
			for (Block block : blocks) {
				double d = center.distance(block.getLocation());
				if (d > distance) list.remove(block);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(EntityExplodeEvent e) {
		Location center = e.getLocation();
		Claimable claimable = Claimable.get(center.getChunk());
		if (!claimable.canExplode()) {
			List<Block> list = e.blockList();
			List<Block> blocks = new ArrayList<Block>();
			blocks.addAll(list);
			double distance = 0;
			for (Block block : blocks) {
				double d = center.distance(block.getLocation());
				if (d > distance) distance = d;
			}
			distance *= (1 - claimable.getCoef());
			for (Block block : blocks) {
				double d = center.distance(block.getLocation());
				if (d > distance) list.remove(block);
			}
		}
	}
	
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(EntityInteractEvent e) {
		Block block = e.getBlock();
		if (block != null) {
			Material material = block.getType();
			boolean isChest = isChest(material);
			boolean isDoor = isDoor(material);
			if (isChest || isDoor) {
				Entity entity = e.getEntity();
				Claimable claimable = Claimable.get(block.getChunk());
				if (isChest && !claimable.canOpenChests(entity)) e.setCancelled(true);
				else if (isDoor && !claimable.canOpenDoors(entity)) e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (player.getGameMode() != GameMode.SPECTATOR && e.hasBlock()) {
			Block block = e.getClickedBlock();
			Material material = block.getType();
			boolean isChest = isChest(material);
			boolean isDoor = isDoor(material);
			if (isChest || isDoor) {
				Claimable claimable = Claimable.get(block.getChunk());
				if (isChest && !claimable.canOpenChests(player)) e.setCancelled(true);
				else if (isDoor && !claimable.canOpenDoors(player)) e.setCancelled(true);
			}
			if (e.hasItem()) {
				Material type = e.getItem().getType();
				if (type == Material.ITEM_FRAME || type == Material.ARMOR_STAND)
					if (!Claimable.get(block.getChunk()).canBuild(e.getPlayer())) e.setCancelled(true);
			}
		}
	}
	
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(HangingPlaceEvent e) {
		if (!Claimable.get(e.getEntity().getLocation().getChunk()).canBuild(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(HangingBreakByEntityEvent e) {
		if (!Claimable.get(e.getEntity().getLocation().getChunk()).canBuild(e.getRemover())) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(PlayerInteractEntityEvent e) {
		Entity entity = e.getRightClicked();
		EntityType type = entity.getType();
		if (type == EntityType.ITEM_FRAME || type == EntityType.ARMOR_STAND)
			if (!Claimable.get(entity.getLocation().getChunk()).canBuild(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(PlayerInteractAtEntityEvent e) {
		Entity entity = e.getRightClicked();
		EntityType type = entity.getType();
		if (type == EntityType.ITEM_FRAME || type == EntityType.ARMOR_STAND)
			if (!Claimable.get(entity.getLocation().getChunk()).canBuild(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(PlayerArmorStandManipulateEvent e) {
		if (!Claimable.get(e.getRightClicked().getLocation().getChunk()).canBuild(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(EntityDamageByEntityEvent e) {
		Entity entity = e.getEntity();
		EntityType type = entity.getType();
		if (type == EntityType.ITEM_FRAME || type == EntityType.ARMOR_STAND)
			if (!Claimable.get(entity.getLocation().getChunk()).canBuild(e.getDamager())) e.setCancelled(true);
	}
	
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(BlockIgniteEvent e) {
		Claimable claimable = Claimable.get(e.getBlock().getChunk());
		if (!claimable.canBurn()) {
			Player player = e.getPlayer();
			boolean canBuild = false;
			if (player == null || (canBuild = !claimable.canBuild(player)))
				if (e.getCause() != IgniteCause.FLINT_AND_STEEL || canBuild) e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(BlockBurnEvent e) {
		if (!Claimable.get(e.getBlock().getChunk()).canBurn()) e.setCancelled(true);
	}
	
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(EntityChangeBlockEvent e) {
		if (!Claimable.get(e.getBlock().getChunk()).canBuild(e.getEntity())) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(PlayerBucketEmptyEvent e) {
		if (!Claimable.get(e.getBlockClicked().getChunk()).canUseBuckets(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(PlayerBucketFillEvent e) {
		if (!Claimable.get(e.getBlockClicked().getChunk()).canUseBuckets(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(BlockPlaceEvent e) {
		if (e.getBlockPlaced().getType() != Material.FIRE)
			if (!Claimable.get(e.getBlock().getChunk()).canBuild(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void on(BlockBreakEvent e) {
		if (!Claimable.get(e.getBlock().getChunk()).canBuild(e.getPlayer())) e.setCancelled(true);
	}
	
}