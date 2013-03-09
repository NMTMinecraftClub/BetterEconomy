package com.m0pt0pmatt.bettereconomy;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public abstract class Account implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private double balance;
	private String owner;
	private List<Debt> debts;
	
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
		
		//create an empty list of debts
		debts = new LinkedList<Debt>();
	}

	/**
	 * Returns the list of debts
	 * @return the list of debts
	 */
	public List<Debt> getDebts(){
		return debts;
	}
	
	/**
	 * Adds a debt to the list
	 * @param debt debt to be added
	 */
	public void addDebt(Debt debt){
		debts.add(debt);
	}
	
	/**
	 * removes a debt from the list
	 * @param debt debt to be removed
	 */
	public void removeDebt(Debt debt){
		debts.remove(debt);
	}
	
	/**
	 * Forgives a given payer of all debts he or she owns the account
	 * @param payer the given payer to be forgiven
	 */
	public void removeDebt(String payer){
		for (Debt d: debts){
			if (d.getPayer() == payer && d.getReceiver() == owner){
				debts.remove(d);
			}
		}
	}
	
	/**
	 * Checks if the owner of the account owes a given player
	 * @param receiver the player that the owner might own
	 * @return true if the owner owes the receiver, false if not
	 */
	public boolean owes(String receiver){
		for (Debt d: debts){
			if (d.getPayer() == owner && d.getReceiver() == receiver){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the given player owes the owner
	 * @param payer the player that might owe the owner
	 * @return true if the given player owes the owner
	 */
	public boolean payedBy(String payer){
		for (Debt d: debts){
			if (d.getPayer() == payer && d.getReceiver() == owner){
				return true;
			}
		}
		return false;
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
