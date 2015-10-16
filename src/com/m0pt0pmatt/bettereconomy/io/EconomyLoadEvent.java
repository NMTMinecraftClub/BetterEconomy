package com.m0pt0pmatt.bettereconomy.io;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.m0pt0pmatt.bettereconomy.EconomyManager;

/**
 * Event signaling that an event has finished.<br />
 * Waves should be certain to only send this once.
 * @author Skyler
 *
 */
public class EconomyLoadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	private EconomyManager economy;
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	/**
	 * Creates a wave finish event, which houses which wave has finished
	 * @param wave The wave that finished
	 */
	public EconomyLoadEvent(EconomyManager econ) {
		economy = econ;
	}
	
	/**
	 * Returns the wave that signaled this event.
	 * @return
	 */
	public EconomyManager getEconomy() {
		return economy;
	}
}
