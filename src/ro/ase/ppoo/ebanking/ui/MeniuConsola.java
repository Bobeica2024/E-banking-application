package ro.ase.ppoo.ebanking.ui;

import ro.ase.ppoo.ebanking.exception.BancarException;
import ro.ase.ppoo.ebanking.exception.ContInexistentException;
import ro.ase.ppoo.ebanking.exception.FormatDateIncorectException;
import ro.ase.ppoo.ebanking.exception.OperatiuneAnulataException;
import ro.ase.ppoo.ebanking.i18n.I18n;
import ro.ase.ppoo.ebanking.i18n.Limba;
import ro.ase.ppoo.ebanking.model.*;
import ro.ase.ppoo.ebanking.service.BancaService;
import ro.ase.ppoo.ebanking.storage.BinaryStorage;
import ro.ase.ppoo.ebanking.storage.CsvStorage;
import ro.ase.ppoo.ebanking.storage.RaportGenerator;

import java.io.Console;
import java.io.IOException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Interfața interactivă de tip consolă pentru sistemul de e-banking.
 * Oferă suport bilingv (Română cu diacritice complete și Engleză),
 * separare riguroasă pe roluri (Mod Client vs. Mod Operator Bancar)
 * și protecție prin PIN / parolă securizată.
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
     * Pornește ciclul de interacțiune cu utilizatorul prin consolă.
     */
    public void porneste() {
        while (aplicatieRuland) {
            afiseazaPoartaAcces();
            int optiune = citesteInt(I18n.t("PROMPT_SELECT_OPT"), 0, 3);
            System.out.println();

            try {
                switch (optiune) {
                    case 1 -> sesiuneClient();
                    case 2 -> sesiuneOperator();
                    case 3 -> optiuneSchimbareLimba();
                    case 0 -> optiuneSalvareSiIesire();
                }
            } catch (OperatiuneAnulataException ex) {
                System.out.println("\n" + I18n.t("STATUS_CANCELLED_HEADER") + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("\n" + I18n.t("ERR_SYSTEM_HEADER") + ex.getMessage());
            }

            if (aplicatieRuland) {
                System.out.println("\n" + I18n.t("PRESS_ENTER"));
                scanner.nextLine();
            }
        }
    }

    private void afiseazaPoartaAcces() {
        System.out.println("==========================================================================");
        System.out.printf("                   %s                \n", I18n.t("GATE_TITLE"));
        System.out.println("==========================================================================");
        System.out.println("  " + I18n.t("GATE_OPT_1"));
        System.out.println("  " + I18n.t("GATE_OPT_2"));
        System.out.println("  " + I18n.t("GATE_OPT_3"));
        System.out.println("  " + I18n.t("GATE_OPT_0"));
        System.out.println("==========================================================================");
    }

    // =========================================================================
    // MODUL 1: SESIUNE CLIENT (E-BANKING / ATM CLIENT-FACING)
    // =========================================================================

    private void sesiuneClient() throws OperatiuneAnulataException {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("         AUTENTIFICARE CLIENT (IBAN & COD PIN)");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(I18n.t("CANCEL_NOTE"));

        String iban = citesteLinieSauAnulare(I18n.t("PROMPT_IBAN_ACCOUNT")).toUpperCase().replaceAll("\\s+", "");

        ContBancar contExistent;
        try {
            bancaService.valideazaIban(iban);
            contExistent = bancaService.gasesteCont(iban);
        } catch (BancarException ex) {
            System.err.println("\n" + I18n.t("ERR_BANKING_HEADER") + ex.getMessage());
            return;
        }

        if (!contExistent.isActiv()) {
            System.err.println("\n" + I18n.t("ERR_BANKING_HEADER") + "Contul " + iban + " este BLOCAT. Vă rugăm contactați un operator bancar.");
            return;
        }

        ContBancar contLogat;
        if (contExistent.isNecesitaResetarePin()) {
            System.out.println("\n==========================================================================");
            System.out.println("  [RESETARE OBLIGATORIE COD PIN]");
            System.out.println("  Operatorul bancar a aprobat procedura de resetare a codului PIN.");
            System.out.println("  Vă rugăm să vă definiți noul cod PIN introducându-l de două ori.");
            System.out.println("==========================================================================");

            String nouPin = citestePinNouCuConfirmare(I18n.t("PROMPT_NEW_PIN"), I18n.t("PROMPT_CONFIRM_PIN"));
            try {
                bancaService.finalizeazaResetarePin(iban, nouPin);
            } catch (BancarException ex) {
                System.err.println("[EROARE] " + ex.getMessage());
                return;
            }
            System.out.println("\n[SUCCES] Noul cod PIN a fost setat cu succes!");
            System.out.println("[INFO] La următoarele logări îl veți introduce o singură dată, ca în mod normal.");
            contLogat = contExistent;
        } else {
            String pin = citestePin(I18n.t("PROMPT_PIN"));
            try {
                contLogat = bancaService.autentificaClient(iban, pin);
            } catch (BancarException ex) {
                System.err.println("\n" + I18n.t("ERR_BANKING_HEADER") + ex.getMessage());
                return;
            }
        }

        System.out.printf(I18n.t("SUCCESS_LOGIN_CLIENT"), contLogat.getTitular().getNume(), contLogat.getIban());

        boolean clientConectat = true;
        while (clientConectat) {
            afiseazaMeniuClient(contLogat);
            int opt = citesteInt(I18n.t("PROMPT_SELECT_OPT"), 0, 8);
            System.out.println();

            try {
                switch (opt) {
                    case 1 -> optiuneClientSold(contLogat);
                    case 2 -> optiuneClientDepunere(contLogat);
                    case 3 -> optiuneClientRetragere(contLogat);
                    case 4 -> optiuneClientTransfer(contLogat);
                    case 5 -> optiuneClientPlataFactura(contLogat);
                    case 6 -> optiuneClientIstoric(contLogat);
                    case 7 -> optiuneClientDeschidereSubcont(contLogat);
                    case 8 -> optiuneClientSchimbarePin(contLogat);
                    case 0 -> {
                        clientConectat = false;
                        System.out.println(I18n.t("LOGOUT_CLIENT_MSG"));
                    }
                }
            } catch (OperatiuneAnulataException ex) {
                System.out.println("\n" + I18n.t("STATUS_CANCELLED_HEADER") + ex.getMessage());
            } catch (BancarException ex) {
                System.err.println("\n" + I18n.t("ERR_BANKING_HEADER") + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("\n" + I18n.t("ERR_SYSTEM_HEADER") + ex.getMessage());
            }

            if (clientConectat) {
                System.out.println("\n[ENTER pt a continua în Portalul Client...]");
                scanner.nextLine();
            }
        }
    }

    private void afiseazaMeniuClient(ContBancar cont) {
        System.out.println("==========================================================================");
        System.out.printf("  %s\n", String.format(I18n.t("CLIENT_MENU_TITLE"), cont.getTitular().getNume(), cont.getMoneda().name()));
        System.out.printf("  IBAN: %s | Sold disponibil: %,.2f %s\n", cont.getIban(), cont.getSold(), cont.getMoneda().name());
        System.out.println("==========================================================================");
        System.out.println("  " + I18n.t("CLIENT_OPT_1"));
        System.out.println("  " + I18n.t("CLIENT_OPT_2"));
        System.out.println("  " + I18n.t("CLIENT_OPT_3"));
        System.out.println("  " + I18n.t("CLIENT_OPT_4"));
        System.out.println("  " + I18n.t("CLIENT_OPT_5"));
        System.out.println("  " + I18n.t("CLIENT_OPT_6"));
        System.out.println("  " + I18n.t("CLIENT_OPT_7"));
        System.out.println("  " + I18n.t("CLIENT_OPT_8"));
        System.out.println("  " + I18n.t("CLIENT_OPT_0"));
        System.out.println("==========================================================================");
    }

    private void optiuneClientSold(ContBancar cont) {
        System.out.println("--- DETALII CONT CURENT ---");
        System.out.println(cont);
        System.out.printf("Titular: %s\n", cont.getTitular().toString());
        System.out.printf("Data deschidere cont: %s\n", cont.getDataDeschidere());
        System.out.printf("Stare cont: %s\n", cont.isActiv() ? I18n.t("STATE_ACTIVE") : I18n.t("STATE_LOCKED"));
    }

    private void optiuneClientDepunere(ContBancar cont) throws BancarException {
        System.out.println(I18n.t("DEPOSIT_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        double suma = citesteSumaDepunere(I18n.t("PROMPT_AMOUNT_DEPOSIT"));
        String descriere = citesteDescriereOptionala(I18n.t("PROMPT_DESC_DEPOSIT"), I18n.t("DEFAULT_DESC_DEPOSIT"));

        Tranzactie trx = bancaService.efectueazaDepunere(cont.getIban(), suma, descriere);
        System.out.print(I18n.t("SUCCESS_DEPOSIT", trx.getIdTranzactie(), suma, cont.getMoneda().name(), cont.getSold(), cont.getMoneda().name()));
    }

    private void optiuneClientRetragere(ContBancar cont) throws BancarException {
        System.out.println(I18n.t("WITHDRAW_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        double suma = citesteSumaRetragere(cont, I18n.t("PROMPT_AMOUNT_WITHDRAW"));
        String descriere = citesteDescriereOptionala(I18n.t("PROMPT_DESC_WITHDRAW"), I18n.t("DEFAULT_DESC_WITHDRAW"));

        Tranzactie trx = bancaService.efectueazaRetragere(cont.getIban(), suma, descriere);
        System.out.print(I18n.t("SUCCESS_WITHDRAW", trx.getIdTranzactie(), suma, cont.getMoneda().name(), trx.getComision(), cont.getMoneda().name(), cont.getSold(), cont.getMoneda().name()));
    }

    private void optiuneClientTransfer(ContBancar contSursa) throws BancarException {
        System.out.println(I18n.t("TRANSFER_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        String ibanDest = citesteIbanDestinatie(contSursa.getIban());
        double suma = citesteSumaTransfer(contSursa, String.format(I18n.t("PROMPT_AMOUNT_TRANSFER"), contSursa.getMoneda().name()));
        String descriere = citesteDescriereOptionala(I18n.t("PROMPT_DESC_TRANSFER"), I18n.t("DEFAULT_DESC_TRANSFER"));

        Tranzactie trx = bancaService.efectueazaTransfer(contSursa.getIban(), ibanDest, suma, descriere);

        ContBancar contDest = null;
        try {
            contDest = bancaService.gasesteCont(ibanDest);
        } catch (ContInexistentException ignored) {
        }

        if (contDest != null) {
            System.out.print(I18n.t("SUCCESS_TRANSFER", trx.getIdTranzactie(), trx.getComision(), contSursa.getMoneda().name(),
                    contSursa.getIban(), contSursa.getSold(), contSursa.getMoneda().name(),
                    contDest.getIban(), contDest.getSold(), contDest.getMoneda().name()));
        } else {
            System.out.printf("\n[SUCCES] Transfer internațional procesat cu succes către contul extern %s!\n" +
                            "Tranzacție ID: %s | Sumă debitată: %,.2f %s | Comision: %,.2f %s | Sold rămas: %,.2f %s\n",
                    ibanDest, trx.getIdTranzactie(), suma, contSursa.getMoneda().name(),
                    trx.getComision(), contSursa.getMoneda().name(), contSursa.getSold(), contSursa.getMoneda().name());
        }
    }

    private void optiuneClientPlataFactura(ContBancar cont) throws BancarException {
        System.out.println(I18n.t("BILL_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        String furnizor = citesteSirObligatoriu(I18n.t("PROMPT_PROVIDER"));
        String nrFactura = citesteSirObligatoriu(I18n.t("PROMPT_BILL_NUM"));
        double suma = citesteSumaFactura(cont, I18n.t("PROMPT_BILL_AMOUNT"));

        Tranzactie trx = bancaService.efectueazaPlataFactura(cont.getIban(), furnizor, suma, nrFactura);
        System.out.print(I18n.t("SUCCESS_BILL", trx.getIdTranzactie(), furnizor, suma, cont.getMoneda().name(), cont.getSold(), cont.getMoneda().name()));
    }

    private void optiuneClientIstoric(ContBancar cont) {
        System.out.println("--- ISTORIC TRANZACȚII PERSONALE ---");
        List<Tranzactie> lista = bancaService.filtreazaTranzactiiCont(cont.getIban());

        if (lista.isEmpty()) {
            System.out.println(I18n.t("NO_TRANSACTIONS"));
            return;
        }

        System.out.printf(I18n.t("HISTORY_RESULTS"), lista.size());
        for (Tranzactie t : lista) {
            System.out.println(" -> " + t);
        }
    }

    private void optiuneClientDeschidereSubcont(ContBancar cont) throws BancarException {
        System.out.println("\n--- DESCHIDERE CONT SUPLIMENTAR (SUBCONT) ---");
        System.out.println(I18n.t("CANCEL_NOTE"));

        TipCont tipCont = citesteTipCont();
        Moneda moneda = citesteMoneda();
        double soldInitial = citesteSoldInitial(I18n.t("PROMPT_INIT_BALANCE"));

        String iban = bancaService.genereazaIbanRomanesc(tipCont, moneda);
        System.out.println("\nConfigurare cod PIN securizat pentru noul subcont:");
        String pin = citestePinNouCuConfirmare(
                "Introduceți codul PIN pentru noul subcont (4 cifre): ",
                "Confirmați codul PIN pentru noul subcont: ");

        bancaService.deschideCont(iban, cont.getTitular(), tipCont, moneda, soldInitial, pin);
        System.out.print(I18n.t("SUCCESS_OPEN_ACC", cont.getTitular().getNume(), iban, tipCont.name(), moneda.name(), soldInitial, moneda.name()));
        System.out.println("[INFO] Noul subcont a fost asociat profilului dumneavoastră și este activ.");
    }

    private void optiuneClientSchimbarePin(ContBancar cont) throws OperatiuneAnulataException, BancarException {
        System.out.println("\n--- RESETARE / SCHIMBARE COD PIN ---");
        System.out.println(I18n.t("CANCEL_NOTE"));

        String pinActual = citestePin("Introduceți codul PIN actual pentru verificare: ");
        if (!cont.getPin().equals(pinActual)) {
            System.out.println("[EROARE] Codul PIN actual este incorect! Operațiunea a fost anulată din motive de securitate.");
            return;
        }

        String nouPin = citestePinNouCuConfirmare(I18n.t("PROMPT_NEW_PIN"), I18n.t("PROMPT_CONFIRM_PIN"));
        bancaService.schimbaPin(cont.getIban(), pinActual, nouPin);
        System.out.print(I18n.t("SUCCESS_PIN_CHANGED"));
        System.out.println("[CONFIDENȚIALITATE] Noul cod PIN a fost actualizat. Conform standardelor bancare (PCI-DSS), niciun angajat sau operator al băncii nu are acces la codul dumneavoastră PIN.");
    }

    // =========================================================================
    // MODUL 2: SESIUNE OPERATOR BANCAR & ADMINISTRARE BACK-OFFICE
    // =========================================================================

    private void sesiuneOperator() throws OperatiuneAnulataException {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("       AUTENTIFICARE CONSOLĂ OPERATOR BANCAR & ADMINISTRATOR");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(I18n.t("CANCEL_NOTE"));

        String parola = citesteInputSecurizat(I18n.t("PROMPT_OPERATOR_PASS"));
        if (!bancaService.valideazaParolaOperator(parola)) {
            System.err.println("\n" + I18n.t("ERR_OPERATOR_PASS_INCORRECT"));
            return;
        }

        System.out.println(I18n.t("SUCCESS_LOGIN_OPERATOR"));

        boolean operatorConectat = true;
        while (operatorConectat) {
            afiseazaMeniuOperator();
            int opt = citesteInt(I18n.t("PROMPT_SELECT_OPT"), 0, 8);
            System.out.println();

            try {
                switch (opt) {
                    case 1 -> optiuneAfisareConturi();
                    case 2 -> optiuneOperatorDeschidereCont();
                    case 3 -> optiuneVizualizareMatriceStatistica();
                    case 4 -> optiuneGenerareRaportText();
                    case 5 -> optiuneExportBackupBinar();
                    case 6 -> optiuneRestaurareBackupBinar();
                    case 7 -> optiuneBlocareDeblocareCont();
                    case 8 -> optiuneOperatorSchimbareParola();
                    case 0 -> {
                        operatorConectat = false;
                        System.out.println(I18n.t("LOGOUT_OPERATOR_MSG"));
                    }
                }
            } catch (OperatiuneAnulataException ex) {
                System.out.println("\n" + I18n.t("STATUS_CANCELLED_HEADER") + ex.getMessage());
            } catch (BancarException ex) {
                System.err.println("\n" + I18n.t("ERR_BANKING_HEADER") + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("\n" + I18n.t("ERR_SYSTEM_HEADER") + ex.getMessage());
            }

            if (operatorConectat) {
                System.out.println("\n[ENTER pt a continua în Consola de Operator...]");
                scanner.nextLine();
            }
        }
    }

    private void afiseazaMeniuOperator() {
        System.out.println("==========================================================================");
        System.out.printf("               %s            \n", I18n.t("OPERATOR_MENU_TITLE"));
        System.out.println("==========================================================================");
        System.out.println("  " + I18n.t("OPERATOR_OPT_1"));
        System.out.println("  " + I18n.t("OPERATOR_OPT_2"));
        System.out.println("  " + I18n.t("OPERATOR_OPT_3"));
        System.out.println("  " + I18n.t("OPERATOR_OPT_4"));
        System.out.println("  " + I18n.t("OPERATOR_OPT_5"));
        System.out.println("  " + I18n.t("OPERATOR_OPT_6"));
        System.out.println("  " + I18n.t("OPERATOR_OPT_7"));
        System.out.println("  " + I18n.t("OPERATOR_OPT_8"));
        System.out.println("  " + I18n.t("OPERATOR_OPT_0"));
        System.out.println("==========================================================================");
    }

    private void optiuneOperatorSchimbareParola() throws OperatiuneAnulataException, BancarException {
        System.out.println("\n--- SCHIMBARE PAROLĂ OPERATOR BANCAR ---");
        System.out.println("Cerințe de securitate parolă:");
        System.out.println(" -> Minim 10 caractere");
        System.out.println(" -> Cel puțin o literă mare (A-Z) și o literă mică (a-z)");
        System.out.println(" -> Cel puțin o cifră (0-9)");
        System.out.println(" -> Cel puțin un caracter special (!@#$%^&* etc.)");
        System.out.println(I18n.t("CANCEL_NOTE"));

        String parolaVeche = citesteInputSecurizat(I18n.t("PROMPT_OLD_PASS"));
        if (!bancaService.valideazaParolaOperator(parolaVeche)) {
            System.err.println("\n[EROARE] Parola actuală este incorectă! Operațiune respinsă.");
            return;
        }

        while (true) {
            String nouaParola = citesteInputSecurizat(I18n.t("PROMPT_NEW_PASS"));
            String confirmare = citesteInputSecurizat(I18n.t("PROMPT_CONFIRM_PASS"));

            if (!nouaParola.equals(confirmare)) {
                System.out.print(I18n.t("ERR_PASS_MISMATCH"));
                continue;
            }

            try {
                bancaService.valideazaComplexitateParola(nouaParola);
            } catch (FormatDateIncorectException ex) {
                System.err.println("[EROARE SECURITATE] " + ex.getMessage());
                continue;
            }

            bancaService.schimbaParolaOperator(parolaVeche, nouaParola);
            System.out.print(I18n.t("SUCCESS_PASS_CHANGED"));
            System.out.println("[INFO] Noua parolă a fost actualizată și în 'data\\operator_secret.txt'.");
            break;
        }
    }

    private void optiuneOperatorDeschidereCont() throws BancarException {
        System.out.println(I18n.t("OPEN_ACC_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        String nume = citesteNume(I18n.t("PROMPT_HOLDER_NAME"));
        String cnp = citesteCnp(I18n.t("PROMPT_CNP"));
        String email = citesteEmail(I18n.t("PROMPT_EMAIL"));
        String telefon = citesteTelefon(I18n.t("PROMPT_PHONE"));

        TipCont tipCont = citesteTipCont();
        Moneda moneda = citesteMoneda();
        double soldInitial = citesteSoldInitial(I18n.t("PROMPT_INIT_BALANCE"));

        System.out.println("\nModalitate alocare IBAN:");
        System.out.println("  1. Generare automată IBAN românesc oficial (Recomandat)");
        System.out.println("  2. Introducere manuală IBAN românesc (prefix RO)");
        int optIban = citesteInt("Selectați opțiunea (1-2 sau 0 pt anulare): ", 0, 2);
        if (optIban == 0) {
            throw new OperatiuneAnulataException();
        }

        String iban;
        if (optIban == 1) {
            iban = bancaService.genereazaIbanRomanesc(tipCont, moneda);
            System.out.println("[INFO] IBAN românesc generat automat: " + iban);
        } else {
            iban = citesteIbanNouRomanesc(I18n.t("PROMPT_IBAN_NEW"));
        }

        System.out.println("\nConfigurare cod PIN pe tastatura securizată (PIN-pad):");
        String pin = citestePinNouCuConfirmare(
                "Clientul introduce codul PIN inițial pe PIN-pad (4 cifre): ",
                "Clientul confirmă codul PIN inițial pe PIN-pad (reintroduceți cele 4 cifre): ");

        Client client = new Client("CLI-" + (cnp.length() >= 4 ? cnp.substring(cnp.length() - 4) : "NEW"), nume, cnp, email, telefon);
        bancaService.deschideCont(iban, client, tipCont, moneda, soldInitial, pin);

        System.out.print(I18n.t("SUCCESS_OPEN_ACC", nume, iban, tipCont.name(), moneda.name(), soldInitial, moneda.name()));
        System.out.println("[SECURITATE] Codul PIN a fost configurat și confirmat de client pe tastatura securizată (confidențial - neafișat operatorului).");
    }

    private void optiuneAfisareConturi() {
        System.out.println(I18n.t("ACCOUNTS_TITLE"));
        Collection<ContBancar> conturi = bancaService.getToateConturile();
        if (conturi.isEmpty()) {
            System.out.println(I18n.t("NO_ACCOUNTS"));
            return;
        }

        List<ContBancar> listaSortata = new ArrayList<>(conturi);
        Collections.sort(listaSortata);

        int index = 1;
        for (ContBancar c : listaSortata) {
            System.out.println(index++ + ". " + c);
        }

        System.out.println("--------------------------------------------------------------------------");
        System.out.print(I18n.t("TOTAL_FUNDS_RON", bancaService.calculeazaTotalFonduri(Moneda.RON)));
        System.out.print(I18n.t("TOTAL_FUNDS_EUR", bancaService.calculeazaTotalFonduri(Moneda.EUR)));
    }

    private void optiuneVizualizareMatriceStatistica() {
        System.out.println(I18n.t("MATRIX_TITLE"));
        double[][] matrice = bancaService.getMatriceStatisticaLunara();
        int[][] matriceNr = bancaService.getMatriceNumarTranzactii();

        System.out.printf("%-12s | %-14s | %-14s | %-14s | %-14s\n",
                I18n.t("MATRIX_COL_MONTH"), I18n.t("MATRIX_COL_DEP"), I18n.t("MATRIX_COL_WITH"),
                I18n.t("MATRIX_COL_TRANSF"), I18n.t("MATRIX_COL_BILLS"));
        System.out.println("-----------------------------------------------------------------------------");

        for (int luna = 0; luna < 12; luna++) {
            String numeLuna = Month.of(luna + 1).getDisplayName(TextStyle.FULL, I18n.getLocale());
            numeLuna = numeLuna.substring(0, 1).toUpperCase(I18n.getLocale()) + numeLuna.substring(1);

            System.out.printf("%-12s | %10.2f (%2d) | %10.2f (%2d) | %10.2f (%2d) | %10.2f (%2d)\n",
                    numeLuna,
                    matrice[luna][0], matriceNr[luna][0],
                    matrice[luna][1], matriceNr[luna][1],
                    matrice[luna][2], matriceNr[luna][2],
                    matrice[luna][3], matriceNr[luna][3]);
        }
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println(I18n.t("MATRIX_NOTE"));
    }

    private void optiuneGenerareRaportText() {
        System.out.println(I18n.t("REPORT_TITLE"));
        try {
            raportGenerator.genereazaRaportStatistici(caleFisierRaport, bancaService);
            System.out.print(I18n.t("SUCCESS_REPORT", caleFisierRaport));
        } catch (IOException e) {
            System.out.print(I18n.t("ERR_REPORT", e.getMessage()));
        }
    }

    private void optiuneExportBackupBinar() {
        System.out.println(I18n.t("BACKUP_EXPORT_TITLE"));
        try {
            binaryStorage.salveazaBackupBinar(caleFisierBackup, bancaService.getToateConturile(), bancaService.getToateTranzactiile());
            System.out.print(I18n.t("SUCCESS_BACKUP_EXPORT", caleFisierBackup));
        } catch (IOException e) {
            System.out.print(I18n.t("ERR_BACKUP_EXPORT", e.getMessage()));
        }
    }

    private void optiuneRestaurareBackupBinar() throws OperatiuneAnulataException {
        System.out.println(I18n.t("BACKUP_RESTORE_TITLE"));
        System.out.print(I18n.t("WARN_BACKUP_RESTORE"));
        String confirmare = scanner.nextLine().trim();
        if (confirmare.equals("0") || confirmare.equalsIgnoreCase("nu") || confirmare.equalsIgnoreCase("no") || confirmare.equalsIgnoreCase("cancel")) {
            throw new OperatiuneAnulataException(I18n.t("BACKUP_RESTORE_CANCELLED"));
        }
        if (!confirmare.equalsIgnoreCase("da") && !confirmare.equalsIgnoreCase("yes")) {
            System.out.println(I18n.t("BACKUP_RESTORE_NOT_CONFIRMED"));
            return;
        }

        try {
            binaryStorage.restaureazaBackupBinar(caleFisierBackup, bancaService);
            System.out.print(I18n.t("SUCCESS_BACKUP_RESTORE"));
        } catch (Exception e) {
            System.out.print(I18n.t("ERR_BACKUP_RESTORE", e.getMessage()));
        }
    }

    private void optiuneBlocareDeblocareCont() throws BancarException {
        System.out.println("--- ADMINISTRARE SECURITATE & BLOCARE CONT BANCAR ---");
        System.out.println(I18n.t("CANCEL_NOTE"));

        ContBancar cont = citesteIbanExistent(I18n.t("PROMPT_IBAN_ACCOUNT"), false);

        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("Cont: %s | Titular: %s\n", cont.getIban(), cont.getTitular().getNume());
        System.out.printf("Stare cont: %s | Încercări eșuate PIN: %d | Necesită resetare PIN: %s\n",
                cont.isActiv() ? I18n.t("STATE_ACTIVE") : I18n.t("STATE_LOCKED"),
                cont.getIncercariPinEsuat(),
                cont.isNecesitaResetarePin() ? "DA" : "NU");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Acțiuni administrative disponibile:");
        System.out.println("  1. " + (cont.isActiv() ? "Blocare cont (dezactivare)" : "Deblocare cont (activare)"));
        System.out.println("  2. Resetare număr încercări PIN eșuate (păstrează PIN-ul existent)");
        System.out.println("  3. Inițiere procedură RESETARE PIN (clientul va introduce noul PIN de 2 ori la logare)");
        System.out.println("  0. Anulare / Înapoi");

        int opt = citesteInt("Selectați acțiunea dorită (0-3): ", 0, 3);
        switch (opt) {
            case 1 -> {
                bancaService.blocheazaDeblocheazaCont(cont.getIban(), !cont.isActiv());
                if (cont.isActiv()) {
                    cont.resetIncercariPin();
                }
                String stareNoua = cont.isActiv() ? I18n.t("STATE_ACTIVE") : I18n.t("STATE_LOCKED");
                System.out.print(String.format(I18n.t("SUCCESS_LOCK_UPDATE"), stareNoua));
            }
            case 2 -> {
                bancaService.reseteazaIncercariPin(cont.getIban());
                System.out.println("\n[SUCCES] Numărul de încercări greșite a fost resetat la 0! Contul este activ cu PIN-ul curent.");
            }
            case 3 -> {
                bancaService.solicitaResetarePin(cont.getIban());
                System.out.println("\n[SUCCES] Procedura de Resetare PIN a fost activată pentru contul " + cont.getIban() + "!");
                System.out.println("[INFO] La următoarea logare, clientul va fi solicitat automat să introducă noul cod PIN de două ori.");
                System.out.println("[CONFIDENȚIALITATE] PIN-ul este definit exclusiv de client, operatorul neavând acces la el.");
            }
            case 0 -> throw new OperatiuneAnulataException();
        }
    }

    private void optiuneSchimbareLimba() {
        System.out.println(I18n.t("LANG_TITLE"));
        System.out.print(I18n.t("LANG_CURRENT", I18n.getLimba().getDenumire()));
        System.out.println(I18n.t("LANG_OPT_1"));
        System.out.println(I18n.t("LANG_OPT_2"));
        System.out.println(I18n.t("LANG_OPT_0"));

        int opt = citesteInt(I18n.t("PROMPT_LANG_SELECT"), 0, 2);
        if (opt == 1) {
            I18n.setLimba(Limba.ROMANA);
            System.out.println(I18n.t("SUCCESS_LANG_RO"));
        } else if (opt == 2) {
            I18n.setLimba(Limba.ENGLEZA);
            System.out.println(I18n.t("SUCCESS_LANG_EN"));
        }
    }

    public void optiuneSalvareSiIesire() {
        System.out.println(I18n.t("EXIT_TITLE"));
        try {
            System.out.print(I18n.t("SAVING_ACCOUNTS", caleFisierConturi));
            csvStorage.salveazaConturi(caleFisierConturi, bancaService.getToateConturile());

            System.out.print(I18n.t("SAVING_TRANSACTIONS", caleFisierTranzactii));
            csvStorage.salveazaTranzactii(caleFisierTranzactii, bancaService.getToateTranzactiile());

            System.out.print(I18n.t("SUCCESS_SAVED"));
        } catch (IOException e) {
            System.err.println("[EROARE CRITICĂ LA SALVARE] " + e.getMessage());
        }

        aplicatieRuland = false;
        System.out.println("\n" + I18n.t("GOODBYE"));
    }

    // =========================================================================
    // METODE AJUTĂTOARE PENTRU CITIRE ȘI VALIDARE IMEDIATĂ ÎN CONSOLĂ
    // =========================================================================

    private String citesteLinieSauAnulare(String prompt) throws OperatiuneAnulataException {
        System.out.print(prompt);
        String val = scanner.nextLine().trim();
        if (esteComandaAnulare(val)) {
            throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
        }
        while (val.isEmpty()) {
            System.out.print(I18n.t("ERR_FIELD_EMPTY"));
            val = scanner.nextLine().trim();
            if (esteComandaAnulare(val)) {
                throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
            }
        }
        return val;
    }

    private boolean esteComandaAnulare(String val) {
        return "0".equals(val) || "cancel".equalsIgnoreCase(val) || "anulare".equalsIgnoreCase(val) || "q".equalsIgnoreCase(val);
    }

    private String citesteInputSecurizat(String prompt) throws OperatiuneAnulataException {
        Console console = System.console();
        if (console != null) {
            char[] secretChars = console.readPassword("%s", prompt);
            if (secretChars == null) {
                throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
            }
            String secret = new String(secretChars).trim();
            if (esteComandaAnulare(secret)) {
                throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
            }
            while (secret.isEmpty()) {
                System.out.print(I18n.t("ERR_FIELD_EMPTY"));
                secretChars = console.readPassword();
                if (secretChars == null) {
                    throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
                }
                secret = new String(secretChars).trim();
                if (esteComandaAnulare(secret)) {
                    throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
                }
            }
            return secret;
        } else {
            return citesteLinieSauAnulare(prompt);
        }
    }

    private String citestePin(String prompt) throws OperatiuneAnulataException {
        while (true) {
            String val = citesteInputSecurizat(prompt);
            if (val.matches("^\\d{4}$")) {
                return val;
            }
            System.out.print(I18n.t("ERR_PIN_FORMAT"));
        }
    }

    private String citestePinNouCuConfirmare(String prompt1, String prompt2) throws OperatiuneAnulataException {
        while (true) {
            String p1 = citestePin(prompt1);
            try {
                bancaService.valideazaComplexitatePin(p1);
            } catch (FormatDateIncorectException ex) {
                System.out.println("\n[EROARE SECURITATE PIN] " + ex.getMessage());
                System.out.println("Vă rugăm să alegeți un cod PIN mai sigur conform standardelor bancare.\n");
                continue;
            }

            String p2 = citestePin(prompt2);
            if (p1.equals(p2)) {
                return p1;
            }
            System.out.println();
            System.out.print(I18n.t("ERR_PIN_MISMATCH"));
            System.out.println();
        }
    }

    private ContBancar citesteIbanExistent(String prompt, boolean necesitaActiv) throws OperatiuneAnulataException {
        while (true) {
            String input = citesteLinieSauAnulare(prompt);
            String cleanIban = input.toUpperCase().replaceAll("\\s+", "");

            try {
                bancaService.valideazaIban(cleanIban);
            } catch (FormatDateIncorectException ex) {
                System.out.print(I18n.t("ERR_IBAN_INVALID", ex.getMessage()));
                continue;
            }

            ContBancar cont;
            try {
                cont = bancaService.gasesteCont(cleanIban);
            } catch (ContInexistentException ex) {
                System.out.print(I18n.t("ERR_IBAN_NOT_FOUND", cleanIban));
                continue;
            }

            if (necesitaActiv && !cont.isActiv()) {
                System.out.print(I18n.t("ERR_IBAN_BLOCKED", cleanIban, cont.getTitular().getNume()));
                continue;
            }

            System.out.print(I18n.t("INFO_ACCOUNT_FOUND",
                    cont.getIban(), cont.getTitular().getNume(), cont.getSold(), cont.getMoneda().name(),
                    cont.isActiv() ? I18n.t("STATE_ACTIVE") : I18n.t("STATE_LOCKED")));
            return cont;
        }
    }

    private String citesteIbanDestinatie(String ibanSursa) throws OperatiuneAnulataException {
        while (true) {
            String input = citesteLinieSauAnulare(I18n.t("PROMPT_IBAN_DEST"));
            String cleanIban = input.toUpperCase().replaceAll("\\s+", "");

            if (cleanIban.equalsIgnoreCase(ibanSursa)) {
                System.out.print(I18n.t("ERR_SAME_ACCOUNT"));
                continue;
            }

            try {
                bancaService.valideazaIban(cleanIban);
            } catch (FormatDateIncorectException ex) {
                System.out.print(I18n.t("ERR_IBAN_INVALID", ex.getMessage()));
                continue;
            }

            try {
                ContBancar contDest = bancaService.gasesteCont(cleanIban);
                if (!contDest.isActiv()) {
                    System.out.print(I18n.t("ERR_IBAN_BLOCKED", cleanIban, contDest.getTitular().getNume()));
                    continue;
                }
                System.out.print(I18n.t("INFO_ACCOUNT_FOUND",
                        contDest.getIban(), contDest.getTitular().getNume(), contDest.getSold(), contDest.getMoneda().name(),
                        I18n.t("STATE_ACTIVE")));
            } catch (ContInexistentException e) {
                String codTara = cleanIban.substring(0, 2);
                System.out.printf("[INFO] Cont extern internațional recunoscut (%s) conform ISO 13616. Se va procesa ca transfer transfrontalier.\n", codTara);
            }

            return cleanIban;
        }
    }

    private String citesteIbanNouRomanesc(String prompt) throws OperatiuneAnulataException {
        while (true) {
            String input = citesteLinieSauAnulare(prompt);
            String cleanIban = input.toUpperCase().replaceAll("\\s+", "");

            if (!cleanIban.startsWith("RO")) {
                System.out.println("[EROARE] Banca noastră poate deschide conturi exclusiv cu IBAN românesc (prefix RO). Reîncercați.");
                continue;
            }

            try {
                bancaService.valideazaIban(cleanIban);
            } catch (FormatDateIncorectException ex) {
                System.out.print(I18n.t("ERR_IBAN_INVALID", ex.getMessage()));
                continue;
            }

            try {
                ContBancar existent = bancaService.gasesteCont(cleanIban);
                System.out.print(I18n.t("ERR_IBAN_EXISTS", cleanIban, existent.getTitular().getNume()));
                continue;
            } catch (ContInexistentException e) {
                // IBAN-ul este disponibil
            }

            return cleanIban;
        }
    }

    private String citesteNume(String prompt) throws OperatiuneAnulataException {
        while (true) {
            String nume = citesteLinieSauAnulare(prompt);
            if (nume.length() < 3 || nume.matches("^\\d+$")) {
                System.out.print(I18n.t("ERR_NAME_INVALID"));
                continue;
            }
            return nume;
        }
    }

    private String citesteCnp(String prompt) throws OperatiuneAnulataException {
        while (true) {
            String cnp = citesteLinieSauAnulare(prompt);
            if (!cnp.matches("^\\d{13}$")) {
                System.out.print(I18n.t("ERR_CNP_INVALID"));
                continue;
            }
            return cnp;
        }
    }

    private String citesteEmail(String prompt) throws OperatiuneAnulataException {
        while (true) {
            String email = citesteLinieSauAnulare(prompt);
            if (!email.contains("@") || !email.contains(".") || email.length() < 5 || email.indexOf('@') > email.lastIndexOf('.')) {
                System.out.print(I18n.t("ERR_EMAIL_INVALID"));
                continue;
            }
            return email;
        }
    }

    private String citesteTelefon(String prompt) throws OperatiuneAnulataException {
        while (true) {
            String tel = citesteLinieSauAnulare(prompt);
            String cleanTel = tel.replaceAll("[\\s\\-+()]", "");
            if (cleanTel.length() < 10 || !cleanTel.matches("^\\d+$")) {
                System.out.print(I18n.t("ERR_PHONE_INVALID"));
                continue;
            }
            return tel;
        }
    }

    private double citesteSumaDepunere(String prompt) throws OperatiuneAnulataException {
        while (true) {
            String linie = citesteLinieSauAnulare(prompt);
            try {
                double val = Double.parseDouble(linie.replace(',', '.'));
                if (val <= 0) {
                    System.out.print(I18n.t("ERR_AMOUNT_POSITIVE_DEP"));
                    continue;
                }
                return val;
            } catch (NumberFormatException ex) {
                System.out.println(I18n.t("ERR_NUMERIC_FORMAT"));
            }
        }
    }

    private double citesteSumaRetragere(ContBancar cont, String prompt) throws OperatiuneAnulataException {
        while (true) {
            System.out.printf(I18n.t("INFO_CURRENT_BALANCE"), cont.getSold(), cont.getMoneda().name());
            String linie = citesteLinieSauAnulare(prompt);
            try {
                double val = Double.parseDouble(linie.replace(',', '.'));
                if (val <= 0) {
                    System.out.print(I18n.t("ERR_AMOUNT_POSITIVE_WITH"));
                    continue;
                }

                double comision = Math.max(1.0, val * 0.002);
                double totalNecesar = val + comision;

                if (totalNecesar > cont.getSold()) {
                    System.out.print(I18n.t("ERR_INSUFFICIENT_FUNDS_WITHDRAW", val, cont.getMoneda().name(), totalNecesar, cont.getMoneda().name(), comision, cont.getMoneda().name(), cont.getSold(), cont.getMoneda().name()));
                    continue;
                }

                return val;
            } catch (NumberFormatException ex) {
                System.out.println(I18n.t("ERR_NUMERIC_FORMAT"));
            }
        }
    }

    private double citesteSumaTransfer(ContBancar contSursa, String prompt) throws OperatiuneAnulataException {
        while (true) {
            System.out.printf(I18n.t("INFO_SOURCE_BALANCE"), contSursa.getSold(), contSursa.getMoneda().name());
            String linie = citesteLinieSauAnulare(prompt);
            try {
                double val = Double.parseDouble(linie.replace(',', '.'));
                if (val <= 0) {
                    System.out.print(I18n.t("ERR_AMOUNT_POSITIVE_TRANSF"));
                    continue;
                }

                double comision = Math.max(2.0, val * 0.005);
                double totalNecesar = val + comision;

                if (totalNecesar > contSursa.getSold()) {
                    System.out.print(I18n.t("ERR_INSUFFICIENT_FUNDS_TRANSFER", totalNecesar, contSursa.getMoneda().name(), val, comision, contSursa.getSold(), contSursa.getMoneda().name()));
                    continue;
                }

                return val;
            } catch (NumberFormatException ex) {
                System.out.println(I18n.t("ERR_NUMERIC_FORMAT"));
            }
        }
    }

    private double citesteSumaFactura(ContBancar cont, String prompt) throws OperatiuneAnulataException {
        while (true) {
            System.out.printf(I18n.t("INFO_CURRENT_BALANCE"), cont.getSold(), cont.getMoneda().name());
            String linie = citesteLinieSauAnulare(prompt);
            try {
                double val = Double.parseDouble(linie.replace(',', '.'));
                if (val <= 0) {
                    System.out.print(I18n.t("ERR_AMOUNT_POSITIVE_BILL"));
                    continue;
                }

                if (val > cont.getSold()) {
                    System.out.print(I18n.t("ERR_INSUFFICIENT_FUNDS_BILL", val, cont.getMoneda().name(), cont.getSold(), cont.getMoneda().name()));
                    continue;
                }

                return val;
            } catch (NumberFormatException ex) {
                System.out.println(I18n.t("ERR_NUMERIC_FORMAT"));
            }
        }
    }

    private double citesteSoldInitial(String prompt) throws OperatiuneAnulataException {
        while (true) {
            System.out.print(prompt);
            String linie = scanner.nextLine().trim();
            if (linie.isEmpty()) {
                return 0.0;
            }
            if (esteComandaAnulare(linie)) {
                throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
            }
            try {
                double val = Double.parseDouble(linie.replace(',', '.'));
                if (val < 0) {
                    System.out.print(I18n.t("ERR_INIT_BALANCE_NEGATIVE"));
                    continue;
                }
                return val;
            } catch (NumberFormatException ex) {
                System.out.println(I18n.t("ERR_NUMERIC_FORMAT"));
            }
        }
    }

    private String citesteSirObligatoriu(String prompt) throws OperatiuneAnulataException {
        return citesteLinieSauAnulare(prompt);
    }

    private String citesteDescriereOptionala(String prompt, String valoareImplicita) throws OperatiuneAnulataException {
        System.out.print(prompt);
        String val = scanner.nextLine().trim();
        if (esteComandaAnulare(val)) {
            throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
        }
        return val.isEmpty() ? valoareImplicita : val;
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
                System.out.printf(I18n.t("ERR_INT_RANGE"), min, max);
            } catch (NumberFormatException ex) {
                System.out.println(I18n.t("ERR_NOT_INT"));
            }
        }
    }

    private Moneda citesteMoneda() throws OperatiuneAnulataException {
        System.out.println(I18n.t("CURRENCIES_MENU"));
        int opt = citesteInt(I18n.t("PROMPT_SELECT_CURRENCY"), 0, 4);
        if (opt == 0) {
            throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
        }
        return switch (opt) {
            case 2 -> Moneda.EUR;
            case 3 -> Moneda.USD;
            case 4 -> Moneda.GBP;
            default -> Moneda.RON;
        };
    }

    private TipCont citesteTipCont() throws OperatiuneAnulataException {
        System.out.println(I18n.t("ACC_TYPES_MENU"));
        int opt = citesteInt(I18n.t("PROMPT_SELECT_ACC_TYPE"), 0, 3);
        if (opt == 0) {
            throw new OperatiuneAnulataException(I18n.t("OP_CANCELLED"));
        }
        return switch (opt) {
            case 2 -> TipCont.ECONOMII;
            case 3 -> TipCont.DEPOZIT;
            default -> TipCont.CURENT;
        };
    }
}
