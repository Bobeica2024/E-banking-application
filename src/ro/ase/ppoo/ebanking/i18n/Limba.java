package ro.ase.ppoo.ebanking.i18n;

/**
 * Enumerare a limbilor suportate in cadrul aplicatiei bancare.
 */
public enum Limba {
    ROMANA("Română", "ro", "RO"),
    ENGLEZA("English", "en", "US");

    private final String denumire;
    private final String codLimba;
    private final String codTara;

    Limba(String denumire, String codLimba, String codTara) {
        this.denumire = denumire;
        this.codLimba = codLimba;
        this.codTara = codTara;
    }

    public String getDenumire() {
        return denumire;
    }

    public String getCodLimba() {
        return codLimba;
    }

    public String getCodTara() {
        return codTara;
    }

    @Override
    public String toString() {
        return denumire;
    }
}
