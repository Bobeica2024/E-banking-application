package ro.ase.ppoo.ebanking.storage;

import ro.ase.ppoo.ebanking.model.ContBancar;
import ro.ase.ppoo.ebanking.model.Tranzactie;
import ro.ase.ppoo.ebanking.service.BancaService;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Clasa responsabila cu persistența și restaurarea datelor in format binar.
 * Foloseste fluxuri de obiecte (ObjectOutputStream / ObjectInputStream)
 * pentru salvarea securizata a snapshot-urilor de audit.
 */
public class BinaryStorage {

    /**
     * Salveaza starea completa a bancii (conturi si tranzactii) intr-un fisier binar.
     *
     * @param caleFisier cale catre fisierul binar
     * @param conturi colectia de conturi bancare
     * @param tranzactii lista de tranzactii
     * @throws IOException in caz de erori I/O
     */
    public void salveazaBackupBinar(String caleFisier, Collection<ContBancar> conturi, List<Tranzactie> tranzactii) throws IOException {
        File fisier = new File(caleFisier);
        if (fisier.getParentFile() != null) {
            fisier.getParentFile().mkdirs();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fisier))) {
            // Salvam ca liste serializabile
            out.writeObject(new ArrayList<>(conturi));
            out.writeObject(new ArrayList<>(tranzactii));
            out.flush();
        }
    }

    /**
     * Restaureaza starea bancii dintr-un fisier binar existent in memorie.
     *
     * @param caleFisier cale catre fisierul binar
     * @param service instanta serviciului bancar
     * @throws IOException eroare I/O
     * @throws ClassNotFoundException eroare la deserializare
     */
    @SuppressWarnings("unchecked")
    public void restaureazaBackupBinar(String caleFisier, BancaService service) throws IOException, ClassNotFoundException {
        File fisier = new File(caleFisier);
        if (!fisier.exists()) {
            throw new FileNotFoundException("Fișierul de backup binar nu există: " + caleFisier);
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fisier))) {
            List<ContBancar> conturiRestaurate = (List<ContBancar>) in.readObject();
            List<Tranzactie> tranzactiiRestaurate = (List<Tranzactie>) in.readObject();

            service.golesteDateleDinMemorie();

            for (ContBancar c : conturiRestaurate) {
                service.adaugaContInMemorie(c);
            }
            for (Tranzactie t : tranzactiiRestaurate) {
                service.incarcaTranzactieDinFisier(t);
            }
        }
    }
}
