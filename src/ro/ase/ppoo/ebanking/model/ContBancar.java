package ro.ase.ppoo.ebanking.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entitatea ContBancar gestioneaza datele de identificare, soldul si starea
 * unui cont bancar deschis pe numele unui titular.
 */
public class ContBancar implements Serializable, Comparable<ContBancar> {
    private static final long serialVersionUID = 1L;

    private String iban;
    private Client titular;
    private TipCont tipCont;
    private Moneda moneda;
    private double sold;
    private boolean activ;
    private LocalDate dataDeschidere;
    private String pin;
    private int incercariPinEsuat;
    private boolean necesitaResetarePin;

    public ContBancar() {
        this.activ = true;
        this.dataDeschidere = LocalDate.now();
        this.pin = "1234";
        this.incercariPinEsuat = 0;
        this.necesitaResetarePin = false;
    }

    public ContBancar(String iban, Client titular, TipCont tipCont, Moneda moneda, double soldInitial) {
        this(iban, titular, tipCont, moneda, soldInitial, true, LocalDate.now(), "1234", false);
    }

    public ContBancar(String iban, Client titular, TipCont tipCont, Moneda moneda, double sold, boolean activ, LocalDate dataDeschidere) {
        this(iban, titular, tipCont, moneda, sold, activ, dataDeschidere, "1234", false);
    }

    public ContBancar(String iban, Client titular, TipCont tipCont, Moneda moneda, double sold, boolean activ, LocalDate dataDeschidere, String pin) {
        this(iban, titular, tipCont, moneda, sold, activ, dataDeschidere, pin, false);
    }

    public ContBancar(String iban, Client titular, TipCont tipCont, Moneda moneda, double sold, boolean activ, LocalDate dataDeschidere, String pin, boolean necesitaResetarePin) {
        this.iban = iban;
        this.titular = titular;
        this.tipCont = tipCont;
        this.moneda = moneda;
        this.sold = sold;
        this.activ = activ;
        this.dataDeschidere = dataDeschidere != null ? dataDeschidere : LocalDate.now();
        this.pin = (pin != null && !pin.isBlank()) ? pin : "1234";
        this.incercariPinEsuat = 0;
        this.necesitaResetarePin = necesitaResetarePin;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Client getTitular() {
        return titular;
    }

    public void setTitular(Client titular) {
        this.titular = titular;
    }

    public TipCont getTipCont() {
        return tipCont;
    }

    public void setTipCont(TipCont tipCont) {
        this.tipCont = tipCont;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public double getSold() {
        return sold;
    }

    public void setSold(double sold) {
        this.sold = sold;
    }

    public boolean isActiv() {
        return activ;
    }

    public void setActiv(boolean activ) {
        this.activ = activ;
    }

    public LocalDate getDataDeschidere() {
        return dataDeschidere;
    }

    public void setDataDeschidere(LocalDate dataDeschidere) {
        this.dataDeschidere = dataDeschidere;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        if (pin != null && pin.matches("^\\d{4}$")) {
            this.pin = pin;
        } else {
            throw new IllegalArgumentException("Codul PIN trebuie sa fie alcatuit din exact 4 cifre numerice.");
        }
    }

    public int getIncercariPinEsuat() {
        return incercariPinEsuat;
    }

    public void incrementeazaIncercariPinEsuat() {
        this.incercariPinEsuat++;
    }

    public void resetIncercariPin() {
        this.incercariPinEsuat = 0;
    }

    public int getIncercariRamase() {
        return Math.max(0, 3 - this.incercariPinEsuat);
    }

    public boolean isNecesitaResetarePin() {
        return necesitaResetarePin;
    }

    public void setNecesitaResetarePin(boolean necesitaResetarePin) {
        this.necesitaResetarePin = necesitaResetarePin;
    }

    /**
     * Depune o suma pozitiva in contul bancar curent.
     * @param suma suma de depus
     */
    public void depune(double suma) {
        if (suma <= 0) {
            throw new IllegalArgumentException("Suma de depus trebuie sa fie strict pozitiva.");
        }
        this.sold += suma;
    }

    /**
     * Retrage o suma specificata din cont.
     * @param suma suma de retras
     */
    public void retrage(double suma) {
        if (suma <= 0) {
            throw new IllegalArgumentException("Suma de retras trebuie sa fie strict pozitiva.");
        }
        this.sold -= suma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContBancar that = (ContBancar) o;
        return Objects.equals(iban, that.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban);
    }

    @Override
    public int compareTo(ContBancar altCont) {
        if (altCont == null) return 1;
        // Sortare implicita descrescatoare dupa sold
        return Double.compare(altCont.sold, this.sold);
    }

    @Override
    public String toString() {
        return String.format("IBAN: %-26s | Titular: %-18s | Moneda: %-3s | Sold: %10.2f %s | Tip: %-9s | Activ: %s",
                iban,
                titular != null ? titular.getNume() : "Anonim",
                moneda.name(),
                sold,
                moneda.getSimbol(),
                tipCont.name(),
                activ ? "DA" : "NU");
    }
}
