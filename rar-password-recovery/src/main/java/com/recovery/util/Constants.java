package com.recovery.util;

public enum Constants {
	EXAMINAR ("Examinar...");
	
	private Constants(String constante) {
		this.constante = constante;
	}
	
	private String constante;
	
	public String getConstante() {
		return constante;
	}
}
