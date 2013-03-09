package com.m0pt0pmatt.bettereconomy;

import java.io.Serializable;

/**
 * 
 * @author Matthew
 *
 */
public class Debt implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double amount = 0;
	private String payer;
	private String receiver;
	private boolean isPayed = false;
	
	public Debt(String payer, String receiver, double amount){
		if (amount >= 0){
			this.payer = payer;
			this.receiver = receiver;
			this.amount = amount;
		}
		else{
			this.payer = receiver;
			this.receiver = payer;
			this.amount = -amount;
		}
		if (amount == 0){
			isPayed = true;
		}
		else{
			isPayed = false;
		}
	}
	
	public double payDebt(double amount){
		this.amount -= amount;
		if (this.amount < 0){
			double extra = -this.amount;
			this.amount = 0;
			return extra;
		}
		
		return 0;
	}
	
	public void addDebt(double amount){
		this.amount += amount;
	}
	
	public void setAmount(double amount){
		this.amount = amount;
	}
	
	public boolean isPayed(){
		return isPayed;
	}
	
	public String getPayer(){
		return payer;
	}
	
	public String getReceiver(){
		return receiver;
	}
	
	public double getAmount(){
		return amount;
	}
}
