package com.m0pt0pmatt.bettereconomy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

import com.m0pt0pmatt.bettereconomy.accounts.Account;
import com.m0pt0pmatt.bettereconomy.banks.Bank;
import com.m0pt0pmatt.bettereconomy.currency.Currency;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyManager implements Economy{
	
	//constant variables
	private static final String economyName = "BetterEconomy";
	private static final String currencyName = "Dollar";
	private static final int decimalPlaces = 2;
	private static final double startingAmount = 50.0;
	
	//internal data structures
	private final Map<String, Currency> currencies = new HashMap<String, Currency>();
	private final Map<String, Account> accounts = new HashMap<String, Account>();
	private final Map<String, Bank> banks = new HashMap<String, Bank>();

	private YamlConfiguration config;
	
	public EconomyManager(File file){
		config = YamlConfiguration.loadConfiguration(file);
		setupCurrencies();
	}
	
	//----------------------
	//START OF FILE-BASED METHODS
	//----------------------
	
	/**
	 * Save economy data to file
	 */
	public void save(){
		
		//write the accounts
		ConfigurationSection accountsSection = config.createSection("accounts");
		for (Account account: accounts.values()){
			accountsSection.set(account.getOwner(), account.getBalance());
			
		}
		
		//save the file
		try {
			BetterEconomy.config.save(BetterEconomy.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * load economy data from file
	 */
	public void load(){
		
		//make sure the config section has an accounts section to load
		if (!config.contains("accounts")){
			config.createSection("accounts");
		}
		
		//get the accounts
		ConfigurationSection accountsSection = config.getConfigurationSection("accounts");
		
		//clear the currently kept accounts
		accounts.clear();
		
		//read the new accounts
		for (String accountName: accountsSection.getKeys(false)){
			accounts.put(accountName, new Account(accountName, accountsSection.getDouble(accountName)));
		}
	}
	
	/**
	 * Sets up the known physical currencies for the economy.
	 * Normal currencies are added via economy.addCurrency
	 * Ores are not tradable, but they still need to be removed from player inventories
	 * for special cases, like deducting certain values from a player (Wilderness)
	 */
	private void setupCurrencies() {
		
		//clear existing currencies
		clearCurrencies();
		
		//add coal
		addCurrency(new Currency("coal", new ItemStack(Material.COAL), 10, true));
		addCurrency(new Currency("coal_block", new ItemStack(Material.COAL_BLOCK), 90, true));
		addCurrency(new Currency("coal_ore", new ItemStack(Material.COAL_ORE), 22, false));
		
		//add iron
		addCurrency(new Currency("iron", new ItemStack(Material.IRON_INGOT), 20, true));
		addCurrency(new Currency("iron_block", new ItemStack(Material.IRON_BLOCK), 180, true));
		addCurrency(new Currency("iron_ore", new ItemStack(Material.IRON_ORE), 20, false));
		
		//add gold
		addCurrency(new Currency("gold",  new ItemStack(Material.GOLD_INGOT), 200, true));
		addCurrency(new Currency("gold_block", new ItemStack(Material.GOLD_BLOCK), 1800, true));
		addCurrency(new Currency("gold_ore", new ItemStack(Material.GOLD_ORE), 200, false));
		addCurrency(new Currency("gold_nugget",  new ItemStack(Material.GOLD_NUGGET), (int)Math.floor(200/9), false));
		
		//add diamond
		addCurrency(new Currency("diamond", new ItemStack(Material.DIAMOND), 500, true));
		addCurrency(new Currency("diamond_block", new ItemStack(Material.DIAMOND_BLOCK), 4500, true));
		addCurrency(new Currency("diamond_ore", new ItemStack(Material.DIAMOND_ORE), 1100, false));
		
		//add lapis
		Dye lapis = new Dye();
		lapis.setColor(DyeColor.BLUE);
		ItemStack inc = new ItemStack(Material.INK_SACK);
		inc.setData(lapis);
		addCurrency(new Currency("lapis", new ItemStack(inc), 55, true));
		addCurrency(new Currency("lapis_block", new ItemStack(Material.LAPIS_BLOCK), 495, true));
		addCurrency(new Currency("lapis_ore", new ItemStack(Material.LAPIS_ORE), 120, false));
		
		//add redstone
		addCurrency(new Currency("redstone", new ItemStack(Material.REDSTONE), 15, true));
		addCurrency(new Currency("redstone_block", new ItemStack(Material.REDSTONE_BLOCK), 135, true));
		addCurrency(new Currency("redstone_ore", new ItemStack(Material.REDSTONE_ORE), 33, false));
		
		//add quartz
		addCurrency(new Currency("quartz", new ItemStack(Material.QUARTZ), 25, true));
		addCurrency(new Currency("quartz_ore", new ItemStack(Material.QUARTZ_ORE), 55, false));
	}
	
	//----------------------
	//END OF FILE-BASED METHODS
	//----------------------
	
	//----------------------
	//START OF PLAYER COMMAND METHODS
	//----------------------
	
	/**
	 * Shows a player his or her current balance
	 * @param sender The player executing the command
	 */
	public void showBalance(CommandSender sender) {
		if (!(hasAccount(sender.getName()))){
			sender.sendMessage("Error: for some reason you do not have an account");
			return;
		}
		
		sender.sendMessage("Your current balance is: $" + ((int)getBalance(sender.getName())));
	}
	
	/**
	 * Allows a player to withdraw an amount of physical currency into his or her inventory, given he or she has the needed funds
	 * @param sender The player executing the command
	 * @param currency The name of the physical currency specified
	 * @param amount The amount of physical currency to be withdrawn
	 */
	public void withdraw(CommandSender sender, String currencyName, int amount) {
		
		//make sure the amount specified was a positive number
		if (amount <= 0){
			sender.sendMessage("Please enter a positive amount");
			return;
		}
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to withdraw");
			return;
		}
		
		Currency currency = currencies.get(currencyName);
		if (currency == null){
			sender.sendMessage("Bad currency name");
			return;
		}
		
		//make sure player has enough funds
		if (getBalance(sender.getName()) < currency.getValue(amount)){
			sender.sendMessage("Sorry, you dont have enough money");
			return;
		}
		
		Inventory playerInventory = ((Player) sender).getInventory();
		
		//checks for enough space to withdraw
		if (countRoomForCurrency(playerInventory, currency) < amount){
			sender.sendMessage("Sorry, there is not enough space in your inventory.");
			return;
		}
		
		//if not enough currency in the bank
		if(BetterEconomy.bank.getCurrencyAmount(currency) < amount){
			sender.sendMessage("Not enough " + currency + " in the bank");
			return;
		}
		
		//add items to inventory
		if(addCurrency(playerInventory, currency, amount) != 0){
			sender.sendMessage("Something bad happened!");
			return;
		}
		
		//remove funds
		this.withdrawPlayer(sender.getName(), currency.getValue(amount));
		sender.sendMessage(amount + " " + currency + " was withdrawn.");
		
		return;
	}
	
	/**
	 * Greedily withdraws a currency from a sender's account
	 * @param sender The individual sending the command
	 * @param currency The currency to be withdrawn
	 */
	public void greedyWithdraw(CommandSender sender, Currency currency){
		int amount = (int) java.lang.Math.floor(getBalance(sender.getName()) / currency.getValue(1));
	
		Inventory playerInventory = ((Player) sender).getInventory();
		
		//checks for max space for currency withdraw
		int i = amount;
		if (countRoomForCurrency(playerInventory, currency) < amount){
			i = countRoomForCurrency(playerInventory, currency);
		}
		
		if(addCurrency(playerInventory, currency, i) != 0){
			sender.sendMessage("Not enough " + currency.getName() + " in the bank");
			return;
		}
		
		//remove funds
		this.withdrawPlayer(sender.getName(), currency.getValue(amount));
		sender.sendMessage(i + " " + currency.getName() + " was withdrawn.");
		return;
	}
	
	/**
	 * Allows a player to deposit an amount of physical currency into his or her account, given he or she has the currency
	 * @param sender The player executing the command
	 * @param currency The name of the physical currency specified
	 * @param amount The amount of physical currency to be deposited
	 */
	public void deposit(CommandSender sender, String currencyName, int amount){
		
		//make sure amount specified was a positive number
		if (amount <= 0){
			sender.sendMessage("Please enter a positive amount");
			return;
		}
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to deposit items");
			return;
		}
		
		Currency currency = this.getCurrency(currencyName);
		
		//get the players inventory
		Inventory playerInventory = ((Player) sender).getInventory();
		
		//check inventory
		int i = countCurrency(playerInventory, currency);
		
		//make sure enough was found
		if (i < amount){
			sender.sendMessage("Sorry, you dont have enough " + currency);
			return;
		}
		
		//remove items
		removeCurrency(playerInventory, currency, amount);
						
		//add funds
		this.depositPlayer(sender.getName(), currency.getValue(amount));
		sender.sendMessage(amount + " " + currency + " was deposited.");
		return;	
	}
	
	/** 
	 *  @Author Timmy Miles
	 *  Allows a player to deposit all currency in his/her inventory to his/her account
	 *  Derivation of Lucas's addition using an arraylist
	 *  @param sender The player executing the command
	 *  @return void
	 */
	 public void depositEverything(CommandSender sender){	 	
	 	for(String currencyName: currencies.keySet()){
	 		depositAll(sender, currencyName);	 	
	 	}
	 
	 	return; 
	 }
	 
	 /**
	  * Deposits all instances of a currency in the sender's inventory
	  * @param sender The individual sending the command
	  * @param currency The currency to be deposited
	  */
	 public void depositAll(CommandSender sender, String currencyName){
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to deposit items");
			return;
		}
		
		//get the players inventory
		Inventory playerInventory = ((Player) sender).getInventory();
		
		Currency currency = getCurrency(currencyName);
		
		//check inventory
		int amount = countCurrency(playerInventory, currency);
		
		removeCurrency(playerInventory, currency, amount);
						
		//add funds
		depositPlayer(sender.getName(), currency.getValue(amount));
		
		if (amount != 0){
			sender.sendMessage(amount + " " + currency + " was deposited.");
		}
		return;
	}
	 
	/**
	 * Checks the value of a given amount of a given currency
	 * @param sender The player executing the command
	 * @param name The name of the currency
	 * @param amount The amount specified
	 */
	public void checkValue(CommandSender sender, String currencyName, int amount){
		Currency currency = getCurrency(currencyName);
		if (currency == null){
			sender.sendMessage("Invalid currency");
		}
		
		String message = amount + " " + currencyName + " is worth $" + (currency.getValue(amount));
		sender.sendMessage(message);
	}
	
	public void calculateWealth(CommandSender sender){
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//get the players inventory
		Inventory inv = ((Player) sender).getInventory();
		
		double wealth = 0;
		for (Currency currency: currencies.values()){
			wealth += currency.getValue(this.countCurrency(inv, currency));
		}
		
		//tell the player
		sender.sendMessage("You are carrying $" + wealth + " worth in materials");
	}
	/**
	 * Sets a players balance. Can only be executed by the server
	 * @param server Server
	 * @param player Player to set balance
	 * @param amount amount to be set
	 */
	public void setBalance(CommandSender server, String playerName, double amount) {

		//make sure its the server
		if (!(server instanceof ConsoleCommandSender)){
			server.sendMessage("Sorry, only the server can execute this command");
			return;
		}
		
		Account account = accounts.get(playerName);
		if (account == null){
			server.sendMessage("No account for " + playerName);
		}
		
		account.setBalance(amount);
		
		server.sendMessage("Account was set");
	}

	public void pay(CommandSender sender, String receiver, double amount) {

		if (!hasAccount(sender.getName())){
			sender.sendMessage("I'm sorry, but you do not have an account");
			return;
		}
		
		if (!hasAccount(receiver)){
			sender.sendMessage("I'm sorry, but " + receiver + " does not have an account");
			return;
		}
		
		//make sure sender has enough money
		Account sendersAccount = accounts.get(sender.getName());
		if (sendersAccount.getBalance() < amount){
			sender.sendMessage("I'm sorry, but you do not have that much money in your account");
			return;
		}
		
		//take money from the sender
		sendersAccount.withdraw(amount);
		
		//pay the player
		Account receiverAccount = accounts.get(receiver);
		receiverAccount.deposit(amount);
		
		//notify both players
		sender.sendMessage("You have payed " + receiver + " $" + amount + " dollars");
		Bukkit.getPlayer(receiver).sendMessage("" + sender.getName() + " has payed you $" + amount + " dollars");
		
	}

	public void top(CommandSender sender, int number) {

		sender.sendMessage("here are the top " + number + " accounts on the server:");
		
		List<Account> sortedList = new LinkedList<Account>();
		sortedList.addAll(accounts.values());
		
		java.util.Collections.sort(sortedList, new BalanceComparator());
		java.util.Collections.reverse(sortedList);
		
		int i = 0;
		for (Account a: sortedList){
			sender.sendMessage(a.getOwner() + ": " + a.getBalance());
			i++;
			if (i == number){
				return;
			}
		}
		
	}

	/**
	 * Counts all currencies in a region and prints the result as a percentage
	 * of all blocks in a selection
	 * @param sender the player who issued the command that calls this function
	 */
	public void evaluateCurrencies(CommandSender sender){
		
		Map<Currency,Integer> map = new HashMap<Currency,Integer>();
		
		for(Currency c: currencies.values()){
			map.put(c,0);
		}
		
		//get the WorldEdit selection
		Selection selection = BetterEconomy.weplugin.getSelection((Player) sender);
		BlockVector b1 = new BlockVector(selection.getMinimumPoint().getX(), selection.getMinimumPoint().getY(), selection.getMinimumPoint().getZ());
		BlockVector b2 = new BlockVector(selection.getMaximumPoint().getX(), selection.getMaximumPoint().getY(), selection.getMaximumPoint().getZ());
		
		//calculate volume of selection
		double height = selection.getMaximumPoint().getY() - selection.getMinimumPoint().getY() + 1;
		double length = selection.getMaximumPoint().getX() - selection.getMinimumPoint().getX() + 1;
		double width = selection.getMaximumPoint().getZ() - selection.getMinimumPoint().getZ() + 1;
		double volume = height * length * width;
		
		//build the map by checking all blocks in the selection
		for (int x = b1.getBlockX(); x < b2.getBlockX() + 1; x++){
			for (int y = b1.getBlockY(); y < b2.getBlockY() + 1; y++){
				for (int z = b1.getBlockZ(); z < b2.getBlockZ() + 1; z++){
					Material b = Bukkit.getWorld(selection.getWorld().getName()).getBlockAt(x,y,z).getType();

					for(Currency c: currencies.values()){
						if(c.getItem().getType().equals(b)){
							map.put(c, map.get(c)+1);
						}
					}
				}
			}
		}
		
		for(Currency c: currencies.values()){
			System.out.println(c.getName() + ": " + map.get(c) + "/" + volume);
			sender.sendMessage(c.getName() + ": " + map.get(c) + "/" + volume);
		}
		
		return;
	}
	
	//----------------------
	//END OF PLAYER COMMAND METHODS
	//----------------------
	
	//----------------------
	//START OF CURRENCY METHODS
	//----------------------
	
	/**
	 * Add a physical currency to the list of valid currencies
	 * @param newCurrency the new Currency type to be added
	 */
	public void addCurrency(Currency newCurrency){
		currencies.put(newCurrency.getName(), newCurrency);
	}
	
	/**
	 * Gets a physical currency from the economy
	 * @param currencyName the name of the currency
	 * @return
	 */
	public Currency getCurrency(String currencyName){
		return currencies.get(currencyName);
	}
	
	/**
	 * Returns all of the currencies
	 * @return all of the currencies
	 */
	public Map<String, Currency> getCurrencies(){
		return currencies;
	}
	
	/**
	 * Checks if an ItemStack is a specified currency
	 * @param stack ItemStack
	 * @param currencyName Currency name
	 * @return true if so, false if not
	 */
	public boolean isCurrency(ItemStack stack, String currencyName){
		if (stack.getData().equals(currencies.get(currencyName).getItem().getData()) 
			&& stack.getItemMeta().equals(currencies.get(currencyName).getItem().getItemMeta())){
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if an ItemStack is a currency
	 * @param item
	 * @return
	 */
	public boolean isCurrency(ItemStack item) {
		for (Currency currency: currencies.values()){
			if (isCurrency(item, currency.getName())){
				return true;
			}
		}
		return false;
	}
	
	public void clearCurrencies(){
		currencies.clear();
	}
	
	//----------------------
	//END OF CURRENCY METHODS
	//----------------------
	
	//----------------------
	//START OF INVENTORY METHODS
	//----------------------
	
	/**
	 * Counts the number of a currency is in a given inventory
	 * @param inv The inventory to be checked
	 * @param currency The currency to be counted
	 * @return The amount of currency in the inventory
	 */
	public int countCurrency(Inventory inv, Currency currency){
		
		int total = 0;
		
		//for the inventory spots
		for (ItemStack stack : inv.getContents()){
			
			//make sure the stack is non-empty
			if (stack != null){
				
				//check if the stack is the given currency type
				if (isCurrency(stack, currency.getName())){
					
					//add the amount of currency
					total += stack.getAmount();
				}
			}
		}
		
		return total;
	}
	
	/**
	 * Counts the room available in a given inventory for a given currency
	 * @param inv The inventory to be checked
	 * @param currency for which space is being checked
	 * @return
	 */
	public int countRoomForCurrency(Inventory inv, Currency currency){
	
		int total = 0;
		
		//for the inventory spots
		for (ItemStack stack : inv.getContents()){
			
			//if the spot is empty, add 64
			if (stack == null){
				total += 64;
			}
			else{
				
				//if the spot if of the currency type, see if we can top off the stack
				if (isCurrency(stack, currency.getName())){
					
					//add the remainder
					total += (64 - stack.getAmount());
				}
			}
		}
		
		return total;
	}
	
	/**
	 * Adds a number of a physical currency to an inventory
	 * @param inv The inventory to be added to
	 * @param currency The currency to be added
	 * @param amount The amount of currency to add
	 */
	public int addCurrency(Inventory inv, Currency currency, int amount){
		
		int remaining = amount;
		
		//for the inventory spots
	 	for (ItemStack stack : inv.getContents()){
	 		
	 		//if there is an empty spot, add as much currency we can
	 		if (stack == null){
	 			
	 			//add a full stack, or the remaining if not a full stack remains
	 			int amountToAdd = java.lang.Math.min(remaining, 64);
	 			
	 			//add a new ItemStack
	 			ItemStack item = currency.getItem().clone();
	 			item.setAmount(amountToAdd);
	 			inv.addItem(item);
	 			
	 			//update remaining counter
	 			remaining -= amountToAdd;
	 		}
	 		
	 		//stack is not empty, but check if it can be toped off
	 		else{
				if (isCurrency(stack, currency.getName())){
					
					//either top off the stack, or add the remaining if it won't top off a stack
					int amountToAdd = java.lang.Math.min(64 - stack.getAmount(), remaining);
					
					//add a new ItemStack
		 			stack.setAmount(stack.getAmount() + amountToAdd);
					
		 			//update remaining counter
		 			remaining -= amountToAdd;
				}
	 		}
	 		
			//stop checking spots if nothing remains
			if (remaining <= 0){
				break;
			}
			
		}
	 	
	 	//returns the amount of currency that was not put into the inventory
	 	//0 on success, negative means too much of the currency was added (should never happen)
	 	return remaining;
	 }
	 
	/**
 	* Removes an amount of physical currency from a given inventory
 	* @param inv The inventory from which the currency is removed
 	* @param currency The currency to be removed
 	* @amount The ammount of currency to be removed
 	*/
	public int removeCurrency(Inventory inv, Currency currency, int amount){
		
		//a little barbaric, but it works
		int inventorySpot = 0;
		
		int remaining = amount;
		
		//for the inventory spots
		for (ItemStack stack : inv.getContents()){
			
			//make sure the stack is not null
			if (stack != null){
				
				//if the stack is of the same type as the currency being removed
				if (isCurrency(stack, currency.getName())){
					
					int amountToRemove;
					
					//only remove the remaining
					if (remaining < stack.getAmount()){
						amountToRemove = remaining;
						stack.setAmount(stack.getAmount() - remaining);
					}
					
					//remove the whole stack
					else{
						amountToRemove = stack.getAmount();
						inv.setItem(inventorySpot, null);	
					}
					
					//update remaining counter
					remaining -= amountToRemove;
				}
			}
			
			if (remaining <= 0){
				break;
			}
			
			inventorySpot++;
		}
		
		return remaining;
	}
	
	/**
	 * Calculates the sum of all physical currencies in the player's inventory
	 * @param sender The player executing the command
	 */
	public int calculateValue(Inventory inv){
		
		//calculate wealth
		int value = 0;
		for (ItemStack stack : inv.getContents()){
			if (stack != null){
				for (Currency c: currencies.values()){
					if (isCurrency(stack, c.getName())){
						value += c.getValue(stack.getAmount());
					}
				}
			}
		}
		
		return value;
	}
	
	//----------------------
	//END OF INVENTORY METHODS
	//----------------------
	
	//----------------------
	//START OF VAULT MATHODS
	//----------------------
	/**
     * Returns the amount the bank has
     * @param economyName
     * @return EconomyResponse Object
     */
	@Override
	public EconomyResponse bankBalance(String arg0) {
		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
	}

	/**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param economyName
     * @param amount
     * @return EconomyResponse Object
     */
	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
	}

	/**
     * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param economyName
     * @param amount
     * @return EconomyResponse Object
     */
	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {
		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
	}

	/**
     * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param economyName
     * @param amount
     * @return EconomyResponse Object
     */
	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
	}

	/**
     * Creates a bank account with the specified name and the player as the owner
     * @param economyName
     * @param player
     * @return EconomyResponse Object
     */
	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
	}

	/**
     * Attempts to create a player account for the given player
     * @return if the account creation was successful
     */
	@Override
	public boolean createPlayerAccount(String playerName) {
		
		//make sure player doesn't already have an account
		if (this.hasAccount(playerName)){
			return false;
		}
		
		//create a new account
		accounts.put(playerName, new Account(playerName, startingAmount));
		return true;
	}

	/**
     * Attempts to create a player account for the given player on the specified world
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @return if the account creation was successful
     */
	@Override
	public boolean createPlayerAccount(String playerName, String world) {
		return createPlayerAccount(playerName);
	}

	/**
     * Returns the name of the currency in plural form.
     * If the economy being used does not support currency names then an empty string will be returned.
     * 
     * @return name of the currency (plural)
     */
	@Override
	public String currencyNamePlural() {
		return currencyName + "s";
	}

	/**
     * Returns the name of the currency in singular form.
     * If the economy being used does not support currency names then an empty string will be returned.
     * 
     * @return name of the currency (singular)
     */
	@Override
	public String currencyNameSingular() {
		return currencyName;
	}

	/**
     * Deletes a bank account with the specified name.
     * @param economyName
     * @return if the operation completed successfully
     */
	@Override
	public EconomyResponse deleteBank(String arg0) {
		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
	}

	/**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param playerName Name of player
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {
		
		//make sure the player has an account
		if (!(hasAccount(playerName))){
			return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "No account exists for the player " + playerName);
		}
		
		//get the account
		Account account = accounts.get(playerName);
		
		//deposit from the account
		account.deposit(amount);
		
		//return success
		return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "Successfully removed "+ amount +" from " + playerName + "'s account");
	}

	/**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @param playerName Name of player
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
	@Override
	public EconomyResponse depositPlayer(String playerName, String world, double amount) {
		return depositPlayer(playerName, amount);
	}

	/**
     * Format amount into a human readable String This provides translation into
     * economy specific formatting to improve consistency between plugins.  
     *
     * For now, we just add a dollar sign. I don't know if we should
     *
     * @param amount
     * @return Human readable string describing amount
     */
	@Override
	public String format(double arg0) {
		return "$" + arg0;
	}

	/**
     * Some economy plugins round off after a certain number of digits.
     * This function returns the number of digits the plugin keeps
     * or -1 if no rounding occurs.
     * 
     * We round after decimalPlaces decimal places
     * 
     * @return decimalPlaces
     */
	@Override
	public int fractionalDigits() {
		return decimalPlaces;
	}

	/**
     * Gets balance of a player
     * @param playerName
     * @return Amount currently held in players account
     */
	@Override
	public double getBalance(String playerName) {
		return accounts.get(playerName).getBalance();
	}

	/**
     * Gets balance of a player on the specified world.
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @param playerName
     * @param world name of the world
     * @return Amount currently held in players account
     */
	@Override
	public double getBalance(String playerName, String world) {
		return getBalance(playerName);
	}

	/**
     * Gets the list of banks
     * @return the List of Banks
     */
	@Override
	public List<String> getBanks() {
		List<String> list = new LinkedList<String>();
		list.addAll(banks.keySet());
		return list;
	}

	/**
     * Gets name of economy method
     * @return Name of Economy Method
     */
	@Override
	public String getName() {
		return economyName;
	}

	/**
     * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param playerName
     * @param amount
     * @return True if <b>playerName</b> has <b>amount</b>, False else wise
     */
	@Override
	public boolean has(String playerName, double amount) {
		return accounts.get(playerName).getBalance() >= amount;
	}

	/**
     * Checks if the player account has the amount in a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @param playerName
     * @param worldName
     * @param amount
     * @return True if <b>playerName</b> has <b>amount</b>, False else wise
     */
	@Override
	public boolean has(String playerName, String world, double amount) {
		return has(playerName, amount);
	}

	/**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     * @param playerName
     * @return if the player has an account
     */
	@Override
	public boolean hasAccount(String playerName) {
		return accounts.containsKey(playerName);
	}

	/**
     * Checks if this player has an account on the server yet on the given world
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     * 
     * Note that we don't support multi-world accounts yet.
     * 
     * @param playerName
     * @return if the player has an account
     */
	@Override
	public boolean hasAccount(String playerName, String world) {
		return hasAccount(playerName);
	}

	/**
     * Returns true if the given implementation supports banks.
     * 
     * Right now, banks are not supported.
     * 
     * @return false
     */
	@Override
	public boolean hasBankSupport() {
		return false;
	}

	/**
     * Check if the player is a member of the bank account
     * @param economyName
     * @param playerName
     * @return EconomyResponse Object
     */
	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
	}

	/**
     * Check if a player is the owner of a bank account
     * @param economyName
     * @param playerName
     * @return EconomyResponse Object
     */
	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
	}

	/**
     * Checks if economy method is enabled.
     * @return true
     */
	@Override
	public boolean isEnabled() {
		return true;
	}

	 /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
     * 
     * @param playerName Name of player
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {
		
		//make sure the player has an account
		if (!(hasAccount(playerName))){
			return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "No account exists for the player " + playerName);
		}
		
		//get the account
		Account account = accounts.get(playerName);
		
		//withdraw from the account
		account.withdraw(amount);
		
		//return success
		return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "Successfully took "+ amount + " from " + playerName + "'s account");
	}

	/**
     * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @param playerName Name of player
     * @param worldName - name of the world
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
	@Override
	public EconomyResponse withdrawPlayer(String playerName, String world, double amount) {
		return withdrawPlayer(playerName, amount);
	}
	
	//----------------------
	//END OF VAULT MATHODS
	//----------------------
}
