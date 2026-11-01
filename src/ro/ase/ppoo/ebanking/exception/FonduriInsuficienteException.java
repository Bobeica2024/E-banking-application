package ro.ase.ppoo.ebanking.exception;

/**
 * Exceptie aruncata atunci cand un cont nu are fonduri suficiente
 * pentru finalizarea unei retrageri, plati sau transfer bancar.
 */
public class FonduriInsuficienteException extends BancarException {
    private static final long serialVersionUID = 1L;

    private final double sumaCeruta;
    private final double soldDisponibil;

    public FonduriInsuficienteException(String iban, double sumaCeruta, double soldDisponibil) {
        super(String.format("Fonduri insuficiente in contul %s: suma ceruta (inclusiv comision) = %.2f, sold disponibil = %.2f",
                iban, sumaCeruta, soldDisponibil));
        this.sumaCeruta = sumaCeruta;
        this.soldDisponibil = soldDisponibil;
    }

    public double getSumaCeruta() {
        return sumaCeruta;
    }

    public double getSoldDisponibil() {
        return soldDisponibil;
    }
}
