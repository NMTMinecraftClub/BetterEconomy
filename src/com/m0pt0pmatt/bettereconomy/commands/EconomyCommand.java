package com.m0pt0pmatt.bettereconomy.commands;

public enum EconomyCommand {
	SETBALANCE("setbalance", "bettereconomy.setbalance", true, false),
	EVALUATEECONOMY("evaluateeconomy", "bettereconomy.evaluate", true, false),
	CREATEBANK("createbank", "bettereconomy.createbank", true, false),
	PAY("pay", "bettereconomy.pay", false, false),
	TOP("top", "bettereconomy.top", false, false),
	VALUE("value", "bettereconomy.value", false, false),
	MONEY("money", "bettereconomy.money", false, false),
	DEPOSIT("deposit", "bettereconomy.deposit", false, true),
	WITHDRAW("withdraw", "bettereconomy.withdraw", false, true);
	
	public final String command;
	public final String permission;
	public final boolean isOP;
	public final boolean isBankCommand;
	
	private EconomyCommand(String command, String permission, boolean isOP, boolean isBankCommand){
		this.command = command;
		this.permission = permission;
		this.isOP = isOP;
		this.isBankCommand = isBankCommand;
	}
}
