package com.m0pt0pmatt.bettereconomy;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterEconomy extends JavaPlugin{

	
	public static EconomyManager economy = null;
	public static ConfigManager configManager = null;
	public static FileSavingThread savingThread = null;
	
	/**
	 * This is ran once the plugin is enabled. It is ran after the constructor.
	 * loads the houses from a local file.
	 */
	public void onEnable(){		
		
		//setup fileManager
		configManager = new ConfigManager(this, "accounts.yml");
		
		//setup economy
		economy = new EconomyManager(this);
		//TODO: turn these into a configuration file
		economy.addCurrency(new Currency("coal", Material.COAL, 0, 10));
		economy.addCurrency(new Currency("redstone", Material.REDSTONE, 0, 15));
		economy.addCurrency(new Currency("coal_ore", Material.COAL_ORE, 0, 22));
		economy.addCurrency(new Currency("iron_ore", Material.IRON_ORE, 0, 20));
		economy.addCurrency(new Currency("iron", Material.IRON_INGOT, 0, 20));
		economy.addCurrency(new Currency("redstone_ore", Material.REDSTONE_ORE, 0, 33));
		economy.addCurrency(new Currency("lapis", Material.INK_SACK, 4, 35));
		economy.addCurrency(new Currency("lapis_ore", Material.LAPIS_ORE, 0, 77));
		economy.addCurrency(new Currency("iblock", Material.IRON_BLOCK, 0, 180));
		economy.addCurrency(new Currency("gold_ore", Material.GOLD_ORE, 0, 200));
		economy.addCurrency(new Currency("gold",  Material.GOLD_INGOT, 0, 200));
		economy.addCurrency(new Currency("lblock", Material.LAPIS_BLOCK, 0, 315));
		economy.addCurrency(new Currency("diamond", Material.DIAMOND, 0, 400));
		economy.addCurrency(new Currency("diamond_ore", Material.DIAMOND_ORE, 0, 880));
		economy.addCurrency(new Currency("gblock", Material.GOLD_BLOCK, 0, 1800));
		economy.addCurrency(new Currency("dblock", Material.DIAMOND_BLOCK, 0, 3600));
		getServer().getPluginManager().registerEvents(new EconomyListener(this), this);
		
		//set up the thread that saves data
		savingThread = new FileSavingThread();
		savingThread.start();
		
		
		//register the economy
		try{
			this.getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, economy, this, ServicePriority.Normal);
		}
		catch (Exception e){
			getLogger().warning("[HomeWorldPlugin] Unable to register Economy.");
		}
		
		
		getLogger().info("[HomeWorldPlugin] HomeWorldPlugin has been enabled.");
	}
 
	/**
	 * ran when the plugin is being disabled. saves the houses to file.
	 */
	public void onDisable(){
		saveAll();
		
		getLogger().info("[HomeWorldPlugin] HomeWorldPlugin has been disbled.");
	}
	
	/**
	 * Reload method. Makes sure all data is saved to file.
	 */
	public void onReload(){
		saveAll();
		loadAll();
		getLogger().info("[HomeWorldPlugin] HomeWorldPlugin has been reloaded.");
	}
	
	/**
	 * commands are handled by CommandHandler
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return CommandHandler.commands(sender, cmd, label, args);
	}
	
	public static void saveAll() {
		economy.save();
	}
	
	public static void loadAll(){
		economy.load();
	}
	
}
