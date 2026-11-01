package ro.ase.ppoo.ebanking.model;

import java.io.Serializable;

/**
 * Tipul tranzactiei bancare realizate de utilizator.
 */
public enum TipTranzactie implements Serializable {
    DEPUNERE(0, "Depunere Numerar"),
    RETRAGERE(1, "Retragere Numerar"),
    TRANSFER(2, "Transfer Bancar"),
    PLATA_FACTURA(3, "Plata Utilitati/Factura");

    private final int indexMatrice;
    private final String eticheta;

    TipTranzactie(int indexMatrice, String eticheta) {
        this.indexMatrice = indexMatrice;
        this.eticheta = eticheta;
    }

    /**
     * Indexul utilizat pentru maparea in matricea bidimensionala fundamentala a activitatii bancare.
     * @return indexul coloanei din matrice (0..3)
     */
    public int getIndexMatrice() {
        return indexMatrice;
    }

    public String getEticheta() {
        return eticheta;
    }
}
