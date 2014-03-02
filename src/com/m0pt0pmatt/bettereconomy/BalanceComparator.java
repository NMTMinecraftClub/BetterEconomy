package com.m0pt0pmatt.bettereconomy;

import java.util.Comparator;

import com.m0pt0pmatt.bettereconomy.accounts.Account;

public class BalanceComparator implements Comparator<Account>{
	   
    public int compare(Account account1, Account account2){
       
        double emp1Balance = account1.getBalance();        
        double emp2Balance = account2.getBalance();
       
        if(emp1Balance > emp2Balance)
            return 1;
        else if(emp1Balance < emp2Balance)
            return -1;
        else
            return 0;    
    }
}
