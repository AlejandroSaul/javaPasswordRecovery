package com.recovery.service;

import java.util.Iterator;

public class BruteForce implements Iterator<String>{
	
	public BruteForce() {
		this.longitud = 1;
		this.indices = new int[]{0};
	}
	
	public BruteForce(String ultimoGenerado) {
		this.longitud = ultimoGenerado.length();
		this.indices = new int[longitud];
		for (int i = 0; i < longitud; i++) {
			indices[i] = ultimoGenerado.charAt(i) - 35;
		}
		avanzar();
	}

	private static final char[] letras = new char[92];

	static {
		for(char caracter  = 35;caracter<=126;caracter++) {
			letras[caracter-35] = caracter;
		}
	}

	private int longitud = 1;
	private int[] indices = {0};

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public String next() {
		StringBuilder resultado = new StringBuilder();
		for (int indice : indices) {
			resultado.append(letras[indice]);
		}
		avanzar();
		return resultado.toString();
	}

	private void avanzar() {
		int posicion = indices.length - 1;
		while (posicion >= 0) {
			indices[posicion]++;
			if (indices[posicion] < letras.length) {
				return;
			}
			indices[posicion] = 0;
			posicion--;
		}
		longitud++;
		indices = new int[longitud];
	}

}
