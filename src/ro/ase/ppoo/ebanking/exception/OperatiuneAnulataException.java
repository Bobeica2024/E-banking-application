package ro.ase.ppoo.ebanking.exception;

/**
 * Exceptie aruncata la nivelul interfetei cu utilizatorul atunci cand o operatiune
 * interactiva este intrerupta sau abandonata voluntar de catre operator (ex: prin tastarea tastei '0').
 */
public class OperatiuneAnulataException extends BancarException {
    private static final long serialVersionUID = 1L;

    public OperatiuneAnulataException() {
        super("Operatiunea a fost anulata de utilizator.");
    }

    public OperatiuneAnulataException(String mesaj) {
        super(mesaj);
    }
}
