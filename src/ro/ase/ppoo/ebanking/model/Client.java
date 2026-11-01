package ro.ase.ppoo.ebanking.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entitatea Client reprezinta un titular de cont bancar in cadrul sistemului e-banking.
 */
public class Client implements Serializable, Comparable<Client> {
    private static final long serialVersionUID = 1L;

    private String idClient;
    private String nume;
    private String cnp;
    private String email;
    private String telefon;

    public Client() {
    }

    public Client(String idClient, String nume, String cnp, String email, String telefon) {
        this.idClient = idClient;
        this.nume = nume;
        this.cnp = cnp;
        this.email = email;
        this.telefon = telefon;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(cnp, client.cnp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cnp);
    }

    @Override
    public int compareTo(Client altClient) {
        if (altClient == null) return 1;
        return this.nume.compareToIgnoreCase(altClient.nume);
    }

    @Override
    public String toString() {
        return String.format("%s (CNP: %s, Email: %s, Tel: %s)", nume, cnp, email, telefon);
    }
}
