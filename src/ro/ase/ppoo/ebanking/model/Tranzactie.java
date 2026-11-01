package ro.ase.ppoo.ebanking.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Entitatea Tranzactie inregistreaza o operatiune financiara efectuata
 * intre doua conturi sau la ghiseu/ATM.
 */
public class Tranzactie implements Serializable, Comparable<Tranzactie> {
    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String idTranzactie;
    private LocalDateTime dataOra;
    private String ibanSursa;
    private String ibanDestinatie;
    private TipTranzactie tip;
    private double suma;
    private Moneda moneda;
    private double comision;
    private StatusTranzactie status;
    private String descriere;

    public Tranzactie() {
        this.dataOra = LocalDateTime.now();
        this.status = StatusTranzactie.FINALIZATA;
    }

    public Tranzactie(String idTranzactie, LocalDateTime dataOra, String ibanSursa, String ibanDestinatie,
                      TipTranzactie tip, double suma, Moneda moneda, double comision,
                      StatusTranzactie status, String descriere) {
        this.idTranzactie = idTranzactie;
        this.dataOra = dataOra != null ? dataOra : LocalDateTime.now();
        this.ibanSursa = ibanSursa;
        this.ibanDestinatie = ibanDestinatie;
        this.tip = tip;
        this.suma = suma;
        this.moneda = moneda;
        this.comision = comision;
        this.status = status != null ? status : StatusTranzactie.FINALIZATA;
        this.descriere = descriere;
    }

    public String getIdTranzactie() {
        return idTranzactie;
    }

    public void setIdTranzactie(String idTranzactie) {
        this.idTranzactie = idTranzactie;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    public String getIbanSursa() {
        return ibanSursa;
    }

    public void setIbanSursa(String ibanSursa) {
        this.ibanSursa = ibanSursa;
    }

    public String getIbanDestinatie() {
        return ibanDestinatie;
    }

    public void setIbanDestinatie(String ibanDestinatie) {
        this.ibanDestinatie = ibanDestinatie;
    }

    public TipTranzactie getTip() {
        return tip;
    }

    public void setTip(TipTranzactie tip) {
        this.tip = tip;
    }

    public double getSuma() {
        return suma;
    }

    public void setSuma(double suma) {
        this.suma = suma;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }

    public StatusTranzactie getStatus() {
        return status;
    }

    public void setStatus(StatusTranzactie status) {
        this.status = status;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tranzactie that = (Tranzactie) o;
        return Objects.equals(idTranzactie, that.idTranzactie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTranzactie);
    }

    @Override
    public int compareTo(Tranzactie altaTranzactie) {
        if (altaTranzactie == null) return 1;
        // Sortare cronologica descrescatoare (cele mai recente primele)
        return altaTranzactie.dataOra.compareTo(this.dataOra);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-14s | %-13s | %10.2f %-3s (comision: %.2f) | Sursa: %s -> Dest: %s | %s",
                dataOra.format(FORMATTER),
                idTranzactie,
                tip.name(),
                suma,
                moneda.name(),
                comision,
                ibanSursa,
                ibanDestinatie,
                descriere);
    }
}
