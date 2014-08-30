package com.m0pt0pmatt.bettereconomy;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.m0pt0pmatt.bettereconomy.commands.EconomyCommand;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
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
		 * server wants to set a players balance
		 * /setbalance [playername] [amount]
		 */
		if(cmd.getName().equalsIgnoreCase(EconomyCommand.SETBALANCE.command)){
			
			if (args.length != 2){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			
			double amount;
			
			try{
				amount = Double.parseDouble(args[1]);
			} catch(Exception e){
				sender.sendMessage("Error. Enter a valid number");
				return false;
			}
			
			return BetterEconomy.economy.setBalance(sender, UUID.fromString(args[0]), amount);
		}
		
		/**
		 * Admin wants to evaluate the economy (currencies)
		 * 
		 * /evaluateeconomy
		 */
		if(cmd.getName().equalsIgnoreCase(EconomyCommand.EVALUATEECONOMY.command)){
			
			if (args.length != 0){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}

			if (!(sender instanceof Player) || !(sender.isOp())){
				sender.sendMessage("Must be an OP Player to execute");
				return false;
			}
			
			return BetterEconomy.economy.evaluateCurrencies(sender);
		}
		
		/**
		 * Admin wants to create a bank
		 * Before running this command, make sure to have a worldedit region selected
		 * 
		 * /createbank [bankName]
		 */
		if(cmd.getName().equalsIgnoreCase(EconomyCommand.CREATEBANK.command)){
			
			if (args.length != 1){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}

			if (!(sender instanceof Player) || !(sender.isOp())){
				sender.sendMessage("Must be an OP Player to execute");
				return false;
			}
			
			return BetterEconomy.economy.createBank(sender, args[0]);
		}
		
		/**
		 * player wants to give another player a certain amount
		 * 
		 * /pay [playerName] [amount]
		 */
		if(cmd.getName().equalsIgnoreCase(EconomyCommand.PAY.command)){
			
			if (args.length != 2){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			
			int amount;
			UUID receiver_id;
			
			try{
				amount = Integer.parseInt(args[1]);
			}
			catch(Exception e){
				sender.sendMessage("Error. Enter a valid number (whole numbers only)");
				return false;
			}
			
			if(amount <= 0){
				sender.sendMessage("Error. Please specify a positive amount");
				return false;
			}
			
			try {
				receiver_id = UUID.fromString(args[0]);
			}
			catch (IllegalArgumentException E) {
				receiver_id = null;
			}
			
			return BetterEconomy.economy.pay(sender, receiver_id == null ? Bukkit.getOfflinePlayer(args[0]) : Bukkit.getOfflinePlayer(receiver_id), amount);
		}
		
		/**
		 * player wants to see the top accounts on the server
		 * 
		 * /top [number]
		 */
		if(cmd.getName().equalsIgnoreCase(EconomyCommand.TOP.command)){
			
			if (args.length != 1){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			
			return BetterEconomy.economy.top(sender, Integer.parseInt(args[0]));
		}
		
		/**
		 * player wanted to check the value of something
		 * 
		 * /value current
		 * /value bank
		 * /value [currencyName]
		 * /value [currencyName] [amount]
		 */
		if(cmd.getName().equalsIgnoreCase(EconomyCommand.VALUE.command)){
			
			if (args.length == 1){
				if (args[0].equalsIgnoreCase("current")){
					return BetterEconomy.economy.calculateWealth(sender);
				}
				else if (args[0].equalsIgnoreCase("bank")){
					return BetterEconomy.economy.showBankValues(sender);
				}
				else{
					return BetterEconomy.economy.checkValue(sender, args[0], 1);
				}
			}	
			else if (args.length == 2){
				return BetterEconomy.economy.checkValue(sender, args[0], 1);
			}
			else{
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
		}
		
		/**
		 * player wants to check their balance
		 * 
		 * /money
		 */
		if(cmd.getName().equalsIgnoreCase(EconomyCommand.MONEY.command)){
			
			if (args.length != 0){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			
			return BetterEconomy.economy.showBalance(sender);
		}
		
		
		//make sure the player is in a bank before executing bank commands
		if (!(sender instanceof Player)){
			return false;
		}	
		
		Player player = (Player) sender;
		RegionManager rm = BetterEconomy.wgplugin.getRegionManager(player.getWorld());
		ApplicableRegionSet ars = rm.getApplicableRegions(player.getLocation());
		
		Iterator<ProtectedRegion> i = ars.iterator();
		boolean good = false;
		while (i.hasNext()){
			ProtectedRegion r = i.next();
			if (r.getId().startsWith("__bank__")){
				good = true; break;
			}
		}
			
		if (!good){
			sender.sendMessage("You must be inside of a bank to execute the command /" + cmd.getName());
			return false;
		}
		/**
		 * player wants to deposit money
		 * 
		 * /deposit [currencyName] [amount]
		 * /deposit [currencyName] all
		 * /deposit all
		 */
		if(cmd.getName().equalsIgnoreCase(EconomyCommand.DEPOSIT.command)){
			
			if (args.length == 2){
				if (args[1].equalsIgnoreCase("all")){
					return BetterEconomy.economy.depositAll(sender, args[0]);
				}
				return BetterEconomy.economy.deposit(sender, args[0],Integer.parseInt(args[1]));
			}
			if (args.length == 1){
				if(args[0].equalsIgnoreCase("all")){
					return BetterEconomy.economy.depositEverything(sender);
				}
			}
			sender.sendMessage("Wrong number of arguments.");
			return false;
		}
		
		/**
		 * player wants to withdraw currency
		 * 
		 *	/withdraw [currencyName] [amount]
		 *	/withdraw [currencyName] all
		 *	/withdraw all
		 */
		if(cmd.getName().equalsIgnoreCase(EconomyCommand.WITHDRAW.command)){
			
			if (args.length == 2){
				if (args[1].equalsIgnoreCase("all")){
					return BetterEconomy.economy.withdrawAll(sender, args[0]);
				}
				return BetterEconomy.economy.withdraw(sender, args[0], Integer.parseInt(args[1]));
			}
			if (args.length == 1){
				if(args[0].equalsIgnoreCase("all")){
					return BetterEconomy.economy.withdrawEverything(sender);
				}
			}
			sender.sendMessage("Wrong number of arguments.");
			return false;
		}
		
		return false;
	}
}
