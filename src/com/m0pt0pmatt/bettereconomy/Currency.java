package com.m0pt0pmatt.bettereconomy;

import org.bukkit.Material;

/**
 * Defines a physical currency
 * @author Matthew
 *
 */
public class Currency {

	/**
	 * name to be given to the currency
	 */
	private String name;
	
	/**
	 * Material of the currency
	 */
	private Material material;
	
	/**
	 * Material data for the currency (This is to solve lapis lazuli)
	 */
	private byte data;
	
	/**
	 * dollar value of currency
	 */
	private int value;
	
	/**
	 * Default Constructor
	 * @param name name to be given to the currency
	 * @param material Material of the currency
	 * @param materialData Material data for the currency (This is to solve lapis lazuli)
	 * @param value dollar value of currency
	 */
	public Currency(String name, Material material, int data, int value){
		this.name = name;
		this.material = material;
		if (data != -1){
			this.data = (byte) data;
		}
		else{
			this.data = -1;
		}
		this.value = value;
	}
	
	/**
	 * Returns the name of the currency
	 * @return the name of the currency
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Returns the Material of the currency
	 * @return the Material of the currency
	 */
	public Material getMaterial(){
		return material;
	}
	
	/**
	 * Returns the MaterialData of the currency
	 * @return the MaterialData of the currency
	 */
	public byte getMaterialData(){
		return data;
	}

	/**
	 * Returns the value of the currency
	 * @return the value of the currency
	 */
	public int getValue(int amount) {
		return (amount * value);
	}
}
