package com.ama.crossword;

import android.widget.EditText;

public class Palabra {
    private String palabra;
    private String pista;
    //true = Horizontal
    //false = Vertical
    private boolean direccion;
    private int[] inicio;
    private int[] fin;

    public Palabra(String palabra, String pista){
        this.palabra = palabra;
        this.pista = pista;
    }

    public String getPalabra() {
        return palabra;
    }

    public void setPalabra(String palabra) {
        this.palabra = palabra;
    }

    public String getPista() {
        return pista;
    }

    public void setPista(String pista) {
        this.pista = pista;
    }

    public boolean isDireccion() {
        return direccion;
    }

    public void setDireccion(boolean direccion) {
        this.direccion = direccion;
    }

    public int[] getInicio() {
        return inicio;
    }

    public void setInicio(int[] inicio) {
        this.inicio = inicio;
    }

    public int[] getFin() {
        return fin;
    }

    public void setFin(int[] fin) {
        this.fin = fin;
    }
}
