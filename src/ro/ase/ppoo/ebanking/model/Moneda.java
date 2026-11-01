package ro.ase.ppoo.ebanking.model;

import java.io.Serializable;

/**
 * Enumerare ce defineste monedele acceptate in cadrul sistemului e-banking.
 * Fiecare moneda are asociat un cod ISO si un simbol specific.
 */
public enum Moneda implements Serializable {
    RON("Leu Romanesc", "lei"),
    EUR("Euro", "€"),
    USD("Dolar American", "$"),
    GBP("Lira Sterlina", "£");

    private final String denumire;
    private final String simbol;

    Moneda(String denumire, String simbol) {
        this.denumire = denumire;
        this.simbol = simbol;
    }

    public String getDenumire() {
        return denumire;
    }

    public String getSimbol() {
        return simbol;
    }
}
