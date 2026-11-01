package ro.ase.ppoo.ebanking.ui;

import ro.ase.ppoo.ebanking.exception.BancarException;
import ro.ase.ppoo.ebanking.exception.FormatDateIncorectException;
import ro.ase.ppoo.ebanking.model.*;
import ro.ase.ppoo.ebanking.service.BancaService;
import ro.ase.ppoo.ebanking.storage.BinaryStorage;
import ro.ase.ppoo.ebanking.storage.CsvStorage;
import ro.ase.ppoo.ebanking.storage.RaportGenerator;

import java.io.IOException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Interfata interactiva de tip consola pentru sistemul de e-banking.
 * Asigura meniul utilizatorului, validarea inputului si apelul metodelor din service.
 */
public class MeniuConsola {

    private final BancaService bancaService;
    private final CsvStorage csvStorage;
    private final BinaryStorage binaryStorage;
    private final RaportGenerator raportGenerator;
    private final Scanner scanner;

    private final String caleFisierConturi;
    private final String caleFisierTranzactii;
    private final String caleFisierBackup;
    private final String caleFisierRaport;

    private boolean aplicatieRuland = true;

    public MeniuConsola(BancaService bancaService, CsvStorage csvStorage, BinaryStorage binaryStorage,
                        RaportGenerator raportGenerator, String caleConturi, String caleTranzactii,
                        String caleBackup, String caleRaport) {
        this.bancaService = bancaService;
        this.csvStorage = csvStorage;
        this.binaryStorage = binaryStorage;
        this.raportGenerator = raportGenerator;
        this.caleFisierConturi = caleConturi;
        this.caleFisierTranzactii = caleTranzactii;
        this.caleFisierBackup = caleBackup;
        this.caleFisierRaport = caleRaport;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Porneste ciclul de interactiune cu utilizatorul prin consola.
     */
    public void porneste() {
        while (aplicatieRuland) {
            afiseazaMeniuPrincipal();
            int optiune = citesteInt("Selectati optiunea dorita: ", 0, 12);
            System.out.println();

            try {
                switch (optiune) {
                    case 1 -> optiuneAfisareConturi();
                    case 2 -> optiuneDeschidereCont();
                    case 3 -> optiuneDepunere();
                    case 4 -> optiuneRetragere();
                    case 5 -> optiuneTransfer();
                    case 6 -> optiunePlataFactura();
                    case 7 -> optiuneIstoricTranzactii();
                    case 8 -> optiuneVizualizareMatriceStatistica();
                    case 9 -> optiuneGenerareRaportText();
                    case 10 -> optiuneExportBackupBinar();
                    case 11 -> optiuneRestaurareBackupBinar();
                    case 12 -> optiuneBlocareDeblocareCont();
                    case 0 -> optiuneSalvareSiIesire();
                }
            } catch (BancarException ex) {
                System.err.println("[EROARE BANCARA] " + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("[EROARE DE SISTEM] A aparut o eroare neasteptata: " + ex.getMessage());
            }

            if (aplicatieRuland) {
                System.out.println("\nApasati ENTER pentru a reveni la meniul principal...");
                scanner.nextLine();
            }
        }
    }

    private void afiseazaMeniuPrincipal() {
        System.out.println("==========================================================================");
        System.out.println("                   SISTEM E-BANKING PPOO - MENIU PRINCIPAL                ");
        System.out.println("==========================================================================");
        System.out.println("  1. Vizualizare lista conturi bancare si solduri");
        System.out.println("  2. Deschidere cont bancar nou");
        System.out.println("  3. Depunere numerar");
        System.out.println("  4. Retragere numerar");
        System.out.println("  5. Transfer bancar interconturi (cu conversie valutara)");
        System.out.println("  6. Plata factura utilitati");
        System.out.println("  7. Istoric tranzactii (global sau filtrat dupa IBAN)");
        System.out.println("  8. Vizualizare statistici lunare (Matrice 2D fundamentala)");
        System.out.println("  9. Generare raport statistic in fisier text");
        System.out.println(" 10. Salvare copie de siguranta binara (Backup .dat)");
        System.out.println(" 11. Restaurare baza de date din backup binar");
        System.out.println(" 12. Blocare / Deblocare cont bancar");
        System.out.println("  0. Salvare date in fisiere si Iesire din aplicatie");
        System.out.println("==========================================================================");
    }

    private void optiuneAfisareConturi() {
        System.out.println("--- LISTA CONTURI BANCARE INREGISTRATE ---");
        Collection<ContBancar> conturi = bancaService.getToateConturile();
        if (conturi.isEmpty()) {
            System.out.println("Nu exista niciun cont inregistrat in sistem.");
            return;
        }

        List<ContBancar> listaSortata = new ArrayList<>(conturi);
        Collections.sort(listaSortata);

        int index = 1;
        for (ContBancar c : listaSortata) {
            System.out.println(index++ + ". " + c);
        }

        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("Total fonduri banca (echivalent RON): %,12.2f lei\n", bancaService.calculeazaTotalFonduri(Moneda.RON));
        System.out.printf("Total fonduri banca (echivalent EUR): %,12.2f €\n", bancaService.calculeazaTotalFonduri(Moneda.EUR));
    }

    private void optiuneDeschidereCont() throws BancarException {
        System.out.println("--- DESCHIDERE CONT BANCAR NOU ---");
        String iban = citesteSir("Introduceti IBAN-ul (ex: RO12BTRLRONCRT...): ").toUpperCase();
        String nume = citesteSir("Nume si prenume titular: ");
        String cnp = citesteSir("CNP titular (13 cifre): ");
        String email = citesteSir("Adresa email: ");
        String telefon = citesteSir("Numar telefon: ");

        TipCont tipCont = citesteTipCont();
        Moneda moneda = citesteMoneda();
        double soldInitial = citesteDouble("Sold initial depus (minim 0): ");

        Client client = new Client("CLI-" + (cnp.length() >= 4 ? cnp.substring(cnp.length() - 4) : "NEW"), nume, cnp, email, telefon);
        bancaService.deschideCont(iban, client, tipCont, moneda, soldInitial);

        System.out.println("[SUCCES] Contul a fost deschis cu succes in memorie pentru " + nume + "!");
    }

    private void optiuneDepunere() throws BancarException {
        System.out.println("--- DEPUNERE NUMERAR ---");
        String iban = citesteSir("Introduceti IBAN-ul contului: ").toUpperCase();
        double suma = citesteDouble("Suma de depus: ");
        String descriere = citesteSir("Detalii / Descriere depunere: ");

        Tranzactie trx = bancaService.efectueazaDepunere(iban, suma, descriere);
        ContBancar cont = bancaService.gasesteCont(iban);

        System.out.println("[SUCCES] Depunere finalizata!");
        System.out.printf("Tranzactie ID: %s | Sold actual: %.2f %s\n",
                trx.getIdTranzactie(), cont.getSold(), cont.getMoneda().name());
    }

    private void optiuneRetragere() throws BancarException {
        System.out.println("--- RETRAGERE NUMERAR ---");
        String iban = citesteSir("Introduceti IBAN-ul contului: ").toUpperCase();
        double suma = citesteDouble("Suma de retras: ");
        String descriere = citesteSir("Detalii / Descriere retragere: ");

        Tranzactie trx = bancaService.efectueazaRetragere(iban, suma, descriere);
        ContBancar cont = bancaService.gasesteCont(iban);

        System.out.println("[SUCCES] Retragere finalizata!");
        System.out.printf("Tranzactie ID: %s | Comision: %.2f | Sold ramas: %.2f %s\n",
                trx.getIdTranzactie(), trx.getComision(), cont.getSold(), cont.getMoneda().name());
    }

    private void optiuneTransfer() throws BancarException {
        System.out.println("--- TRANSFER BANCAR INTERCONTURI ---");
        String ibanSursa = citesteSir("IBAN cont debitor (Sursa): ").toUpperCase();
        String ibanDestinatie = citesteSir("IBAN cont creditor (Destinatie): ").toUpperCase();
        double suma = citesteDouble("Suma de transferat: ");
        String descriere = citesteSir("Detalii transfer: ");

        Tranzactie trx = bancaService.efectueazaTransfer(ibanSursa, ibanDestinatie, suma, descriere);
        ContBancar sursa = bancaService.gasesteCont(ibanSursa);
        ContBancar dest = bancaService.gasesteCont(ibanDestinatie);

        System.out.println("[SUCCES] Transfer bancar procesat!");
        System.out.printf("Tranzactie: %s | Comision: %.2f %s\n", trx.getIdTranzactie(), trx.getComision(), sursa.getMoneda().name());
        System.out.printf("Sold cont sursa: %.2f %s | Sold cont destinatie: %.2f %s\n",
                sursa.getSold(), sursa.getMoneda().name(), dest.getSold(), dest.getMoneda().name());
    }

    private void optiunePlataFactura() throws BancarException {
        System.out.println("--- PLATA FACTURA UTILITATI ---");
        String iban = citesteSir("IBAN cont platitor: ").toUpperCase();
        String furnizor = citesteSir("Denumire furnizor (ex: Enel, Digi, Engie): ");
        String nrFactura = citesteSir("Numar factura: ");
        double suma = citesteDouble("Suma facturii: ");

        Tranzactie trx = bancaService.efectueazaPlataFactura(iban, furnizor, suma, nrFactura);
        ContBancar cont = bancaService.gasesteCont(iban);

        System.out.println("[SUCCES] Factura a fost achitata!");
        System.out.printf("ID Tranzactie: %s | Sold ramas: %.2f %s\n", trx.getIdTranzactie(), cont.getSold(), cont.getMoneda().name());
    }

    private void optiuneIstoricTranzactii() {
        System.out.println("--- ISTORIC TRANZACTII BANCAR ---");
        System.out.println("1. Afisare toate tranzactiile din sistem");
        System.out.println("2. Filtrare tranzactii dupa un IBAN");
        int opt = citesteInt("Selectati modul de afisare (1/2): ", 1, 2);

        List<Tranzactie> lista;
        if (opt == 1) {
            lista = new ArrayList<>(bancaService.getToateTranzactiile());
            Collections.sort(lista);
        } else {
            String iban = citesteSir("Introduceti IBAN-ul pentru filtrare: ").toUpperCase();
            lista = bancaService.filtreazaTranzactiiCont(iban);
        }

        if (lista.isEmpty()) {
            System.out.println("Nu exista tranzactii inregistrate pentru criteriul selectat.");
            return;
        }

        System.out.println("\nRezultate (" + lista.size() + " tranzactii):");
        for (Tranzactie t : lista) {
            System.out.println(" -> " + t);
        }
    }

    private void optiuneVizualizareMatriceStatistica() {
        System.out.println("--- VIZUALIZARE MATRICE 2D STATISTICA TRANZACTII (ECHIVALENT RON) ---");
        double[][] matrice = bancaService.getMatriceStatisticaLunara();
        int[][] matriceNr = bancaService.getMatriceNumarTranzactii();

        System.out.printf("%-12s | %-14s | %-14s | %-14s | %-14s\n",
                "Luna", "DEPUNERI", "RETRAGERI", "TRANSFERURI", "FACTURI");
        System.out.println("-----------------------------------------------------------------------------");

        for (int luna = 0; luna < 12; luna++) {
            String numeLuna = Month.of(luna + 1).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ro-RO"));
            numeLuna = numeLuna.substring(0, 1).toUpperCase() + numeLuna.substring(1);

            System.out.printf("%-12s | %10.2f (%2d) | %10.2f (%2d) | %10.2f (%2d) | %10.2f (%2d)\n",
                    numeLuna,
                    matrice[luna][0], matriceNr[luna][0],
                    matrice[luna][1], matriceNr[luna][1],
                    matrice[luna][2], matriceNr[luna][2],
                    matrice[luna][3], matriceNr[luna][3]);
        }
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("* Nota: Formatul celulei este 'Suma (Numar operatiuni)'");
    }

    private void optiuneGenerareRaportText() {
        System.out.println("--- GENERARE RAPORT STATISTIC FINANCIAR ---");
        try {
            raportGenerator.genereazaRaportStatistici(caleFisierRaport, bancaService);
            System.out.println("[SUCCES] Raportul statistic a fost generat in fisierul: " + caleFisierRaport);
        } catch (IOException e) {
            System.err.println("[EROARE] Nu s-a putut genera raportul: " + e.getMessage());
        }
    }

    private void optiuneExportBackupBinar() {
        System.out.println("--- EXPORT COPIE DE SIGURANTA BINARA (AUDIT) ---");
        try {
            binaryStorage.salveazaBackupBinar(caleFisierBackup, bancaService.getToateConturile(), bancaService.getToateTranzactiile());
            System.out.println("[SUCCES] Backup-ul binar complet a fost salvat in: " + caleFisierBackup);
        } catch (IOException e) {
            System.err.println("[EROARE] Nu s-a putut crea backup-ul binar: " + e.getMessage());
        }
    }

    private void optiuneRestaurareBackupBinar() {
        System.out.println("--- RESTAURARE DATE DIN BACKUP BINAR ---");
        System.out.print("ATENTIE: Toate datele curente din memorie vor fi inlocuite cu cele din backup. Continuati? (da/nu): ");
        String confirmare = scanner.nextLine().trim();
        if (!confirmare.equalsIgnoreCase("da")) {
            System.out.println("Operatiune anulata.");
            return;
        }

        try {
            binaryStorage.restaureazaBackupBinar(caleFisierBackup, bancaService);
            System.out.println("[SUCCES] Baza de date din memorie a fost restaurata cu succes din backup!");
        } catch (Exception e) {
            System.err.println("[EROARE] Restaurarea din backup a esuat: " + e.getMessage());
        }
    }

    private void optiuneBlocareDeblocareCont() throws BancarException {
        System.out.println("--- BLOCARE / DEBLOCARE CONT BANCAR ---");
        String iban = citesteSir("Introduceti IBAN-ul contului: ").toUpperCase();
        ContBancar cont = bancaService.gasesteCont(iban);

        System.out.println("Starea actuala a contului: " + (cont.isActiv() ? "ACTIV" : "BLOCAT"));
        System.out.print("Doriti sa " + (cont.isActiv() ? "BLOCATI" : "ACTIVATI") + " contul? (da/nu): ");
        String raspuns = scanner.nextLine().trim();

        if (raspuns.equalsIgnoreCase("da")) {
            bancaService.blocheazaDeblocheazaCont(iban, !cont.isActiv());
            System.out.println("[SUCCES] Starea contului a fost actualizata: " + (!cont.isActiv() ? "BLOCAT" : "ACTIV"));
        } else {
            System.out.println("Nicio modificare efectuata.");
        }
    }

    public void optiuneSalvareSiIesire() {
        System.out.println("--- SALVARE DATE SI INCHIDERE APLICATIE ---");
        try {
            System.out.println("Salvare conturi actualizate in " + caleFisierConturi + "...");
            csvStorage.salveazaConturi(caleFisierConturi, bancaService.getToateConturile());

            System.out.println("Salvare tranzactii actualizate in " + caleFisierTranzactii + "...");
            csvStorage.salveazaTranzactii(caleFisierTranzactii, bancaService.getToateTranzactiile());

            System.out.println("[SUCCES] Toate modificarile din memorie au fost persistate cu succes pe disc!");
        } catch (IOException e) {
            System.err.println("[EROARE CRITICA LA SALVARE] " + e.getMessage());
        }

        aplicatieRuland = false;
        System.out.println("\nVa multumim ca ati utilizat Sistemul de E-Banking PPOO. La revedere!");
    }

    // =========================================================================
    // METODE AJUTATOARE PENTRU CITIRE SI VALIDARE DIN CONSOLA (Cerinta 5)
    // =========================================================================

    private String citesteSir(String prompt) {
        System.out.print(prompt);
        String val = scanner.nextLine().trim();
        while (val.isEmpty()) {
            System.out.print("Campul nu poate fi gol. Reintroduceti: ");
            val = scanner.nextLine().trim();
        }
        return val;
    }

    private int citesteInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String linie = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(linie);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Valoarea trebuie sa fie un numar intre %d si %d!\n", min, max);
            } catch (NumberFormatException ex) {
                System.out.println("Format invalid! Introduceti un numar intreg.");
            }
        }
    }

    private double citesteDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linie = scanner.nextLine().trim().replace(',', '.');
            try {
                double val = Double.parseDouble(linie);
                if (val >= 0) {
                    return val;
                }
                System.out.println("Suma nu poate fi negativa!");
            } catch (NumberFormatException ex) {
                System.out.println("Format numeric invalid! Exemplu: 1500.50");
            }
        }
    }

    private Moneda citesteMoneda() {
        System.out.println("Monede disponibile: 1. RON | 2. EUR | 3. USD | 4. GBP");
        int opt = citesteInt("Selectati moneda (1-4): ", 1, 4);
        return switch (opt) {
            case 2 -> Moneda.EUR;
            case 3 -> Moneda.USD;
            case 4 -> Moneda.GBP;
            default -> Moneda.RON;
        };
    }

    private TipCont citesteTipCont() {
        System.out.println("Tipuri de cont disponibile: 1. CURENT | 2. ECONOMII | 3. DEPOZIT");
        int opt = citesteInt("Selectati tipul contului (1-3): ", 1, 3);
        return switch (opt) {
            case 2 -> TipCont.ECONOMII;
            case 3 -> TipCont.DEPOZIT;
            default -> TipCont.CURENT;
        };
    }
}
