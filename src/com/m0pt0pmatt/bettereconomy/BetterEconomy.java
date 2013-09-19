package com.m0pt0pmatt.bettereconomy;

import java.io.File;
import java.io.IOException;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.Dye;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterEconomy extends JavaPlugin{
	
	public static EconomyManager economy = null;
	public static FileSavingThread savingThread = null;
	public static YamlConfiguration config;
	public static File configFile;
	
	/**
	 * This is ran once the plugin is enabled. It is ran after the constructor.
	 * loads the houses from a local file.
	 */
	public void onEnable(){		
		
		//setup config file
		if (!this.getDataFolder().exists()){
			this.getDataFolder().mkdir();
		}
		configFile = new File(this.getDataFolder(), "accounts.yml");
		if (!configFile.exists()){
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(configFile);
		
		//setup economy
		economy = new EconomyManager(this);
		//TODO: turn these into a configuration file
		economy.addCurrency(new Currency("dblock", Material.DIAMOND_BLOCK, 3600));
		economy.addCurrency(new Currency("gblock", Material.GOLD_BLOCK, 1800));
		economy.addOre(new Currency("diamond_ore", Material.DIAMOND_ORE, 880));
		economy.addCurrency(new Currency("diamond", Material.DIAMOND, 400));
		economy.addCurrency(new Currency("lblock", Material.LAPIS_BLOCK, 315));
		economy.addCurrency(new Currency("gold",  Material.GOLD_INGOT, 200));
		economy.addOre(new Currency("gold_ore", Material.GOLD_ORE, 200));
		economy.addCurrency(new Currency("iblock", Material.IRON_BLOCK, 180));
		economy.addCurrency(new Currency("rblock", Material.REDSTONE_BLOCK, 135));
		economy.addCurrency(new Currency("cblock", Material.COAL_BLOCK, 90));
		economy.addOre(new Currency("lapis_ore", Material.LAPIS_ORE, 77));
		Dye lapis = new Dye();
		lapis.setColor(DyeColor.BLUE);
		economy.addCurrency(new Currency("lapis", lapis, 35));
		economy.addOre(new Currency("redstone_ore", Material.REDSTONE_ORE, 33));
		economy.addCurrency(new Currency("iron", Material.IRON_INGOT, 20));
		economy.addOre(new Currency("iron_ore", Material.IRON_ORE, 20));
		economy.addOre(new Currency("coal_ore", Material.COAL_ORE, 22));
		economy.addCurrency(new Currency("redstone", Material.REDSTONE, 15));
		economy.addCurrency(new Currency("coal", Material.COAL, 10));

		getServer().getPluginManager().registerEvents(new EconomyListener(this), this);
		
		loadAll();
		
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
