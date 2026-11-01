package ro.ase.ppoo.ebanking.exception;

/**
 * Clasa de baza pentru toate exceptiile specifice aplicatiei e-banking.
 * Mosteneste Exception (checked exception), fortand tratarea sau propagarea explicita.
 */
public class BancarException extends Exception {
    private static final long serialVersionUID = 1L;

    public BancarException(String mesaj) {
        super(mesaj);
    }

    public BancarException(String mesaj, Throwable cauza) {
        super(mesaj, cauza);
    }
}
