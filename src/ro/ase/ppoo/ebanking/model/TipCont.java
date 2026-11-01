package ro.ase.ppoo.ebanking.model;

import java.io.Serializable;

/**
 * Tipul contului bancar gestionat in aplicatie.
 */
public enum TipCont implements Serializable {
    CURENT("Cont Curent de Operatiuni Zilnice", 0.0),
    ECONOMII("Cont de Economii", 0.05),
    DEPOZIT("Depozit Bancar la Termen", 0.07);

    private final String descriere;
    private final double rataDobandaAnuala;

    TipCont(String descriere, double rataDobandaAnuala) {
        this.descriere = descriere;
        this.rataDobandaAnuala = rataDobandaAnuala;
    }

    public String getDescriere() {
        return descriere;
    }

    public double getRataDobandaAnuala() {
        return rataDobandaAnuala;
    }
}
