package ro.ase.ppoo.ebanking.exception;

/**
 * Exceptie aruncata la incercarea de a efectua operatiuni asupra unui cont inactiv sau suspendat/blocat.
 */
public class ContBlocatException extends BancarException {
    private static final long serialVersionUID = 1L;

    private final String iban;

    public ContBlocatException(String iban) {
        super("Contul bancar " + iban + " este blocat sau inactiv. Operațiunea financiară a fost refuzată.");
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }
}
