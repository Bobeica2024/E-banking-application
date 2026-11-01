package ro.ase.ppoo.ebanking;

import ro.ase.ppoo.ebanking.exception.FormatDateIncorectException;
import ro.ase.ppoo.ebanking.service.BancaService;
import ro.ase.ppoo.ebanking.storage.BinaryStorage;
import ro.ase.ppoo.ebanking.storage.CsvStorage;
import ro.ase.ppoo.ebanking.storage.RaportGenerator;
import ro.ase.ppoo.ebanking.ui.MeniuConsola;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Punctul principal de intrare (entry-point) in aplicatia de e-banking.
 * 
 * Implementeaza cerintele PPOO:
 * - Incarcarea automata a datelor la pornire din fisiere
 * - Gestiunea ciclului de viata in memorie
 * - Asigurarea persistentei la inchidere
 */
public class Main {

    private static final String DATA_DIR = "data";
    private static final String FISIER_CONTURI = DATA_DIR + File.separator + "conturi.csv";
    private static final String FISIER_TRANZACTII = DATA_DIR + File.separator + "tranzactii.csv";
    private static final String FISIER_BACKUP = DATA_DIR + File.separator + "backup_audit.dat";
    private static final String FISIER_RAPORT = DATA_DIR + File.separator + "raport_statistici.txt";

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        afiseazaBannerPornire();

        BancaService bancaService = new BancaService();
        CsvStorage csvStorage = new CsvStorage();
        BinaryStorage binaryStorage = new BinaryStorage();
        RaportGenerator raportGenerator = new RaportGenerator();

        // 1. Incarcare date initiale din fisiere (Cerinta 1, 2)
        try {
            System.out.println("[INIT] Se încarcă datele conturilor din '" + FISIER_CONTURI + "'...");
            csvStorage.incarcaConturi(FISIER_CONTURI, bancaService);
            System.out.printf("[INIT] Încărcare finalizată: %d conturi încărcate în memorie.\n", bancaService.getToateConturile().size());

            System.out.println("[INIT] Se încarcă istoricul tranzacțiilor din '" + FISIER_TRANZACTII + "'...");
            csvStorage.incarcaTranzactii(FISIER_TRANZACTII, bancaService);
            System.out.printf("[INIT] Încărcare finalizată: %d tranzacții încărcate în memorie.\n", bancaService.getToateTranzactiile().size());

            bancaService.recalculeazaMatriciStatistice();
            System.out.println("[INIT] Matricea statistică fundamentală a fost inițializată cu succes.");
        } catch (IOException | FormatDateIncorectException e) {
            System.err.println("[AVERTISMENT LA INIȚIALIZARE] " + e.getMessage());
            System.out.println("[INIT] Se continuă pornirea aplicației cu starea existentă.");
        }

        // 2. Inregistrare Shutdown Hook pentru salvare automata garantata in caz de oprire fortata (Cerinta 2, 4)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("\n[SHUTDOWN-HOOK] Asigurare persistență date la închidere...");
                csvStorage.salveazaConturi(FISIER_CONTURI, bancaService.getToateConturile());
                csvStorage.salveazaTranzactii(FISIER_TRANZACTII, bancaService.getToateTranzactiile());
                System.out.println("[SHUTDOWN-HOOK] Datele au fost salvate pe disc.");
            } catch (Exception e) {
                System.err.println("[SHUTDOWN-HOOK EROARE] " + e.getMessage());
            }
        }));

        // 3. Lansare meniu interactiv pentru utilizator (Cerinta 6, 7)
        MeniuConsola meniu = new MeniuConsola(bancaService, csvStorage, binaryStorage, raportGenerator,
                FISIER_CONTURI, FISIER_TRANZACTII, FISIER_BACKUP, FISIER_RAPORT);

        meniu.porneste();
    }

    private static void afiseazaBannerPornire() {
        System.out.println("**************************************************************************");
        System.out.println("*              PROIECT PPOO - SISTEM E-BANKING JAVA (MASTERAT)           *");
        System.out.println("*                  Tema 1: Gestiunea Tranzacțiilor Bancare               *");
        System.out.println("*              Persistență Fișiere | Matrice & Colecții | Validări       *");
        System.out.println("**************************************************************************\n");
    }
}
