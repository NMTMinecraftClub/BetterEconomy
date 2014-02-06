package com.m0pt0pmatt.bettereconomy.util;

import org.bukkit.event.Listener;

import com.m0pt0pmatt.bettereconomy.BetterEconomy;

/**
 * class for separate thread. Saves accounts every once in a while
 * @author Matthew
 */
public class FileSavingThread extends Thread implements Listener{
	
	public boolean die = false;
	private boolean isRunning = true;
	public final long sleepTime = 1000 * 60 * 15; //15 minutes
	
	@Override
	public void run(){
		while(!die){
			
			long wait = 0;
			
			while (wait < sleepTime){
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (die){
					isRunning = false;
					return;
				}
				wait += 3000;
			}
			
			//save everything
			BetterEconomy.save();
		}
		
	}
	
	public boolean isRunning(){
		return isRunning;
	}
}