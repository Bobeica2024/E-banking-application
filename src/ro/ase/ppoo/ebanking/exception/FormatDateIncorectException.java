package ro.ase.ppoo.ebanking.exception;

/**
 * Exceptie aruncata la parsarea sau validarea datelor citite din fisiere sau introduse de la consola
 * (de exemplu campuri lipsa in CSV, format invalid de data/ora, valori numerice corupte, CNP incorect).
 */
public class FormatDateIncorectException extends BancarException {
    private static final long serialVersionUID = 1L;

    public FormatDateIncorectException(String mesaj) {
        super("Eroare de formatare sau validare date: " + mesaj);
    }

    public FormatDateIncorectException(String mesaj, Throwable cauza) {
        super("Eroare de formatare sau validare date: " + mesaj, cauza);
    }
}
