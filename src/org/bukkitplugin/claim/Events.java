package org.bukkitplugin.claim;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkitplugin.claim.claimable.Claimable;

public class Events implements Listener {
	
	public static boolean isContainer(Material material) {
		List<Material> materials = new ArrayList<Material>();
		materials.add(Material.BEACON);
		materials.add(Material.CHEST);
		materials.add(Material.TRAPPED_CHEST);
		materials.add(Material.DISPENSER);
		materials.add(Material.DROPPER);
		if (materials.contains(material)) return true;
		if (material.name().contains("SHULKER_BOX")) return true;
		return false;
	}
	
	public static boolean isDoor(Material material) {
		return material.name().contains("DOOR") || material.name().contains("FENCE_GATE");
	}
	
	@EventHandler
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
	
	@EventHandler
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
	
	@EventHandler
	public void on(EntityInteractEvent e) {
		Entity entity = e.getEntity();
		Block block = e.getBlock();
		if (block != null) {
			Material material = block.getType();
			boolean isChest = isContainer(material);
			boolean isDoor = isDoor(material);
			if (isChest || isDoor) {
				Claimable claimable = Claimable.get(block.getChunk());
				if (isChest && !claimable.canOpenContainers(entity)) e.setCancelled(true);
				else if (isDoor && !claimable.canOpenDoors(entity)) e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void on(PlayerInteractEntityEvent e) {
		Player player = e.getPlayer();
		Entity entity = e.getRightClicked();
		EntityType type = entity.getType();
		if (type == EntityType.ITEM_FRAME && !Claimable.get(entity.getLocation().getChunk()).canOpenContainers(player)) e.setCancelled(true);
	}
	
	@EventHandler
	public void on(PlayerArmorStandManipulateEvent e) {
		if (!Claimable.get(e.getRightClicked().getLocation().getChunk()).canOpenContainers(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler
	public void on(EntityDamageByEntityEvent e) {
		Entity entity = e.getEntity();
		Entity damager = e.getDamager();
		if (damager instanceof Player) {
			Player player = (Player) damager;
			EntityType type = entity.getType();
			if (type == EntityType.ITEM_FRAME || type == EntityType.ARMOR_STAND) {
				if (!Claimable.get(entity.getLocation().getChunk()).canBuild(player)) e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void on(BlockIgniteEvent e) {
		Player player = e.getPlayer();
		Claimable claimable = Claimable.get(e.getBlock().getChunk());
		if (e.getCause() != IgniteCause.FLINT_AND_STEEL && !claimable.canBurn()) e.setCancelled(true);
		if (player != null && !claimable.canBuild(player)) e.setCancelled(true);
	}
	
	@EventHandler
	public void on(BlockBurnEvent e) {
		if (!Claimable.get(e.getBlock().getChunk()).canBurn()) e.setCancelled(true);
	}
	
	@EventHandler
	public void on(PlayerBucketEmptyEvent e) {
		if (!Claimable.get(e.getBlockClicked().getChunk()).canUseBuckets(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler
	public void on(PlayerBucketFillEvent e) {
		if (!Claimable.get(e.getBlockClicked().getChunk()).canUseBuckets(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler
	public void on(BlockPlaceEvent e) {
		if (!Claimable.get(e.getBlock().getChunk()).canBuild(e.getPlayer())) e.setCancelled(true);
	}
	
	@EventHandler
	public void on(BlockBreakEvent e) {
		if (!Claimable.get(e.getBlock().getChunk()).canBuild(e.getPlayer())) e.setCancelled(true);
	}
	
}