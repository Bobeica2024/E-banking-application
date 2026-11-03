package ro.ase.ppoo.ebanking.exception;

/**
 * Exceptie aruncata atunci cand o tranzactie incalca regulile de business ale bancii
 * (de exemplu: sume negative, cont sursa identic cu cont destinatie, depasire limita legala).
 */
public class TranzactieInvalidaException extends BancarException {
    private static final long serialVersionUID = 1L;

    public TranzactieInvalidaException(String motiv) {
        super("Tranzacție invalidă: " + motiv);
    }
}
