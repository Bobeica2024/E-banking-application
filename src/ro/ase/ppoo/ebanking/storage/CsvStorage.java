package ro.ase.ppoo.ebanking.storage;

import ro.ase.ppoo.ebanking.exception.FormatDateIncorectException;
import ro.ase.ppoo.ebanking.model.*;
import ro.ase.ppoo.ebanking.service.BancaService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Clasa responsabila cu persistenta datelor in format text (CSV).
 * Permite incarcarea initiala si actualizarea fisierelor la inchiderea aplicatiei.
 */
public class CsvStorage {

    private static final String CSV_DELIMITER = ",";
    private static final String HEADER_CONTURI = "IBAN,NumeTitular,CNP,Email,Telefon,TipCont,Moneda,Sold,Activ,PIN,NecesitaResetarePin";
    private static final String HEADER_TRANZACTII = "IdTranzactie,DataOra,IbanSursa,IbanDestinatie,TipTranzactie,Suma,Moneda,Comision,Status,Descriere";

    /**
     * Incarca datele despre conturi din fisierul CSV de intrare in memorie.
     */
    public void incarcaConturi(String caleFisier, BancaService service) throws IOException, FormatDateIncorectException {
        File fisier = new File(caleFisier);
        if (!fisier.exists()) {
            throw new FileNotFoundException("Fișierul de conturi nu există: " + caleFisier);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fisier), StandardCharsets.UTF_8))) {
            String linie = reader.readLine(); // Sarim peste header
            int indexLinie = 1;

            while ((linie = reader.readLine()) != null) {
                indexLinie++;
                linie = linie.trim();
                if (linie.isEmpty()) continue;

                String[] tokens = linie.split(CSV_DELIMITER, -1);
                if (tokens.length < 9) {
                    throw new FormatDateIncorectException("Linia " + indexLinie + " din conturi.csv conține mai puțin de 9 coloane.");
                }

                try {
                    String iban = tokens[0].trim();
                    String nume = tokens[1].trim();
                    String cnp = tokens[2].trim();
                    String email = tokens[3].trim();
                    String telefon = tokens[4].trim();
                    TipCont tipCont = TipCont.valueOf(tokens[5].trim().toUpperCase());
                    Moneda moneda = Moneda.valueOf(tokens[6].trim().toUpperCase());
                    double sold = Double.parseDouble(tokens[7].trim());
                    boolean activ = Boolean.parseBoolean(tokens[8].trim());
                    String pin = (tokens.length >= 10 && !tokens[9].trim().isEmpty()) ? tokens[9].trim() : "1234";
                    boolean necesitaResetare = (tokens.length >= 11 && !tokens[10].trim().isEmpty()) ? Boolean.parseBoolean(tokens[10].trim()) : false;

                    Client client = new Client("CLI-" + cnp.substring(Math.max(0, cnp.length() - 4)), nume, cnp, email, telefon);
                    ContBancar cont = new ContBancar(iban, client, tipCont, moneda, sold, activ, null, pin, necesitaResetare);

                    service.adaugaContInMemorie(cont);
                } catch (IllegalArgumentException ex) {
                    throw new FormatDateIncorectException("Eroare de parsare date numerice/enum la linia " + indexLinie + ": " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Incarca istoricul tranzactiilor din fisierul CSV in memorie.
     */
    public void incarcaTranzactii(String caleFisier, BancaService service) throws IOException, FormatDateIncorectException {
        File fisier = new File(caleFisier);
        if (!fisier.exists()) {
            return; // Daca fisierul nu exista inca, pornim cu istoric gol
        }

        long maxTrxId = 1000;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fisier), StandardCharsets.UTF_8))) {
            String linie = reader.readLine(); // Sarim peste header
            int indexLinie = 1;

            while ((linie = reader.readLine()) != null) {
                indexLinie++;
                linie = linie.trim();
                if (linie.isEmpty()) continue;

                String[] tokens = linie.split(CSV_DELIMITER, -1);
                if (tokens.length < 10) {
                    throw new FormatDateIncorectException("Linia " + indexLinie + " din tranzactii.csv conține mai puțin de 10 coloane.");
                }

                try {
                    String idTrx = tokens[0].trim();
                    LocalDateTime dataOra = LocalDateTime.parse(tokens[1].trim(), Tranzactie.FORMATTER);
                    String ibanSursa = tokens[2].trim();
                    String ibanDest = tokens[3].trim();
                    TipTranzactie tip = TipTranzactie.valueOf(tokens[4].trim().toUpperCase());
                    double suma = Double.parseDouble(tokens[5].trim());
                    Moneda moneda = Moneda.valueOf(tokens[6].trim().toUpperCase());
                    double comision = Double.parseDouble(tokens[7].trim());
                    StatusTranzactie status = StatusTranzactie.valueOf(tokens[8].trim().toUpperCase());
                    String descriere = tokens[9].trim();

                    Tranzactie t = new Tranzactie(idTrx, dataOra, ibanSursa, ibanDest, tip, suma, moneda, comision, status, descriere);
                    service.incarcaTranzactieDinFisier(t);

                    // Parsam id-ul numeric pentru contor
                    if (idTrx.startsWith("TRX-")) {
                        try {
                            long idNum = Long.parseLong(idTrx.substring(4));
                            if (idNum > maxTrxId) maxTrxId = idNum;
                        } catch (NumberFormatException ignored) {}
                    }
                } catch (Exception ex) {
                    throw new FormatDateIncorectException("Eroare la parsarea tranzacției la linia " + indexLinie + ": " + ex.getMessage(), ex);
                }
            }
        }
        service.seteazaContorTranzactii(maxTrxId);
    }

    /**
     * Salveaza colectia curenta de conturi din memorie in fisierul CSV de intrare.
     */
    public void salveazaConturi(String caleFisier, Collection<ContBancar> conturi) throws IOException {
        File fisier = new File(caleFisier);
        if (fisier.getParentFile() != null) {
            fisier.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fisier), StandardCharsets.UTF_8))) {
            writer.write(HEADER_CONTURI);
            writer.newLine();

            for (ContBancar c : conturi) {
                Client t = c.getTitular();
                String linie = String.format("%s,%s,%s,%s,%s,%s,%s,%.2f,%b,%s,%b",
                        c.getIban(),
                        t != null ? t.getNume() : "",
                        t != null ? t.getCnp() : "",
                        t != null ? t.getEmail() : "",
                        t != null ? t.getTelefon() : "",
                        c.getTipCont().name(),
                        c.getMoneda().name(),
                        c.getSold(),
                        c.isActiv(),
                        c.getPin() != null ? c.getPin() : "1234",
                        c.isNecesitaResetarePin());
                writer.write(linie);
                writer.newLine();
            }
        }
    }

    /**
     * Salveaza lista tranzactiilor din memorie in fisierul text CSV.
     */
    public void salveazaTranzactii(String caleFisier, List<Tranzactie> tranzactii) throws IOException {
        File fisier = new File(caleFisier);
        if (fisier.getParentFile() != null) {
            fisier.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fisier), StandardCharsets.UTF_8))) {
            writer.write(HEADER_TRANZACTII);
            writer.newLine();

            for (Tranzactie t : tranzactii) {
                String linie = String.format("%s,%s,%s,%s,%s,%.2f,%s,%.2f,%s,%s",
                        t.getIdTranzactie(),
                        t.getDataOra().format(Tranzactie.FORMATTER),
                        t.getIbanSursa(),
                        t.getIbanDestinatie(),
                        t.getTip().name(),
                        t.getSuma(),
                        t.getMoneda().name(),
                        t.getComision(),
                        t.getStatus().name(),
                        t.getDescriere() != null ? t.getDescriere().replace(",", ";") : "");
                writer.write(linie);
                writer.newLine();
            }
        }
    }
}
