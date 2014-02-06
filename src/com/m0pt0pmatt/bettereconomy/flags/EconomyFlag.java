package com.m0pt0pmatt.bettereconomy.flags;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;

/**
 * All Worldguard flags for BetterEconomy
 * @author Matthew
 *
 */
public enum EconomyFlag {

	BANKFLAG( new StateFlag("bank", false));
	
	private Flag<?> flag;
	
	private EconomyFlag(Flag<?> flag){
		this.flag = flag;
	}
	
	public Flag<?> getFlag(){
		return flag;
	}
	
}
