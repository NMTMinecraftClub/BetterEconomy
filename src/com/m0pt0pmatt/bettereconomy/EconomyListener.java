package com.m0pt0pmatt.bettereconomy;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.m0pt0pmatt.bettereconomy.accounts.UUIDFetcher;
import com.m0pt0pmatt.bettereconomy.currency.Currency;

/**
 * Listener for economy related events
 * @author Matthew Broomfield
 *
 */
public class EconomyListener implements Listener{
	
	/**
	 * hook to grab the economy
	 */
	private EconomyManager economy;
	
	/**
	 * Default constructor
	 */
	public EconomyListener(BetterEconomy plugin){
		
		//grab the EconomyManager
		this.economy = BetterEconomy.economy;
	}
	
	/**
	 * Creates accounts for users when they log in, if they don't already have an account
	 * @param event PlayerLoginEvent
	 */
	@EventHandler(priority = EventPriority.LOWEST)
    public void createAccount(PlayerLoginEvent event) {
		
		//try to grab the economy. I know
		while (economy == null){
			economy = BetterEconomy.economy;
			return;
		}
		
		//If player does not have an account, create one
		if (!(economy.hasAccount(event.getPlayer()))){
			
			economy.createPlayerAccount(event.getPlayer());
			event.getPlayer().sendMessage("Your new account has been made");
			System.out.println("Made new account for " + event.getPlayer().getName());
		}
	}
	
	/*
	 * Removes a percentage (40-70) of currency from a player's enderchest upon death in the wilderness
	 * @param event PlayerDeathEvent
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void wildernessDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(player.getLocation().getWorld().getName().equals("Wilderness")) {
			Random random = new Random();
			int percentLost = random.nextInt(30) + 40;
			int inventoryAmount = 0;
			int valueLost = 0;

			// Iterates through arraylists depositing items
		 	for(Currency currency: economy.getCurrencies().values()){
		 		inventoryAmount = economy.countCurrency(player.getEnderChest(), currency);
				economy.removeCurrency(player.getEnderChest(), currency, (int)Math.ceil(inventoryAmount * percentLost / 100.));
				valueLost += currency.getValue(1) * (int)Math.ceil(inventoryAmount * percentLost / 100.);
		 	}

			player.sendMessage("You died in the wilderness");
			player.sendMessage("$" + valueLost + " was lost from your EnderChest (" + percentLost + "% of currencies)");
			
			if(player.getKiller() != null){
				economy.depositPlayer(player.getKiller().getPlayer(), valueLost);
				player.getKiller().sendMessage("You have been rewarded " + valueLost + "for killing " + player.getName());
			}
			else{
				try {
					if (!economy.hasAccount(UUIDFetcher.getUUIDOf("__Server"))){
						economy.createPlayerAccount(UUIDFetcher.getUUIDOf("__Server"));
					}
					economy.depositPlayer(UUIDFetcher.getUUIDOf("__Server"), valueLost);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
