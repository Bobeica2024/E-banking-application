package ro.ase.ppoo.ebanking;

import ro.ase.ppoo.ebanking.exception.*;
import ro.ase.ppoo.ebanking.i18n.*;
import ro.ase.ppoo.ebanking.model.*;
import ro.ase.ppoo.ebanking.service.BancaService;
import ro.ase.ppoo.ebanking.storage.BinaryStorage;
import ro.ase.ppoo.ebanking.storage.CsvStorage;
import ro.ase.ppoo.ebanking.storage.RaportGenerator;

import java.io.File;
import java.util.Collection;

/**
 * Suita de teste unitare si de integrare pentru validarea cerintelor aplicatiei PPOO.
 * Executa verificari automate fara a necesita dependinte externe (JUnit/Maven).
 */
public class BancaTest {

    private static int testeTrecute = 0;
    private static int testeTotale = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("     RULARE SUITA DE TESTE AUTOMATE SISTEM E-BANKING      ");
        System.out.println("==========================================================");

        ruleazaTest("Test deschidere cont si colectii", BancaTest::testDeschidereCont);
        ruleazaTest("Test validare IBAN romanesc si international", BancaTest::testValidareIban);
        ruleazaTest("Test depunere si retragere", BancaTest::testDepunereRetragere);
        ruleazaTest("Test exceptie fonduri insuficiente", BancaTest::testExceptieFonduriInsuficiente);
        ruleazaTest("Test transfer valutar si vector rate (masiv 1D)", BancaTest::testTransferValutar);
        ruleazaTest("Test matrice statistica bidimensionala (mMasiv 2D)", BancaTest::testMatriceStatistica);
        ruleazaTest("Test exceptie cont blocat si inexistent", BancaTest::testExceptiiCont);
        ruleazaTest("Test persistenta CSV (salvare/incarcare)", BancaTest::testPersistentaCsv);
        ruleazaTest("Test persistenta binar (salvare/restaurare)", BancaTest::testPersistentaBinar);
        ruleazaTest("Test generare raport text statistici", BancaTest::testGenerareRaport);
        ruleazaTest("Test autentificare PIN si blocare cont la 3 incercari", BancaTest::testAutentificarePinSiBlocareLaTreiIncercari);
        ruleazaTest("Test securitate si validare parola operator", BancaTest::testSecuritateParolaOperator);
        ruleazaTest("Test generare IBAN RO si restrictie deschidere", BancaTest::testGenerareIbanSiRestrictieDeschidere);
        ruleazaTest("Test transfer international catre IBAN extern", BancaTest::testTransferInternationalContExtern);

        System.out.println("==========================================================");
        System.out.printf("REZULTAT TESTE: %d / %d TESTE REUSITE (100%% SUCCESS)\n", testeTrecute, testeTotale);
        System.out.println("==========================================================");

