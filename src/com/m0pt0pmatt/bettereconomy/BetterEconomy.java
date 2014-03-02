package com.m0pt0pmatt.bettereconomy;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.m0pt0pmatt.bettereconomy.banks.Bank;
import com.m0pt0pmatt.bettereconomy.currency.CurrencyListener;
import com.m0pt0pmatt.bettereconomy.util.FileSavingThread;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * BetterEconomy is an economy plugin which is suited for the specific needs
 * of the NMT Minecraft Club.
 * 
 * In BetterEconomy, physical currencies are assigned dollar values.
 * These currencies can be exchanged for their respective values
 * at banks. Banks, however, cannot "magically" exchange different physical
 * currencies. A.K.A: no items are created or deleted, only naturally generated
 * 
 * @author Matthew Broomfield, Lucas Stuyvesant
 *
 */
public class BetterEconomy extends JavaPlugin{
	
	/**
	 * The Bank stores all inventory for the bank storage
	 */
	public static Bank bank = null;

	/**
	 * The EconomyManager handles all economy-based functionality
	 */
	public static EconomyManager economy = null;
	
	/**
	 * A simple thread which saves data to file every couple of minutes
	 */
	public static FileSavingThread savingThread = null;
	
	/**
	 * The base configuration file for BetterEconomy
	 */
	public static YamlConfiguration config;
	
	/**
	 * The actual File object of the base config file.
	 */
	public static File configFile;
	
	/**
	 * The WorldGuard hook
	 */
	public static WorldGuardPlugin wgplugin = null;
	
	/**
	 * The WorldEdit hook
	 */
	public static WorldEditPlugin weplugin = null;
	
	public static StateFlag isBank = new StateFlag("can-bank", false);
	
	/**
	 * This is ran once the plugin is enabled. It is ran after the constructor.
	 * loads the houses from a local file.
	 */
	public void onEnable(){
		
		if (!this.getDataFolder().exists()){
			this.getDataFolder().mkdir();
		}
		
		//hook into worldedit and worldguard
		weplugin = getWorldEdit();
		wgplugin = getWorldGuard();
		
		//setup economy
		economy = new EconomyManager(new File(this.getDataFolder(), "accounts.yml"));
				
		//Register Listeners
		getServer().getPluginManager().registerEvents(new EconomyListener(this), this);
		getServer().getPluginManager().registerEvents(new CurrencyListener(economy), this);
		
		bank = new Bank("globalbank",new File(this.getDataFolder(), "globalbank.yml"));
		
		load();
		
		//set up the thread that saves data
		if (savingThread == null){
			savingThread = new FileSavingThread();
			savingThread.start();
		}
		
		//register the economy
		try{
			this.getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, economy, this, ServicePriority.Normal);
		}
		catch (Exception e){
			getLogger().warning("[HomeWorldPlugin] Unable to register Economy.");
		}

		//set global flags
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "/region flag __global__ blocked-cmds withdraw,deposit,wealth");
		
		//the worst fix ever
		//get the region manager for the homeworld
		RegionManager rm = BetterEconomy.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		ProtectedRegion region = rm.getRegion("__bank");
		if (region != null){
			region.setFlag(BetterEconomy.isBank, State.ALLOW);
		}
		
		
		getLogger().info("[HomeWorldPlugin] HomeWorldPlugin has been enabled.");
	}

	

	/**
	 * Saves player accounts when the plugin is being disabled.
	 */
	public void onDisable(){
		savingThread.die = true;
		save();
		getLogger().info("BetterEconomy has been disabled.");
	}
	
	/**
	 * Sends commands to the CommandHandler to be dealt with.
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return CommandHandler.commands(sender, cmd, label, args);
	}
	
	/**
	 * Saves everything.
	 */
	public static void save() {
		bank.save();
		economy.save();
	}
	
	/**
	 * Loads everything.
	 */
	public static void load(){
		bank.load();
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