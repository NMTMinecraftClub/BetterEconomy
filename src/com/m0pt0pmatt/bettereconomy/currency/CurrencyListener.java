package com.m0pt0pmatt.bettereconomy.currency;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.m0pt0pmatt.bettereconomy.EconomyManager;

/**
 * Listens for events which are related to currencies
 * @author Matthew
 *
 */
public class CurrencyListener implements Listener{
	
	//The EconomyManager
	EconomyManager manager;
	
	public CurrencyListener(EconomyManager manager){
		this.manager = manager;
	}

	/**
	 * Mob death event. Adjust drops so no physical currencies are dropped
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onMobDeath(EntityDeathEvent event){
		
		//do nothing if the entity was a player
		if (event.getEntityType().equals(EntityType.PLAYER)) return;
		
		//get the dropped items
		List<ItemStack> drops = event.getDrops();
		if (drops == null) return;
		
		//Iterate through the items
		Iterator<ItemStack> i = drops.iterator();
		while (i.hasNext()){
			
			//check if the item is a physical currency
			ItemStack item = i.next();
			if (manager.isCurrency(item)){
				
				//add the "not currency" mark to the lore
				ItemMeta meta = item.getItemMeta();
				List<String> lore = meta.getLore();
				if (lore == null) lore = new LinkedList<String>();
				lore.add(ChatColor.RED + "(Not Currency)");
				meta.setLore(lore);
				item.setItemMeta(meta);
			}
		}
	}
	
	/**
	 * Prevent placing fake redstone currency currency
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onCurrencyPlaceEvent(PlayerInteractEvent event){
		
		//get the item
		ItemStack item = event.getItem();
		if (item == null) return;
		
		//check if the item is a physical currency
		if (manager.isCurrency(item)){
			
			//get the lore
			List<String> lore = item.getItemMeta().getLore();
			if (lore == null) return;
		
			//check if the item is fake currency
			if (lore.contains(ChatColor.RED + "(Not Currency)")){
				
				//prevent the event
				event.setCancelled(true);
			}
		}	
	}
	
	/**
	 * Prevent crafting with fake currencies
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCraft(CraftItemEvent event){
		
		//for every item on the crafting table
		for (ItemStack item: event.getInventory().getMatrix()){
			
			//only continue if the item exists and has meta and lore
			if (item == null) continue;
			if (item.getItemMeta() == null) continue;
			List<String> lore = item.getItemMeta().getLore();
			if (lore == null) continue;
			
			//check if the item is a fake currency
			if (lore.contains(ChatColor.RED + "(Not Currency)")){
				
				//cancel the event
				event.setCancelled(true);
				return;
			}
		}
	}
}
