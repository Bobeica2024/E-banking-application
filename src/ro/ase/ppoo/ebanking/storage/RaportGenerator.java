package ro.ase.ppoo.ebanking.storage;

import ro.ase.ppoo.ebanking.model.ContBancar;
import ro.ase.ppoo.ebanking.model.Moneda;
import ro.ase.ppoo.ebanking.model.TipTranzactie;
import ro.ase.ppoo.ebanking.model.Tranzactie;
import ro.ase.ppoo.ebanking.service.BancaService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Generator de rapoarte si statistici financiare in fisier text.
 * Utilizeaza masivul fundamental 2D si 1D din BancaService pentru analize.
 */
public class RaportGenerator {

    /**
     * Genereaza fisierul text cu raportul statistic complet al bancii.
     *
     * @param caleFisier cale de iesire (ex: data/raport_statistici.txt)
     * @param service serviciul de banking
     * @throws IOException in caz de eroare la scriere
     */
    public void genereazaRaportStatistici(String caleFisier, BancaService service) throws IOException {
        File fisier = new File(caleFisier);
        if (fisier.getParentFile() != null) {
            fisier.getParentFile().mkdirs();
        }

        double[][] matrice = service.getMatriceStatisticaLunara();
        int[][] matriceNr = service.getMatriceNumarTranzactii();
        double[] cursuri = service.getRateSchimbValutar();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fisier), StandardCharsets.UTF_8))) {
            writer.write("=========================================================================================\n");
            writer.write("                  RAPORT STATISTIC FINANCIAR - SISTEM E-BANKING PPOO                     \n");
            writer.write("                      Data generarii: " + LocalDateTime.now().format(Tranzactie.FORMATTER) + "\n");
            writer.write("=========================================================================================\n\n");

            // 1. Sinteza solduri si clienti
            Collection<ContBancar> conturi = service.getToateConturile();
            writer.write("1. SINTEZA GENERALA CONTURI SI LICHIDITATI:\n");
            writer.write("-----------------------------------------------------------------------------------------\n");
            writer.write(String.format(" - Numar total de conturi active: %d\n", conturi.size()));
            writer.write(String.format(" - Numar clienti unici: %d\n", service.getTotiClientii().size()));
            writer.write(String.format(" - Total fonduri gestionate (in RON): %,12.2f lei\n", service.calculeazaTotalFonduri(Moneda.RON)));
            writer.write(String.format(" - Total fonduri gestionate (in EUR): %,12.2f €\n", service.calculeazaTotalFonduri(Moneda.EUR)));
            writer.write(String.format(" - Total fonduri gestionate (in USD): %,12.2f $\n", service.calculeazaTotalFonduri(Moneda.USD)));
            writer.write(String.format(" - Total fonduri gestionate (in GBP): %,12.2f £\n", service.calculeazaTotalFonduri(Moneda.GBP)));
            writer.write("\n");

            // 2. Rate de schimb utilizate (Masiv 1D fundamental)
            writer.write("2. RATE DE SCHIMB VALUTAR DE REFERINTA (Vector fundamental double[]):\n");
            writer.write("-----------------------------------------------------------------------------------------\n");
            for (Moneda m : Moneda.values()) {
                writer.write(String.format(" - 1 %-3s = %7.4f RON (%s)\n", m.name(), cursuri[m.ordinal()], m.getDenumire()));
            }
            writer.write("\n");

            // 3. Matricea bidimensionala a volumelor tranzactionate (Masiv 2D fundamental)
            writer.write("3. ACTIVITATE TRANZACTIONALA LUNARA (Matrice fundamentala double[12][4] - echiv. RON):\n");
            writer.write("-----------------------------------------------------------------------------------------\n");
            writer.write(String.format("%-12s | %-15s | %-15s | %-15s | %-15s | %-15s\n",
                    "Luna", "DEPUNERI", "RETRAGERI", "TRANSFERURI", "FACTURI", "TOTAL LUNA"));
            writer.write("-----------------------------------------------------------------------------------------\n");

            double totalDepuneri = 0, totalRetrageri = 0, totalTransferuri = 0, totalFacturi = 0;

            for (int luna = 0; luna < 12; luna++) {
                String numeLuna = Month.of(luna + 1).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ro-RO"));
                numeLuna = numeLuna.substring(0, 1).toUpperCase() + numeLuna.substring(1);

                double dep = matrice[luna][TipTranzactie.DEPUNERE.getIndexMatrice()];
                double ret = matrice[luna][TipTranzactie.RETRAGERE.getIndexMatrice()];
                double trf = matrice[luna][TipTranzactie.TRANSFER.getIndexMatrice()];
                double fac = matrice[luna][TipTranzactie.PLATA_FACTURA.getIndexMatrice()];
                double totalLuna = dep + ret + trf + fac;

                totalDepuneri += dep;
                totalRetrageri += ret;
                totalTransferuri += trf;
                totalFacturi += fac;

                writer.write(String.format("%-12s | %,12.2f lei | %,12.2f lei | %,12.2f lei | %,12.2f lei | %,12.2f lei\n",
                        numeLuna, dep, ret, trf, fac, totalLuna));
            }
            writer.write("-----------------------------------------------------------------------------------------\n");
            double totalGeneral = totalDepuneri + totalRetrageri + totalTransferuri + totalFacturi;
            writer.write(String.format("%-12s | %,12.2f lei | %,12.2f lei | %,12.2f lei | %,12.2f lei | %,12.2f lei\n\n",
                    "TOTAL ANUAL", totalDepuneri, totalRetrageri, totalTransferuri, totalFacturi, totalGeneral));

            // 4. Distributia numarului de tranzactii din matricea secundara int[12][4]
            int nrTotalTranzactii = 0;
            for (int l = 0; l < 12; l++) {
                for (int c = 0; c < 4; c++) {
                    nrTotalTranzactii += matriceNr[l][c];
                }
            }
            writer.write(String.format("4. NUMAR TOTAL DE TRANZACTII AUDITATE: %d operatiuni\n\n", nrTotalTranzactii));

            // 5. Top 5 cele mai mari tranzactii din istoric
            writer.write("5. TOP 5 TRANZACTII CA VOLUM:\n");
            writer.write("-----------------------------------------------------------------------------------------\n");
            List<Tranzactie> tranzactiiCopie = new ArrayList<>(service.getToateTranzactiile());
            tranzactiiCopie.sort((t1, t2) -> Double.compare(
                    service.converteste(t2.getSuma(), t2.getMoneda(), Moneda.RON),
                    service.converteste(t1.getSuma(), t1.getMoneda(), Moneda.RON)));

            int topCount = Math.min(5, tranzactiiCopie.size());
            for (int i = 0; i < topCount; i++) {
                Tranzactie t = tranzactiiCopie.get(i);
                double inRon = service.converteste(t.getSuma(), t.getMoneda(), Moneda.RON);
                writer.write(String.format(" %d. [%s] %s | %,10.2f %s (~%,10.2f RON) | %s -> %s | %s\n",
                        (i + 1),
                        t.getIdTranzactie(),
                        t.getTip().name(),
                        t.getSuma(),
                        t.getMoneda().name(),
                        inRon,
                        t.getIbanSursa(),
                        t.getIbanDestinatie(),
                        t.getDescriere()));
            }
            writer.write("\n=========================================================================================\n");
            writer.write("                              SFARSIT RAPORT STATISTIC                                   \n");
            writer.write("=========================================================================================\n");
        }
    }
}
