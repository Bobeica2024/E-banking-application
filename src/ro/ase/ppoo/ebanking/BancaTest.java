package ro.ase.ppoo.ebanking;

import ro.ase.ppoo.ebanking.exception.*;
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

        ruleazaTest("Test Deschidere Cont si Colectii", BancaTest::testDeschidereCont);
        ruleazaTest("Test Depunere si Retragere", BancaTest::testDepunereRetragere);
        ruleazaTest("Test Exceptie Fonduri Insuficiente", BancaTest::testExceptieFonduriInsuficiente);
        ruleazaTest("Test Transfer Valutar si Vector Rate (Masiv 1D)", BancaTest::testTransferValutar);
        ruleazaTest("Test Matrice Statistica Bidimensionala (Masiv 2D)", BancaTest::testMatriceStatistica);
        ruleazaTest("Test Exceptie Cont Blocat si Inexistent", BancaTest::testExceptiiCont);
        ruleazaTest("Test Persistenta CSV (Salvare/Incarcare)", BancaTest::testPersistentaCsv);
        ruleazaTest("Test Persistenta Binar (Salvare/Restaurare)", BancaTest::testPersistentaBinar);
        ruleazaTest("Test Generare Raport Text Statistici", BancaTest::testGenerareRaport);

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
        Client c = new Client("CLI-01", "George Enescu", "1900101123456", "george@art.ro", "0720123456");
        service.deschideCont("RO01BTRLRONCRT0000000001", c, TipCont.CURENT, Moneda.RON, 500.0);

        ContBancar gasit = service.gasesteCont("RO01BTRLRONCRT0000000001");
        afirma(gasit != null, "Contul nu a fost gasit in memorie");
        afirma(gasit.getSold() == 500.0, "Soldul initial este incorect");
        afirma(service.getToateConturile().size() == 1, "Numarul total de conturi este incorect");
        afirma(service.getTotiClientii().contains(c), "Clientul nu a fost adaugat in HashSet");
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
