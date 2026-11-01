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

import java.io.IOException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Interfața interactivă de tip consolă pentru sistemul de e-banking.
 * Oferă suport bilingv (Română cu diacritice complete și Engleză),
 * validare defensivă imediată a inputului cu reîncercare sau anulare prin tasta '0'.
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
            afiseazaMeniuPrincipal();
            int optiune = citesteInt(I18n.t("PROMPT_SELECT_OPT"), 0, 13);
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
                    case 13 -> optiuneSchimbareLimba();
                    case 0 -> optiuneSalvareSiIesire();
                }
            } catch (OperatiuneAnulataException ex) {
                System.out.println("\n" + I18n.t("STATUS_CANCELLED_HEADER") + ex.getMessage());
            } catch (BancarException ex) {
                System.err.println("\n" + I18n.t("ERR_BANKING_HEADER") + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("\n" + I18n.t("ERR_SYSTEM_HEADER") + ex.getMessage());
            }

            if (aplicatieRuland) {
                System.out.println("\n" + I18n.t("PRESS_ENTER"));
                scanner.nextLine();
            }
        }
    }

    private void afiseazaMeniuPrincipal() {
        System.out.println("==========================================================================");
        System.out.printf("                   %s                \n", I18n.t("MENU_TITLE"));
        System.out.println("==========================================================================");
        System.out.println("  " + I18n.t("OPT_1"));
        System.out.println("  " + I18n.t("OPT_2"));
        System.out.println("  " + I18n.t("OPT_3"));
        System.out.println("  " + I18n.t("OPT_4"));
        System.out.println("  " + I18n.t("OPT_5"));
        System.out.println("  " + I18n.t("OPT_6"));
        System.out.println("  " + I18n.t("OPT_7"));
        System.out.println("  " + I18n.t("OPT_8"));
        System.out.println("  " + I18n.t("OPT_9"));
        System.out.println(" " + I18n.t("OPT_10"));
        System.out.println(" " + I18n.t("OPT_11"));
        System.out.println(" " + I18n.t("OPT_12"));
        System.out.println(" " + I18n.t("OPT_13"));
        System.out.println(" " + I18n.t("OPT_0"));
        System.out.println("==========================================================================");
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

    private void optiuneDeschidereCont() throws BancarException {
        System.out.println(I18n.t("OPEN_ACC_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        String iban = citesteIbanNou(I18n.t("PROMPT_IBAN_NEW"));
        String nume = citesteNume(I18n.t("PROMPT_HOLDER_NAME"));
        String cnp = citesteCnp(I18n.t("PROMPT_CNP"));
        String email = citesteEmail(I18n.t("PROMPT_EMAIL"));
        String telefon = citesteTelefon(I18n.t("PROMPT_PHONE"));

        TipCont tipCont = citesteTipCont();
        Moneda moneda = citesteMoneda();
        double soldInitial = citesteSoldInitial(I18n.t("PROMPT_INIT_BALANCE"));

        Client client = new Client("CLI-" + (cnp.length() >= 4 ? cnp.substring(cnp.length() - 4) : "NEW"), nume, cnp, email, telefon);
        bancaService.deschideCont(iban, client, tipCont, moneda, soldInitial);

        System.out.print(I18n.t("SUCCESS_OPEN_ACC", nume, iban, tipCont.name(), moneda.name(), soldInitial, moneda.name()));
    }

    private void optiuneDepunere() throws BancarException {
        System.out.println(I18n.t("DEPOSIT_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        ContBancar cont = citesteIbanExistent(I18n.t("PROMPT_IBAN_ACCOUNT"), true);
        double suma = citesteSumaDepunere(I18n.t("PROMPT_AMOUNT_DEPOSIT"));
        String descriere = citesteDescriereOptionala(I18n.t("PROMPT_DESC_DEPOSIT"), I18n.t("DEFAULT_DESC_DEPOSIT"));

        Tranzactie trx = bancaService.efectueazaDepunere(cont.getIban(), suma, descriere);

        System.out.print(I18n.t("SUCCESS_DEPOSIT", trx.getIdTranzactie(), suma, cont.getMoneda().name(), cont.getSold(), cont.getMoneda().name()));
    }

    private void optiuneRetragere() throws BancarException {
        System.out.println(I18n.t("WITHDRAW_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        ContBancar cont = citesteIbanExistent(I18n.t("PROMPT_IBAN_ACCOUNT"), true);
        double suma = citesteSumaRetragere(cont, I18n.t("PROMPT_AMOUNT_WITHDRAW"));
        String descriere = citesteDescriereOptionala(I18n.t("PROMPT_DESC_WITHDRAW"), I18n.t("DEFAULT_DESC_WITHDRAW"));

        Tranzactie trx = bancaService.efectueazaRetragere(cont.getIban(), suma, descriere);

        System.out.print(I18n.t("SUCCESS_WITHDRAW", trx.getIdTranzactie(), suma, cont.getMoneda().name(), trx.getComision(), cont.getMoneda().name(), cont.getSold(), cont.getMoneda().name()));
    }

    private void optiuneTransfer() throws BancarException {
        System.out.println(I18n.t("TRANSFER_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        ContBancar contSursa = citesteIbanExistent(I18n.t("PROMPT_IBAN_SOURCE"), true);

        ContBancar contDest;
        while (true) {
            contDest = citesteIbanExistent(I18n.t("PROMPT_IBAN_DEST"), true);
            if (contSursa.getIban().equalsIgnoreCase(contDest.getIban())) {
                System.out.print(I18n.t("ERR_SAME_ACCOUNT"));
                continue;
            }
            break;
        }

        double suma = citesteSumaTransfer(contSursa, I18n.t("PROMPT_AMOUNT_TRANSFER", contSursa.getMoneda().name()));
        String descriere = citesteDescriereOptionala(I18n.t("PROMPT_DESC_TRANSFER"), I18n.t("DEFAULT_DESC_TRANSFER"));

        Tranzactie trx = bancaService.efectueazaTransfer(contSursa.getIban(), contDest.getIban(), suma, descriere);

        System.out.print(I18n.t("SUCCESS_TRANSFER", trx.getIdTranzactie(), trx.getComision(), contSursa.getMoneda().name(),
                contSursa.getIban(), contSursa.getSold(), contSursa.getMoneda().name(),
                contDest.getIban(), contDest.getSold(), contDest.getMoneda().name()));
    }

    private void optiunePlataFactura() throws BancarException {
        System.out.println(I18n.t("BILL_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        ContBancar cont = citesteIbanExistent(I18n.t("PROMPT_IBAN_PAYER"), true);
        String furnizor = citesteSirObligatoriu(I18n.t("PROMPT_PROVIDER"));
        String nrFactura = citesteSirObligatoriu(I18n.t("PROMPT_BILL_NUM"));
        double suma = citesteSumaFactura(cont, I18n.t("PROMPT_BILL_AMOUNT"));

        Tranzactie trx = bancaService.efectueazaPlataFactura(cont.getIban(), furnizor, suma, nrFactura);

        System.out.print(I18n.t("SUCCESS_BILL", trx.getIdTranzactie(), furnizor, suma, cont.getMoneda().name(), cont.getSold(), cont.getMoneda().name()));
    }

    private void optiuneIstoricTranzactii() throws OperatiuneAnulataException {
        System.out.println(I18n.t("HISTORY_TITLE"));
        System.out.println(I18n.t("HISTORY_OPT_ALL"));
        System.out.println(I18n.t("HISTORY_OPT_FILTER"));
        System.out.println(I18n.t("HISTORY_OPT_CANCEL"));
        int opt = citesteInt(I18n.t("PROMPT_HISTORY_MODE"), 0, 2);

        if (opt == 0) {
            throw new OperatiuneAnulataException(I18n.t("HISTORY_CANCELLED"));
        }

        List<Tranzactie> lista;
        if (opt == 1) {
            lista = new ArrayList<>(bancaService.getToateTranzactiile());
            Collections.sort(lista);
        } else {
            ContBancar cont = citesteIbanExistent(I18n.t("PROMPT_IBAN_FILTER"), false);
            lista = bancaService.filtreazaTranzactiiCont(cont.getIban());
        }

        if (lista.isEmpty()) {
            System.out.println(I18n.t("NO_TRANSACTIONS"));
            return;
        }

        System.out.printf(I18n.t("HISTORY_RESULTS", lista.size()));
        for (Tranzactie t : lista) {
            System.out.println(" -> " + t);
        }
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
        System.out.println(I18n.t("LOCK_TITLE"));
        System.out.println(I18n.t("CANCEL_NOTE"));

        ContBancar cont = citesteIbanExistent(I18n.t("PROMPT_IBAN_ACCOUNT"), false);

        System.out.print(I18n.t("CURRENT_STATE", cont.isActiv() ? I18n.t("STATE_ACTIVE") : I18n.t("STATE_LOCKED")));
        String actiune = cont.isActiv() ? I18n.t("ACTION_LOCK") : I18n.t("ACTION_UNLOCK");
        System.out.print(I18n.t("PROMPT_LOCK_ACTION", actiune));
        String raspuns = scanner.nextLine().trim();

        if (raspuns.equals("0") || raspuns.equalsIgnoreCase("cancel") || raspuns.equalsIgnoreCase("anulare")) {
            throw new OperatiuneAnulataException();
        }

        if (raspuns.equalsIgnoreCase("da") || raspuns.equalsIgnoreCase("yes")) {
            bancaService.blocheazaDeblocheazaCont(cont.getIban(), !cont.isActiv());
            String stareNoua = !cont.isActiv() ? I18n.t("STATE_LOCKED") : I18n.t("STATE_ACTIVE");
            System.out.print(I18n.t("SUCCESS_LOCK_UPDATE", stareNoua));
        } else {
            System.out.println(I18n.t("NO_CHANGE"));
        }
    }

    /**
     * Comuta limba interfetei intre Romana (cu diacritice) si Engleza.
     */
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
            System.err.println("[EROARE CRITICA LA SALVARE] " + e.getMessage());
        }

        aplicatieRuland = false;
        System.out.println("\n" + I18n.t("GOODBYE"));
    }

    // =========================================================================
    // METODE AJUTATOARE PENTRU CITIRE SI VALIDARE IMEDIATA IN CONSOLA (Cerinta 5)
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

    private String citesteIbanNou(String prompt) throws OperatiuneAnulataException {
        while (true) {
            String input = citesteLinieSauAnulare(prompt);
            String cleanIban = input.toUpperCase().replaceAll("\\s+", "");

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
            System.out.print(I18n.t("INFO_CURRENT_BALANCE", cont.getSold(), cont.getMoneda().name()));
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
            System.out.print(I18n.t("INFO_SOURCE_BALANCE", contSursa.getSold(), contSursa.getMoneda().name()));
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
            System.out.print(I18n.t("INFO_CURRENT_BALANCE", cont.getSold(), cont.getMoneda().name()));
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
                System.out.print(I18n.t("ERR_INT_RANGE", min, max));
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
