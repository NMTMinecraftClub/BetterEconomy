package com.m0pt0pmatt.bettereconomy.accounts;

import java.io.Serializable;

public abstract class Account implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private double balance;
	private String owner;
	
	/**
	 * Creates a new account with a balance of 0
	 * @param owner name of the account owner
	 */
	public Account(String owner){
		this(owner, 0);
	}
	
	/**
	 * Creates a new account
	 * @param owner name of the account owner
	 * @param balance initial balance of the account
	 */
	public Account(String owner, double balance){
		this.setOwner(owner);
		this.setBalance(balance);
		
	}
	
	/**
	 * Returns the balance of the account
	 * @return the balance of the account
	 */
	public double getBalance(){
		return balance;
	}
	
	/**
	 * Returns the name of the owner of the account
	 * @return the name of the owner of the account
	 */
	public String getOwner(){
		return owner;
	}
	
	/**
	 * Sets the balance
	 * @param balance amount to set the balance to
	 */
	public void setBalance(double balance){
		this.balance = balance;
	}
	
	/**
	 * Sets the owner
	 * @param owner new owner to be set
	 */
	public void setOwner(String owner){
		this.owner = owner;
	}
	
	/**
	 * Deposits a given amount into the account
	 * @param amount the amount specified
	 */
	public void deposit(double amount){
		if (amount > 0){
			this.balance += amount;
		}
	}
	
	/**
	 * Withdraws a given amount from the account, only if enough money can be withdrawn
	 * @param amount the amount to be withdrawn
	 * @return true if there was enough money in the account to withdraw, false if not
	 */
	public boolean withdraw(double amount){
		if (amount > 0){
			if (balance >= amount){
				this.balance -= amount;
				return true;
			}
		}
		return false;
	}
}
