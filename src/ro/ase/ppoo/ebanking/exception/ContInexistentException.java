package ro.ase.ppoo.ebanking.exception;

/**
 * Exceptie aruncata atunci cand un cont specificat prin IBAN nu exista in sistem.
 */
public class ContInexistentException extends BancarException {
    private static final long serialVersionUID = 1L;

    private final String iban;

    public ContInexistentException(String iban) {
        super("Contul bancar cu IBAN-ul '" + iban + "' nu a fost gasit in evidentele bancii.");
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }
}
