package com.m0pt0pmatt.bettereconomy;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

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
	 * MaterialData of the currency
	 */
	private MaterialData material;
	
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
	public Currency(String name, MaterialData material, int value){
		this.name = name;
		this.material = material;
		this.value = value;
	}
	
	public Currency(String name, Material material, int value){
		this.name = name;
		this.material = new MaterialData(material);
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
	 * Returns the MaterialData of the currency
	 * @return the MaterialData of the currency
	 */
	public MaterialData getMaterial(){
		return material;
	}

	/**
	 * Returns the value of the currency
	 * @return the value of the currency
	 */
	public int getValue(int amount) {
		return (amount * value);
	}
}
