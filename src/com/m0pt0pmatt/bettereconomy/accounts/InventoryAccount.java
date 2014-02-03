package com.m0pt0pmatt.bettereconomy.accounts;


public class InventoryAccount extends Account{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InventoryAccount(String owner){
		super(owner);
	}
	
	public InventoryAccount(String owner, double balance){
		super(owner, balance);
	}

}
