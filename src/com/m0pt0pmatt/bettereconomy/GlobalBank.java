package com.m0pt0pmatt.bettereconomy;

import java.util.HashMap;

public class GlobalBank {
	
	//A map from a currency to the amound of that currency currently stored
	HashMap<Currency,Integer> amounts;
	
	public GlobalBank(){
		this(new HashMap<Currency,Integer>());
	}

	public GlobalBank(HashMap<Currency,Integer> m){
		amounts = m;
	}
	
	/**
	 * @return map of currencies to their amounts
	 */
	public HashMap<Currency,Integer> getMap(){
		return amounts;
	}
	
	/**
	 * Adds a currency to the bank (initial amount = 0)
	 * @param c currency to be added
	 */
	public void addCurrency(Currency c){
		amounts.put(c,0);
	}
	
	/**
	 * @param c currency whose amount is desired
	 * @return amount of a currency in the bank, null if currency is not in the bank
	 */
	public Integer getCurrencyAmount(Currency c){
		return amounts.get(c);
	}
	
	/**
	 * Removes a currency from the bank
	 * @param c currency to be removed
	 */
	public void removeCurrency(Currency c){
		amounts.remove(c);
	}
	
	/**
	 * Updates the amount of a given currency
	 * @param c currency to be updated
	 * @param amount new amount of the currency
	 */
	public void updateAmount(Currency c, Integer amount){
		amounts.put(c,amount);
	}
}
