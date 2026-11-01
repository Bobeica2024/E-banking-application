package ro.ase.ppoo.ebanking.model;

import java.io.Serializable;

/**
 * Starea de procesare a unei tranzactii.
 */
public enum StatusTranzactie implements Serializable {
    FINALIZATA("Tranzactie procesata cu succes"),
    IN_PROCESARE("In curs de procesare interbancara"),
    REFUZATA("Tranzactie refuzata (fonduri insuficiente sau cont blocat)"),
    ANULATA("Tranzactie anulata de utilizator");

    private final String explicatie;

    StatusTranzactie(String explicatie) {
        this.explicatie = explicatie;
    }

    public String getExplicatie() {
        return explicatie;
    }
}
