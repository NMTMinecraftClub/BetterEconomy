package com.m0pt0pmatt.bettereconomy.currency;

import org.bukkit.inventory.ItemStack;

/**
 * Defines a physical currency
 * @author Matthew
 *
 */
public class Currency {
	
	private final String name;		//name of the physical currency
	private final ItemStack item;	//actual representation of the currency
	private final int value;		//value of the currency
	private final boolean tradeable;//whether or not the currency can be exchanged at a bank
	
	public Currency(String name, ItemStack item, int value, boolean tradeable){
		this.name = name;
		this.item = item;
		this.value = value;
		this.tradeable = tradeable;
	}
	
	public String getName(){
		return name;
	}
	
	public ItemStack getItem(){
		return item;
	}

	public int getValue() {
		return value;
	}
	
	public int getValue(int amount) {
		return (amount * value);
	}

	public boolean isTradeable() {
		return tradeable;
	}
}
