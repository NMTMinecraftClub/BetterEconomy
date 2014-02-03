package com.m0pt0pmatt.bettereconomy;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.m0pt0pmatt.bettereconomy.currency.Currency;
import com.m0pt0pmatt.bettereconomy.currency.CurrencyListener;
import com.m0pt0pmatt.bettereconomy.util.FileSavingThread;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class BetterEconomy extends JavaPlugin{
	
	public static EconomyManager economy = null;
	public static FileSavingThread savingThread = null;
	public static YamlConfiguration config;
	public static File configFile;

	public static StateFlag bankFlag = new StateFlag("bank", false);
	
	/**
	 * The WorldGuard hook
	 */
	public static WorldGuardPlugin wgplugin = null;
	
	/**
	 * The WorldEdit hook
	 */
	public static WorldEditPlugin weplugin = null;
	
	/**
	 * This is ran once the plugin is enabled. It is ran after the constructor.
	 * loads the houses from a local file.
	 */
	public void onEnable(){		
		
		weplugin = getWorldEdit();
		wgplugin = getWorldGuard();
		
		//setup config file
		if (!this.getDataFolder().exists()){
			this.getDataFolder().mkdir();
		}
		configFile = new File(this.getDataFolder(), "accounts.yml");
		if (!configFile.exists()){
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(configFile);
		
		//setup economy
		economy = new EconomyManager(this);
		
		//TODO: turn these into a configuration file
		economy.addCurrency(new Currency("dblock", new ItemStack(Material.DIAMOND_BLOCK), 3600));
		economy.addCurrency(new Currency("gblock", new ItemStack(Material.GOLD_BLOCK), 1800));
		economy.addOre(new Currency("diamond_ore", new ItemStack(Material.DIAMOND_ORE), 880));
		economy.addCurrency(new Currency("diamond", new ItemStack(Material.DIAMOND), 400));
		economy.addCurrency(new Currency("lblock", new ItemStack(Material.LAPIS_BLOCK), 315));
		economy.addCurrency(new Currency("gold",  new ItemStack(Material.GOLD_INGOT), 200));
		economy.addCurrency(new Currency("gold_nugget",  new ItemStack(Material.GOLD_NUGGET), (int)Math.floor(200/9)));
		economy.addOre(new Currency("gold_ore", new ItemStack(Material.GOLD_ORE), 200));
		economy.addCurrency(new Currency("iblock", new ItemStack(Material.IRON_BLOCK), 180));
		economy.addCurrency(new Currency("rblock", new ItemStack(Material.REDSTONE_BLOCK), 135));
		economy.addCurrency(new Currency("cblock", new ItemStack(Material.COAL_BLOCK), 90));
		economy.addOre(new Currency("lapis_ore", new ItemStack(Material.LAPIS_ORE), 77));
		Dye lapis = new Dye();
		lapis.setColor(DyeColor.BLUE);
		ItemStack inc = new ItemStack(Material.INK_SACK);
		inc.setData(lapis);
		economy.addCurrency(new Currency("lapis", new ItemStack(inc), 35));
		economy.addOre(new Currency("redstone_ore", new ItemStack(Material.REDSTONE_ORE), 33));
		economy.addCurrency(new Currency("iron", new ItemStack(Material.IRON_INGOT), 20));
		economy.addOre(new Currency("iron_ore", new ItemStack(Material.IRON_ORE), 20));
		economy.addOre(new Currency("coal_ore", new ItemStack(Material.COAL_ORE), 22));
		economy.addCurrency(new Currency("redstone", new ItemStack(Material.REDSTONE), 15));
		economy.addCurrency(new Currency("coal", new ItemStack(Material.COAL), 10));

		getServer().getPluginManager().registerEvents(new EconomyListener(this), this);
		getServer().getPluginManager().registerEvents(new CurrencyListener(economy), this);
		
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

		//set global flags
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "/region flag __global__ blocked-cmds withdraw,deposit,wealth");
		
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
	
	/**
	 * method for WorldGuard to get the WorldGuard Plugin
	 * @return the WorldGuard Plugin
	 */
	public static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	    
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	    	return null; // Maybe you want throw an exception instead
	    }
	    
	    return (WorldGuardPlugin) plugin;
	}
	
	/**
	 * method for WorldEdit to get the WorldEdit Plugin
	 * @return the WorldEdit Plugin
	 */
	public static WorldEditPlugin getWorldEdit() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldEditPlugin) plugin;
	}
}