        if (testeTrecute != testeTotale) {
            System.exit(1);
        }
    }

    private static void ruleazaTest(String numeTest, RunnableTest test) {
        testeTotale++;
        try {
            test.run();
            testeTrecute++;
            System.out.printf("[PASS] %-48s OK\n", numeTest);
        } catch (Throwable t) {
            System.out.printf("[FAIL] %-48s ESUAT: %s\n", numeTest, t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    private static void testDeschidereCont() throws Exception {
        BancaService service = new BancaService();
        Client c = new Client("CLI-01", "George Ifrim", "1900101123456", "georgeifrim93@gmail.ro", "0720123456");
        service.deschideCont("RO01BTRLRONCRT0000000001", c, TipCont.CURENT, Moneda.RON, 500.0);

        ContBancar gasit = service.gasesteCont("RO01BTRLRONCRT0000000001");
        afirma(gasit != null, "Contul nu a fost gasit in memorie");
        afirma(gasit.getSold() == 500.0, "Soldul initial este incorect");
        afirma(service.getToateConturile().size() == 1, "Numarul total de conturi este incorect");
        afirma(service.getTotiClientii().contains(c), "Clientul nu a fost adaugat in HashSet");
    }

    private static void testValidareIban() throws Exception {
        BancaService service = new BancaService();

        // 1. IBAN Romanesc valid (exact 24 caractere)
        service.valideazaIban("RO49BTRLRONCRT0012345601");

        // 2. IBAN-uri internationale valide (intre 15 si 34 caractere conform ISO 13616)
        service.valideazaIban("DE89370400440532013000"); // Germania (22 caractere)
        service.valideazaIban("GB29NWBK60161331926819"); // Marea Britanie (22 caractere)
        service.valideazaIban("FR1420041010050500013M02606"); // Franta (27 caractere)
        service.valideazaIban("MT84MALT01100000000000000000123"); // Malta (31 caractere)

        // 3. IBAN Romanesc invalid (23 caractere in loc de 24)
        boolean aPicatRo23 = false;
        try {
            service.valideazaIban("RO49BTRLRONCRT001234560");
        } catch (FormatDateIncorectException ex) {
            aPicatRo23 = true;
        }
        afirma(aPicatRo23, "Trebuia sa arunce exceptie pentru IBAN RO cu 23 de caractere");

        // 4. IBAN peste limita maxima internationala de 34 caractere (ex: 35 caractere)
        boolean aPicatPeste34 = false;
        try {
            service.valideazaIban("DE893704004405320130001111222233334444");
        } catch (FormatDateIncorectException ex) {
            aPicatPeste34 = true;
        }
        afirma(aPicatPeste34, "Trebuia sa arunce exceptie pentru IBAN de peste 34 caractere");

        // 5. IBAN sub limita minima internationala (< 15 caractere)
        boolean aPicatSub15 = false;
        try {
            service.valideazaIban("RO1234567890");
        } catch (FormatDateIncorectException ex) {
            aPicatSub15 = true;
        }
        afirma(aPicatSub15, "Trebuia sa arunce exceptie pentru IBAN sub 15 caractere");

        // 6. IBAN Germania gresit ca lungime (21 caractere in loc de 22)
        boolean aPicatDeLungime = false;
        try {
            service.valideazaIban("DE8937040044053201300");
        } catch (FormatDateIncorectException ex) {
            aPicatDeLungime = true;
        }
        afirma(aPicatDeLungime, "Trebuia sa arunce exceptie pentru IBAN Germania cu lungime != 22");

        // 7. IBAN Germania cu litere in loc de cifre (Germania foloseste doar cifre dupa DE)
        boolean aPicatDeFormat = false;
        try {
            service.valideazaIban("DE89370400440532ABCD00");
        } catch (FormatDateIncorectException ex) {
            aPicatDeFormat = true;
        }
        afirma(aPicatDeFormat, "Trebuia sa arunce exceptie pentru IBAN Germania continand litere in numarul de cont");

        // 8. IBAN Franta gresit ca lungime (25 caractere in loc de 27)
        boolean aPicatFrLungime = false;
        try {
            service.valideazaIban("FR1420041010050500013M026");
        } catch (FormatDateIncorectException ex) {
            aPicatFrLungime = true;
        }
        afirma(aPicatFrLungime, "Trebuia sa arunce exceptie pentru IBAN Franta cu lungime != 27");

        // 9. Validare tari nordice (Norvegia 15, Danemarca 18, Suedia 24) si Elvetia (21)
        service.valideazaIban("NO9386011117947"); // Norvegia (15 caractere)
        service.valideazaIban("DK5000400440105849"); // Danemarca (18 caractere)
        service.valideazaIban("SE4550000000054910000003"); // Suedia (24 caractere)
        service.valideazaIban("CH9300762011623852957"); // Elvetia (21 caractere)

        // 10. Validare state internationale / non-SEPA (SUA, Canada, Australia, Noua Zeelanda)
        service.valideazaIban("US64SVBKUS6S3330058864"); // SUA (22 caractere)
        service.valideazaIban("CA12000400010001234567"); // Canada (22 caractere)
        service.valideazaIban("AU1200000012345678"); // Australia (18 caractere)
        service.valideazaIban("NZ1200010001234567"); // Noua Zeelanda (18 caractere)

        // 11. Norvegia gresita (16 caractere in loc de 15)
        boolean aPicatNo = false;
        try {
            service.valideazaIban("NO93860111179470");
        } catch (FormatDateIncorectException ex) {
            aPicatNo = true;
        }
        afirma(aPicatNo, "Trebuia sa arunce exceptie pentru IBAN Norvegia cu lungime != 15");

        // 12. Elvetia gresita (20 caractere in loc de 21)
        boolean aPicatCh = false;
        try {
            service.valideazaIban("CH930076201162385295");
        } catch (FormatDateIncorectException ex) {
            aPicatCh = true;
        }
        afirma(aPicatCh, "Trebuia sa arunce exceptie pentru IBAN Elvetia cu lungime != 21");

        // 13. Validare tari vecine cu Romania si Turcia
        service.valideazaIban("MD24AG000000000123456789"); // Moldova (exact 24 caractere)
        service.valideazaIban("UA213000010000000041234567890"); // Ucraina (exact 29 caractere)
        service.valideazaIban("RS35260000000123456789"); // Serbia (exact 22 caractere)
        service.valideazaIban("TR330006100512345678901234"); // Turcia (exact 26 caractere)

        // 14. Turcia gresita ca lungime (25 in loc de 26)
        boolean aPicatTr = false;
        try {
            service.valideazaIban("TR33000610051234567890123");
        } catch (FormatDateIncorectException ex) {
            aPicatTr = true;
        }
        afirma(aPicatTr, "Trebuia sa arunce exceptie pentru IBAN Turcia cu lungime != 26");

        // 15. Validare alte state UE si Balcanice (Croatia 21, Bosnia 20, Muntenegru 22, Macedonia 19, Albania 28, Cipru 28, Estonia 20, Slovacia 24)
        service.valideazaIban("HR1212345678901234567"); // Croatia (21 caractere)
        service.valideazaIban("BA391234567890123456"); // Bosnia si Hertegovina (20 caractere)
        service.valideazaIban("ME25123456789012345678"); // Muntenegru (22 caractere)
        service.valideazaIban("MK07200123456789012"); // Macedonia de Nord (19 caractere)
        service.valideazaIban("AL35212110090000000235698741"); // Albania (28 caractere)
        service.valideazaIban("CY17002001280000001200527600"); // Cipru (28 caractere)
        service.valideazaIban("EE382200221020145682"); // Estonia (20 caractere)
        service.valideazaIban("SK3112000000001987426375"); // Slovacia (24 caractere)

        // 16. Bosnia gresita (21 caractere in loc de 20)
        boolean aPicatBa = false;
        try {
            service.valideazaIban("BA3912345678901234560");
        } catch (FormatDateIncorectException ex) {
            aPicatBa = true;
        }
        afirma(aPicatBa, "Trebuia sa arunce exceptie pentru IBAN Bosnia cu lungime != 20");

        // 17. Validare orientul mijlociu (EAU/Dubai 23, Israel 23), America Latina (Brazilia 29), Asia (Pakistan 24, Nepal, India)
        service.valideazaIban("AE260331234567890123456"); // EAU (23 caractere)
        service.valideazaIban("IL450100121234567890123"); // Israel (23 caractere)
        service.valideazaIban("BR1200000000000000000000000A1"); // Brazilia (29 caractere)
        service.valideazaIban("PK12HABB0000000123456789"); // Pakistan (24 caractere)
        service.valideazaIban("NP120000000001234567"); // Nepal (20 caractere)
        service.valideazaIban("IN12SBIN00012345678901"); // India (22 caractere)

        // 18. EAU gresit ca lungime (22 caractere in loc de 23)
        boolean aPicatAe = false;
        try {
            service.valideazaIban("AE26033123456789012345");
        } catch (FormatDateIncorectException ex) {
            aPicatAe = true;
        }
        afirma(aPicatAe, "Trebuia sa arunce exceptie pentru IBAN EAU cu lungime != 23");

        // 19. Validare America Latina extinsa (Mexic 22, Argentina 26, Uruguay 28, Chile 24, Columbia 24, Peru 24)
        service.valideazaIban("MX12000000000012345678"); // Mexic (22 caractere)
        service.valideazaIban("AR120000000000000012345678"); // Argentina (26 caractere)
        service.valideazaIban("UY12000100000000000012345678"); // Uruguay (28 caractere)
        service.valideazaIban("CL12BANC0000000012345678"); // Chile (24 caractere)
        service.valideazaIban("CO1200000000000012345678"); // Columbia (24 caractere)
        service.valideazaIban("PE1200000000000012345678"); // Peru (24 caractere)

        // 20. Validare Asia & Extremul Orient (China, Japonia, Coreea de Sud, Singapore, Bangladesh)
        service.valideazaIban("CN12000100100012345678"); // China (22 caractere)
        service.valideazaIban("JP120001001012345678"); // Japonia (20 caractere)
        service.valideazaIban("KR120001001012345678"); // Coreea de Sud (20 caractere)
        service.valideazaIban("SG120001001012345678"); // Singapore (20 caractere)
        service.valideazaIban("BD120001001012345678"); // Bangladesh (20 caractere)

        // 21. Mexic gresit ca lungime (21 in loc de 22)
        boolean aPicatMx = false;
        try {
            service.valideazaIban("MX1200000000001234567");
        } catch (FormatDateIncorectException ex) {
            aPicatMx = true;
        }
        afirma(aPicatMx, "Trebuia sa arunce exceptie pentru IBAN Mexic cu lungime != 22");

        // 22. Uruguay gresit ca lungime (27 in loc de 28)
        boolean aPicatUy = false;
        try {
            service.valideazaIban("UY1200010000000000001234567");
        } catch (FormatDateIncorectException ex) {
            aPicatUy = true;
        }
        afirma(aPicatUy, "Trebuia sa arunce exceptie pentru IBAN Uruguay cu lungime != 28");

        // 23. Validare Indonezia (ID) si verificare eroare lungime invalida
        service.valideazaIban("ID120001001012345678"); // Indonezia (20 caractere)
        boolean aPicatId = false;
        try {
            service.valideazaIban("ID123"); // Sub 15 caractere
        } catch (FormatDateIncorectException ex) {
            aPicatId = true;
        }
        afirma(aPicatId, "Trebuia sa arunce exceptie pentru IBAN Indonezia cu lungime sub limita minima");
    }

    private static void testDepunereRetragere() throws Exception {
        BancaService service = new BancaService();
        Client c = new Client("CLI-02", "Mihai Viteazu", "1910202123456", "mihai@istoria.ro", "0720999888");
        service.deschideCont("RO02BTRLRONCRT0000000002", c, TipCont.CURENT, Moneda.RON, 1000.0);

        service.efectueazaDepunere("RO02BTRLRONCRT0000000002", 500.0, "Test depunere");
        ContBancar cont = service.gasesteCont("RO02BTRLRONCRT0000000002");
        afirma(cont.getSold() == 1500.0, "Depunerea a calculat gresit soldul");

        // Retragere 200 lei, comision 1 leu
        service.efectueazaRetragere("RO02BTRLRONCRT0000000002", 200.0, "Test retragere");
        afirma(cont.getSold() == 1299.0, "Retragerea nu a scazut soldul si comisionul corect");
    }

    private static void testExceptieFonduriInsuficiente() throws Exception {
        BancaService service = new BancaService();
        Client c = new Client("CLI-03", "Test Client", "1930303123456", "test@test.ro", "0720111222");
        service.deschideCont("RO03BTRLRONCRT0000000003", c, TipCont.CURENT, Moneda.RON, 100.0);

        boolean aAruncat = false;
        try {
            service.efectueazaRetragere("RO03BTRLRONCRT0000000003", 500.0, "Retragere peste sold");
        } catch (FonduriInsuficienteException ex) {
            aAruncat = true;
            afirma(ex.getSoldDisponibil() == 100.0, "Sold disponibil incorect in exceptie");
        }
        afirma(aAruncat, "Nu a fost aruncata FonduriInsuficienteException");
    }

    private static void testTransferValutar() throws Exception {
        BancaService service = new BancaService();
        Client c1 = new Client("CLI-04", "Client Euro", "1940404123456", "c1@test.ro", "0720333444");
        Client c2 = new Client("CLI-05", "Client Ron", "1950505123456", "c2@test.ro", "0720555666");

        service.deschideCont("RO04BTRLEURCRT0000000004", c1, TipCont.CURENT, Moneda.EUR, 1000.0);
        service.deschideCont("RO05BTRLRONCRT0000000005", c2, TipCont.CURENT, Moneda.RON, 0.0);

        // Transfer 100 EUR in contul de RON.
        // Rata EUR = 4.975 => 100 EUR = 497.5 RON.
        // Comision sursa = max(0.5, 100 * 0.003) = 0.5 EUR.
        service.efectueazaTransfer("RO04BTRLEURCRT0000000004", "RO05BTRLRONCRT0000000005", 100.0, "Transfer EUR -> RON");

        ContBancar sursa = service.gasesteCont("RO04BTRLEURCRT0000000004");
        ContBancar dest = service.gasesteCont("RO05BTRLRONCRT0000000005");

        afirma(sursa.getSold() == 899.5, "Soldul sursei dupa transfer EUR e incorect: " + sursa.getSold());
        afirma(Math.abs(dest.getSold() - 497.5) < 0.001, "Conversia valutara prin masivul fundamental 1D e incorecta");
    }

    private static void testMatriceStatistica() throws Exception {
        BancaService service = new BancaService();
        Client c = new Client("CLI-06", "Client Matrice", "1960606123456", "mat@test.ro", "0720777888");
        service.deschideCont("RO06BTRLRONCRT0000000006", c, TipCont.CURENT, Moneda.RON, 0.0);

        service.efectueazaDepunere("RO06BTRLRONCRT0000000006", 1000.0, "Depunere matrice");
        service.efectueazaRetragere("RO06BTRLRONCRT0000000006", 200.0, "Retragere matrice");

        double[][] matrice = service.getMatriceStatisticaLunara();
        int lunaCurenta = java.time.LocalDate.now().getMonthValue() - 1;

        afirma(matrice[lunaCurenta][TipTranzactie.DEPUNERE.getIndexMatrice()] == 1000.0, "Matrice 2D depuneri eronata");
        afirma(matrice[lunaCurenta][TipTranzactie.RETRAGERE.getIndexMatrice()] == 200.0, "Matrice 2D retrageri eronata");
    }

    private static void testExceptiiCont() throws Exception {
        BancaService service = new BancaService();

        boolean inexistentAruncat = false;
        try {
            service.gasesteCont("RO99INEXISTENT0000000099");
        } catch (ContInexistentException ex) {
            inexistentAruncat = true;
        }
        afirma(inexistentAruncat, "ContInexistentException nu a fost aruncata");

        Client c = new Client("CLI-07", "Client Blocat", "1970707123456", "blocat@test.ro", "0720000111");
        service.deschideCont("RO07BTRLRONCRT0000000007", c, TipCont.CURENT, Moneda.RON, 500.0);
        service.blocheazaDeblocheazaCont("RO07BTRLRONCRT0000000007", false);

        boolean blocatAruncat = false;
        try {
            service.efectueazaDepunere("RO07BTRLRONCRT0000000007", 100.0, "Incercare depunere pe cont blocat");
        } catch (ContBlocatException ex) {
            blocatAruncat = true;
        }
        afirma(blocatAruncat, "ContBlocatException nu a fost aruncata pe cont inactiv");

        // Test OperatiuneAnulataException (ierarhie BancarException)
        OperatiuneAnulataException exAnulare = new OperatiuneAnulataException("Test anulare");
        afirma(exAnulare instanceof BancarException, "OperatiuneAnulataException trebuie sa fie derivata din BancarException");
        afirma(exAnulare.getMessage().equals("Test anulare"), "Mesajul exceptiei de anulare este incorect");

        // Test i18n si comutare bilingva (Romana cu diacritice / Engleza)
        I18n.setLimba(Limba.ROMANA);
        afirma(I18n.getLimba() == Limba.ROMANA, "Limba initiala trebuie sa fie Romana");
        afirma(I18n.t("MENU_TITLE").contains("SISTEM E-BANKING"), "Titlul in romana trebuie sa fie setat");
        I18n.setLimba(Limba.ENGLEZA);
        afirma(I18n.getLimba() == Limba.ENGLEZA, "Limba trebuie sa poata fi comutata pe Engleza");
        afirma(I18n.t("MENU_TITLE").contains("E-BANKING SYSTEM"), "Titlul in engleza trebuie sa fie tradus corect");
        I18n.setLimba(Limba.ROMANA);
    }

    private static void testPersistentaCsv() throws Exception {
        String testConturiPath = "data/test_conturi_temp.csv";
        String testTrxPath = "data/test_trx_temp.csv";

        CsvStorage storage = new CsvStorage();
        BancaService service1 = new BancaService();
        Client c = new Client("CLI-08", "Persistenta CSV", "1980808123456", "csv@test.ro", "0720444555");
        service1.deschideCont("RO08BTRLRONCRT0000000008", c, TipCont.ECONOMII, Moneda.RON, 1234.56);

        // Salvare
        storage.salveazaConturi(testConturiPath, service1.getToateConturile());
        storage.salveazaTranzactii(testTrxPath, service1.getToateTranzactiile());

        // Incarcare in alt service curat
        BancaService service2 = new BancaService();
        storage.incarcaConturi(testConturiPath, service2);
        storage.incarcaTranzactii(testTrxPath, service2);

        ContBancar incarcat = service2.gasesteCont("RO08BTRLRONCRT0000000008");
        afirma(incarcat != null, "Contul nu a fost restaurat din CSV");
        afirma(incarcat.getSold() == 1234.56, "Soldul restaurat din CSV este diferit");

        // Curatare fisiere temporare
        new File(testConturiPath).delete();
        new File(testTrxPath).delete();
    }

    private static void testPersistentaBinar() throws Exception {
        String testBinPath = "data/test_backup_temp.dat";

        BinaryStorage storage = new BinaryStorage();
        BancaService service1 = new BancaService();
        Client c = new Client("CLI-09", "Persistenta Binar", "1990909123456", "bin@test.ro", "0720666777");
        service1.deschideCont("RO09BTRLUSDDEP0000000009", c, TipCont.DEPOZIT, Moneda.USD, 7777.0);

        storage.salveazaBackupBinar(testBinPath, service1.getToateConturile(), service1.getToateTranzactiile());

        BancaService service2 = new BancaService();
        storage.restaureazaBackupBinar(testBinPath, service2);

        ContBancar incarcat = service2.gasesteCont("RO09BTRLUSDDEP0000000009");
        afirma(incarcat != null, "Contul nu a fost restaurat din fisierul binar");
        afirma(incarcat.getSold() == 7777.0, "Soldul din backup-ul binar este incorect");
        afirma(incarcat.getMoneda() == Moneda.USD, "Moneda din backup binar este incorecta");

        new File(testBinPath).delete();
    }

    private static void testGenerareRaport() throws Exception {
        String testRaportPath = "data/test_raport_temp.txt";
        BancaService service = new BancaService();
        Client c = new Client("CLI-10", "Raport Test", "1901010123456", "rap@test.ro", "0720888999");
        service.deschideCont("RO10BTRLRONCRT0000000010", c, TipCont.CURENT, Moneda.RON, 2000.0);

        RaportGenerator generator = new RaportGenerator();
        generator.genereazaRaportStatistici(testRaportPath, service);

        File f = new File(testRaportPath);
        afirma(f.exists() && f.length() > 500, "Fisierul de raport nu a fost generat corespunzator");
        f.delete();
    }

    private static void testAutentificarePinSiBlocareLaTreiIncercari() throws Exception {
        BancaService service = new BancaService();
        Client c = new Client("CLI-11", "Client Securitate", "1951111123456", "sec@test.ro", "0720111999");
        service.deschideCont("RO11BTRLRONCRT0000000011", c, TipCont.CURENT, Moneda.RON, 500.0, "9876");

        // 1. Autentificare reusita cu PIN corect
        ContBancar auth = service.autentificaClient("RO11BTRLRONCRT0000000011", "9876");
        afirma(auth != null, "Autentificarea cu PIN corect a esuat");
        afirma(auth.isActiv(), "Contul ar trebui sa fie activ");

        // 2. Incercare 1 PIN gresit -> arunca TranzactieInvalidaException
        boolean err1 = false;
        try {
            service.autentificaClient("RO11BTRLRONCRT0000000011", "0000");
        } catch (TranzactieInvalidaException ex) {
            err1 = true;
            afirma(auth.getIncercariRamase() == 2, "Trebuiau sa ramana 2 incercari");
        }
        afirma(err1, "Trebuia sa arunce TranzactieInvalidaException la PIN gresit 1");

        // 3. Incercare 2 PIN gresit
        boolean err2 = false;
        try {
            service.autentificaClient("RO11BTRLRONCRT0000000011", "0000");
        } catch (TranzactieInvalidaException ex) {
            err2 = true;
            afirma(auth.getIncercariRamase() == 1, "Trebuia sa ramana 1 incercare");
        }
        afirma(err2, "Trebuia sa arunce TranzactieInvalidaException la PIN gresit 2");

        // 4. Incercare 3 PIN gresit -> BLOCARE AUTOMATA a contului cu ContBlocatException
        boolean blocat = false;
        try {
            service.autentificaClient("RO11BTRLRONCRT0000000011", "0000");
        } catch (ContBlocatException ex) {
            blocat = true;
        }
        afirma(blocat, "Contul trebuia sa fie blocat automat cu ContBlocatException dupa 3 incercari");
        afirma(!auth.isActiv(), "Starea contului in memorie trebuia sa devina inactiv (blocat)");

        // 5. Deblocare cont de catre operator
        service.blocheazaDeblocheazaCont("RO11BTRLRONCRT0000000011", true);
        auth.resetIncercariPin();
        ContBancar reauth = service.autentificaClient("RO11BTRLRONCRT0000000011", "9876");
        afirma(reauth != null && reauth.isActiv(), "Contul deblocat nu se poate autentifica");
    }

    private static void testSecuritateParolaOperator() throws Exception {
        BancaService service = new BancaService();
        afirma(!service.valideazaParolaOperator("banca2026"), "Parola simpla/gresita a fost acceptata");
        afirma(!service.valideazaParolaOperator("admin"), "Parola 'admin' nu trebuie acceptata");
        afirma(service.valideazaParolaOperator("Op#Secur!ty_9874Banca@PPOO"), "Parola securizata de operator nu a fost validata corect");
    }

    private static void testGenerareIbanSiRestrictieDeschidere() throws Exception {
        BancaService service = new BancaService();
        Client c = new Client("CLI-12", "Client RO", "1951212123456", "ro@test.ro", "0720222333");

        // 1. Generare automata IBAN romanesc
        String ibanGenerat = service.genereazaIbanRomanesc(TipCont.CURENT, Moneda.RON);
        afirma(ibanGenerat != null && ibanGenerat.startsWith("RO49BTRL"), "Format IBAN generat incorect: " + ibanGenerat);
        afirma(ibanGenerat.length() == 24, "Lungimea IBAN-ului romanesc generat trebuie sa fie de 24 caractere");

        // 2. Restrictie: incercare deschidere cont cu IBAN strain (ex: Germania DE)
        boolean aPicatIbanStrain = false;
        try {
            service.deschideCont("DE89370400440532013000", c, TipCont.CURENT, Moneda.EUR, 100.0);
        } catch (FormatDateIncorectException ex) {
            aPicatIbanStrain = true;
        }
        afirma(aPicatIbanStrain, "Banca nu ar trebui sa permita deschiderea unui cont cu IBAN non-romanesc");
    }

    private static void testTransferInternationalContExtern() throws Exception {
        BancaService service = new BancaService();
        Client c = new Client("CLI-13", "Expeditor Transfer", "1951313123456", "exp@test.ro", "0720333444");
        service.deschideCont("RO13BTRLRONCRT0000000013", c, TipCont.CURENT, Moneda.RON, 1000.0);

        // Transfer 200 RON catre un cont extern din Germania (DE) conform ISO 13616
        // Comision extern 0.4% = 0.8 RON. Total debitat = 200.8 RON. Sold ramas = 799.2 RON.
        service.efectueazaTransfer("RO13BTRLRONCRT0000000013", "DE89370400440532013000", 200.0, "Plata serviciu Germania");

        ContBancar sursa = service.gasesteCont("RO13BTRLRONCRT0000000013");
        afirma(Math.abs(sursa.getSold() - 799.2) < 0.001, "Soldul sursei dupa transfer extern este incorect: " + sursa.getSold());

        // Verificam ca transferul extern a actualizat si matricea statistica 2D
        double[][] matrice = service.getMatriceStatisticaLunara();
        int lunaCurenta = java.time.LocalDate.now().getMonthValue() - 1;
        afirma(matrice[lunaCurenta][TipTranzactie.TRANSFER.getIndexMatrice()] >= 200.0, "Matricea statistica nu a inregistrat transferul extern");
    }

    private static void afirma(boolean conditie, String mesajEroare) {
        if (!conditie) {
            throw new AssertionError(mesajEroare);
        }
    }

    @FunctionalInterface
    private interface RunnableTest {
        void run() throws Exception;
    }
}
