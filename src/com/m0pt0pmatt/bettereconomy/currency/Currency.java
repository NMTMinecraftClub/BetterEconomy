package com.m0pt0pmatt.bettereconomy.currency;

import org.bukkit.inventory.ItemStack;

/**
 * Defines a physical currency
 * @author Matthew
 *
 */
public class Currency {

	/**
	 * name to be given to the currency
	 */
	private String name;
	
	private ItemStack item;
	
	/**
	 * dollar value of currency
	 */
	private int value;
	
	public Currency(String name, ItemStack item, int value){
		this.name = name;
		this.item = item;
		this.value = value;
	}
	
	/**
	 * Returns the name of the currency
	 * @return the name of the currency
	 */
	public String getName(){
		return name;
	}
	
	public ItemStack getItem(){
		return item;
	}

	/**
	 * Returns the value of the currency
	 * @return the value of the currency
	 */
	public int getValue(int amount) {
		return (amount * value);
	}
}
