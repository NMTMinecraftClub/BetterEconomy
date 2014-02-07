package com.m0pt0pmatt.bettereconomy;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.m0pt0pmatt.bettereconomy.currency.Currency;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * The CommandHandler is the class responsible for handling all commands.
 * @author Matthew Broomfield, Lucas Stuyvesant
 *
 */
public class CommandHandler {
	
	/**
	 * Handle commands send from the BetterEconomy plugin
	 * @param sender the CommandSender of the command
	 * @param cmd the Command being executed
	 * @param label 
	 * @param args the arguments sent with the command
	 * @return true if a command was executed successfully, false otherwise
	 */
	public static boolean commands(CommandSender sender, Command cmd, String label, String[] args){
		
		/**
		 * admin wants to evaluate the economy (currencies)
		 */
		if(cmd.getName().equalsIgnoreCase("evaluateEconomy")){
			if (args.length != 0){
				sender.sendMessage("wrong number of args");
				return false;
			}

			if (!(sender instanceof Player) || !(sender.isOp())){
				sender.sendMessage("Must be an OP to execute");
				return false;
			}
			
			BetterEconomy.economy.evaluateCurrencies(sender);
			
			return true;
		}
		
		/**
		 * admin wants to create a bank
		 */
		if(cmd.getName().equalsIgnoreCase("createBank")){
			if (args.length != 0){
				sender.sendMessage("wrong number of args");
				return false;
			}

			if (!(sender instanceof Player) || !(sender.isOp())){
				sender.sendMessage("Must be an OP to execute");
				return false;
			}
			
			//get the region manager for the homeworld
			RegionManager rm = BetterEconomy.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
			if (rm == null){
				sender.sendMessage("No region manager for the homeworld");
				return false;
			}
			
			String name = "__Bank";
			//make sure name isn't already used
			if (rm.getRegion("__Bank") != null){
				sender.sendMessage("I'm sorry, but that name is already in use.");
				return false;
			}
			
			//get the WorldEdit selection
			Selection selection = BetterEconomy.weplugin.getSelection((Player) sender);
			BlockVector b1 = new BlockVector(selection.getMinimumPoint().getX(), selection.getMinimumPoint().getY(), selection.getMinimumPoint().getZ());
			BlockVector b2 = new BlockVector(selection.getMaximumPoint().getX(), selection.getMaximumPoint().getY(), selection.getMaximumPoint().getZ());
			
			//create WorldGuard Region
			ProtectedRegion region = new ProtectedCuboidRegion(name, b1, b2);
			region.setFlag(BetterEconomy.isBank, State.ALLOW);
			region.setFlag(DefaultFlag.GREET_MESSAGE, "Welcome to the bank");
			
			//add the new region to WorldGuard
			rm.addRegion(region);
			
			//add player to the owner of the new region
			DefaultDomain newDomain = new DefaultDomain();
			newDomain.addPlayer("__Server");
			rm.getRegion(name).setOwners(newDomain);
			
			//save WorldGuard
			try {
				rm.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			sender.sendMessage("bank created");
			return true;
		}
		
		/**
		 * player wants to give another player a certain amount
		 */
		if(cmd.getName().equalsIgnoreCase("pay")){
			if (args.length == 2){
				double d;
				if((d = Double.parseDouble(args[1])) <= 0){
					sender.sendMessage("Invalid payment amount.");
					return false;
				}
				BetterEconomy.economy.pay(sender, args[0], d);
				return true;
			}
			
			sender.sendMessage("Wrong number of arguments.");
			return false;
		}
		
		/**
		 * player wants to see the top accounts on the server
		 */
		if(cmd.getName().equalsIgnoreCase("top")){
			if (args.length == 1){
				BetterEconomy.economy.top(sender, Integer.parseInt(args[0]));
				return true;
			}
			
			sender.sendMessage("Wrong number of arguments.");
			return false;
		}
		
		/**
		 * player wanted to check the value of something
		 */
		if(cmd.getName().equalsIgnoreCase("value")){
			if (args.length == 1){
				if (args[0].equalsIgnoreCase("current")){
					BetterEconomy.economy.calculateWealth(sender);
					return true;
				}
				else if (args[0].equalsIgnoreCase("bank")){
					HashMap<Currency,Integer> map = BetterEconomy.bank.getMap();
					for(Currency c: BetterEconomy.economy.getCurrencies()){
						sender.sendMessage(c.getName() + ": " + map.get(c));
					}
					return true;
				}
				else{
					BetterEconomy.economy.checkValue(sender, args[0], 1);
					return true;
				}			
			}
			if (args.length == 2){
				
				BetterEconomy.economy.checkValue(sender, args[0], Integer.parseInt(args[1]));
				return true;
			}
			
			sender.sendMessage("Wrong number of arguments.");
			return false;
		}
		
		/**
		 * player wants to check their balance
		 */
		if(cmd.getName().equalsIgnoreCase("money")){
			if (args.length == 0){
				BetterEconomy.economy.showBalance(sender);
				return true;
			}
			
			sender.sendMessage("Wrong number of arguments.");
			return false;
		}
		
		
		//make sure the player is in a bank before executing bank commands
		if (!(sender instanceof Player)){
			return false;
		}
		
		
		
		Player player = (Player) sender;
		RegionManager rm = BetterEconomy.wgplugin.getRegionManager(player.getWorld());
		ApplicableRegionSet ars = rm.getApplicableRegions(player.getLocation());
		
		if (!ars.allows(BetterEconomy.isBank)){
			sender.sendMessage("You must be inside of a bank to execute the command /" + cmd.getName());
			return false;
		}
		

		/**
		 * player wants to deposit money
		 */
		if(cmd.getName().equalsIgnoreCase("deposit")){
			if (args.length == 3){
				if (!(sender instanceof BlockCommandSender)){
					return false;
				}
				//ADDED 2/21/13 @author Lucas Stuyvesant
				//deposit [currency] all 
				if (args[2].equalsIgnoreCase("all")){
					BetterEconomy.economy.depositAll(Bukkit.getPlayer(args[0]), args[1]);
					return true;
				}
				else{
					BetterEconomy.economy.deposit(Bukkit.getPlayer(args[0]), args[1],Integer.parseInt(args[2]));
					return true;	
				}
				
			}
			if (args.length == 2){
				if (args[1].equalsIgnoreCase("all")){
					BetterEconomy.economy.depositAll(sender, args[0]);
					return true;
				}
				else{
					BetterEconomy.economy.deposit(sender, args[0],Integer.parseInt(args[1]));
					return true;
				}
			}
			if (args.length == 1){
				if(args[0].equalsIgnoreCase("all")){
					BetterEconomy.economy.depositEverything(sender);
					return true;
				}
				else{
					sender.sendMessage("Wrong number of arguments");
					return false;
				}
			}
			else{
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
		}
		
		/**
		 * player wants to withdraw currency
		 */
		if(cmd.getName().equalsIgnoreCase("withdraw")){
			if (args.length == 3){
				if (!(sender instanceof BlockCommandSender)){
					return false;
				}
				if(args[2].equalsIgnoreCase("all")){
					BetterEconomy.economy.greedyWithdraw(Bukkit.getPlayer(args[0]), BetterEconomy.economy.getCurrency(args[1]));
					return true;
				}
				else{
					BetterEconomy.economy.withdraw(Bukkit.getPlayer(args[0]), args[1],Integer.parseInt(args[2]));
					return true;
				}
			}
			if (args.length == 2){
				if(args[1].equalsIgnoreCase("all")){
					BetterEconomy.economy.greedyWithdraw(sender, BetterEconomy.economy.getCurrency(args[0]));
					return true;
				}
				else{
					BetterEconomy.economy.withdraw(sender, args[0],Integer.parseInt(args[1]));
					return true;
				}
			}
			if (args.length == 1){
				if(args[0].equalsIgnoreCase("all")){
					BetterEconomy.economy.withdrawAll(sender);
					return true;
				}
				else{
					sender.sendMessage("Wrong number of arguments");
					return false;
				}
			}
			else{
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
		}
		
		/**
		 * server wants to set a players balance
		 */
		if(cmd.getName().equalsIgnoreCase("setbalance")){
			if (args.length == 2){
				BetterEconomy.economy.setBalance(sender, args[0], Double.parseDouble(args[1]));
				return true;
			}
			
			sender.sendMessage("Wrong number of arguments.");
			return false;
		}
		
			
		
		return false;
	}
}
