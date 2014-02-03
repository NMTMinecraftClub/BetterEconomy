package com.m0pt0pmatt.bettereconomy.util;

import org.bukkit.event.Listener;

import com.m0pt0pmatt.bettereconomy.BetterEconomy;

/**
 * class for separate thread. Saves accounts every once in a while
 * @author Matthew
 */
public class FileSavingThread extends Thread implements Listener{
	
	@Override
	public void run(){

		while(true){
			//save everything
			BetterEconomy.saveAll();
			try {
				Thread.sleep(1000 * 60 * 15);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}