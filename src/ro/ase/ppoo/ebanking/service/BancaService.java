package ro.ase.ppoo.ebanking.service;

import ro.ase.ppoo.ebanking.exception.*;
import ro.ase.ppoo.ebanking.model.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Serviciul principal de gestiune a operatiunilor bancare in memorie.
 * 
 * Implementeaza cerintele academice PPOO:
 * - 2 masive fundamentale (1D: double[] rate de schimb, 2D: double[][] volume lunare tranzactionate)
 * - 3 colectii de obiecte (Map<String, ContBancar>, List<Tranzactie>, Set<Client>)
 * - Toate modificarile se realizeaza exclusiv in memorie pe parcursul sesiunii.
 */
public class BancaService {

    // =========================================================================
    // 1. MASIVE DE TIPURI DE DATE FUNDAMENTALE (Cerinta 3 din barem)
    // =========================================================================

    /**
     * MASIV 1D FUNDAMENTAL: Rate de schimb valutar raportate la 1 RON (moneda de referinta).
     * Indexat prin ordinalul din enum Moneda:
     * 0 -> RON (1.0)
     * 1 -> EUR (4.975)
     * 2 -> USD (4.650)
     * 3 -> GBP (5.850)
     */
    private final double[] rateSchimbValutar;

    /**
     * MASIV 2D FUNDAMENTAL: Matrice statistica a sumelor totale tranzactionate (in RON).
     * Dimensiuni: 12 randuri (luni 0..11) x 4 coloane (tipuri tranzactie: DEPUNERE=0, RETRAGERE=1, TRANSFER=2, PLATA_FACTURA=3).
     */
    private final double[][] matriceStatisticaLunara;

    /**
     * Masiv 2D fundamental suplimentar: Numarul de tranzactii pe luna si tip de tranzactie.
     */
    private final int[][] matriceNumarTranzactii;

    // =========================================================================
    // 2. COLECTII DIFERITE DE OBIECTE (Cerinta 3 din barem)
    // =========================================================================

    /** Colectia 1: Harta conturilor bancare indexate unic dupa IBAN pentru acces O(1) */
    private final Map<String, ContBancar> conturiMap;

    /** Colectia 2: Lista cronologica a tuturor tranzactiilor inregistrate */
    private final List<Tranzactie> tranzactiiList;

    /** Colectia 3: Set pentru asigurarea unicitatii clientilor bancii dupa CNP */
    private final Set<Client> clientiSet;

    private long contorTranzactii = 1000;

    public BancaService() {
        // Initializare masiv 1D
        this.rateSchimbValutar = new double[] {
                1.0000, // RON
                4.9750, // EUR
                4.6500, // USD
                5.8500  // GBP
        };

        // Initializare masive 2D (12 luni x 4 tipuri tranzactie)
        this.matriceStatisticaLunara = new double[12][4];
        this.matriceNumarTranzactii = new int[12][4];

        // Initializare colectii
        this.conturiMap = new LinkedHashMap<>();
        this.tranzactiiList = new ArrayList<>();
        this.clientiSet = new HashSet<>();
    }

    // =========================================================================
    // METODE ASUPRA MASIVELOR FUNDAMENTALE
    // =========================================================================

    /**
     * Realizeaza conversia valutara a unei sume intre doua monede,
     * utilizand vectorul unidimensional fundamental de rate de schimb.
     *
     * @param suma suma de convertit
     * @param sursa moneda initiala
     * @param destinatie moneda dorita
     * @return suma echivalenta in moneda destinatie
     */
    public double converteste(double suma, Moneda sursa, Moneda destinatie) {
        if (sursa == destinatie) {
            return suma;
        }
        // Conversie prin intermediul monedei de baza (RON) folosind vectorul fundamental
        double rataSursaInRon = rateSchimbValutar[sursa.ordinal()];
        double rataDestinatieInRon = rateSchimbValutar[destinatie.ordinal()];

        double sumaInRon = suma * rataSursaInRon;
        return sumaInRon / rataDestinatieInRon;
    }

    /**
     * Actualizeaza matricea bidimensionala fundamentala cu noua tranzactie inregistrata.
     * @param tranzactie tranzactia efectuata
     */
    private void actualizeazaMatriceStatistica(Tranzactie tranzactie) {
        if (tranzactie.getStatus() != StatusTranzactie.FINALIZATA) {
            return;
        }
        int luna = tranzactie.getDataOra().getMonthValue() - 1; // 0..11
        int tipIdx = tranzactie.getTip().getIndexMatrice();    // 0..3

        // Conversie in RON pentru agregare uniforma in matricea statistica
        double valoareRon = converteste(tranzactie.getSuma(), tranzactie.getMoneda(), Moneda.RON);

        matriceStatisticaLunara[luna][tipIdx] += valoareRon;
        matriceNumarTranzactii[luna][tipIdx]++;
    }

    /**
     * Recalculeaza valorile din matricea bidimensionala pe baza tranzactiilor existente in memorie.
     */
    public void recalculeazaMatriciStatistice() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 4; j++) {
                matriceStatisticaLunara[i][j] = 0.0;
                matriceNumarTranzactii[i][j] = 0;
            }
        }
        for (Tranzactie t : tranzactiiList) {
            actualizeazaMatriceStatistica(t);
        }
    }

    public double[][] getMatriceStatisticaLunara() {
        return matriceStatisticaLunara;
    }

    public int[][] getMatriceNumarTranzactii() {
        return matriceNumarTranzactii;
    }

    public double[] getRateSchimbValutar() {
        return rateSchimbValutar;
    }

    // =========================================================================
    // METODE GESTIUNE CONTURI SI CLIENTI
    // =========================================================================

    public void adaugaClient(Client client) {
        if (client != null) {
            clientiSet.add(client);
        }
    }

    public void adaugaContInMemorie(ContBancar cont) {
        if (cont != null) {
            conturiMap.put(cont.getIban(), cont);
            if (cont.getTitular() != null) {
                clientiSet.add(cont.getTitular());
            }
        }
    }

    /**
     * Inregistreaza o tranzactie citita din fisier de persistenta sau backup.
     * @param tranzactie tranzactia de adaugat
     */
    public void incarcaTranzactieDinFisier(Tranzactie tranzactie) {
        if (tranzactie != null) {
            tranzactiiList.add(tranzactie);
            actualizeazaMatriceStatistica(tranzactie);
        }
    }

    /**
     * Reseteaza toate datele din memorie (utilizata la restaurarea unui backup).
     */
    public void golesteDateleDinMemorie() {
        conturiMap.clear();
        tranzactiiList.clear();
        clientiSet.clear();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 4; j++) {
                matriceStatisticaLunara[i][j] = 0.0;
                matriceNumarTranzactii[i][j] = 0;
            }
        }
    }

    public void deschideCont(String iban, Client client, TipCont tipCont, Moneda moneda, double soldInitial)
            throws TranzactieInvalidaException, FormatDateIncorectException {

        valideazaIban(iban);
        valideazaClient(client);

        if (conturiMap.containsKey(iban)) {
            throw new TranzactieInvalidaException("Exista deja un cont bancar inregistrat cu IBAN-ul: " + iban);
        }
        if (soldInitial < 0) {
            throw new TranzactieInvalidaException("Soldul initial nu poate fi negativ.");
        }

        ContBancar contNou = new ContBancar(iban, client, tipCont, moneda, soldInitial);
        conturiMap.put(iban, contNou);
        clientiSet.add(client);

        if (soldInitial > 0) {
            String idTrx = genereazaIdTranzactie();
            Tranzactie t = new Tranzactie(idTrx, LocalDateTime.now(), iban, iban,
                    TipTranzactie.DEPUNERE, soldInitial, moneda, 0.0,
                    StatusTranzactie.FINALIZATA, "Depunere sold initial la deschidere cont");
            tranzactiiList.add(t);
            actualizeazaMatriceStatistica(t);
        }
    }

    public ContBancar gasesteCont(String iban) throws ContInexistentException {
        ContBancar cont = conturiMap.get(iban);
        if (cont == null) {
            throw new ContInexistentException(iban);
        }
        return cont;
    }

    public Collection<ContBancar> getToateConturile() {
        return Collections.unmodifiableCollection(conturiMap.values());
    }

    public Set<Client> getTotiClientii() {
        return Collections.unmodifiableSet(clientiSet);
    }

    public List<Tranzactie> getToateTranzactiile() {
        return Collections.unmodifiableList(tranzactiiList);
    }

    public void blocheazaDeblocheazaCont(String iban, boolean stareActiva) throws ContInexistentException {
        ContBancar cont = gasesteCont(iban);
        cont.setActiv(stareActiva);
    }

    // =========================================================================
    // TRANZACTII FINANCIARE IN MEMORIE
    // =========================================================================

    public Tranzactie efectueazaDepunere(String iban, double suma, String descriere)
            throws BancarException {

        if (suma <= 0) {
            throw new TranzactieInvalidaException("Suma de depus trebuie sa fie strict pozitiva.");
        }

        ContBancar cont = gasesteCont(iban);
        if (!cont.isActiv()) {
            throw new ContBlocatException(iban);
        }

        cont.depune(suma);

        String idTrx = genereazaIdTranzactie();
        Tranzactie trx = new Tranzactie(idTrx, LocalDateTime.now(), iban, iban,
                TipTranzactie.DEPUNERE, suma, cont.getMoneda(), 0.0,
                StatusTranzactie.FINALIZATA, descriere != null && !descriere.isBlank() ? descriere : "Depunere numerar");

        tranzactiiList.add(trx);
        actualizeazaMatriceStatistica(trx);
        return trx;
    }

    public Tranzactie efectueazaRetragere(String iban, double suma, String descriere)
            throws BancarException {

        if (suma <= 0) {
            throw new TranzactieInvalidaException("Suma de retras trebuie sa fie strict pozitiva.");
        }

        ContBancar cont = gasesteCont(iban);
        if (!cont.isActiv()) {
            throw new ContBlocatException(iban);
        }

        // Comision retragere 0.2%, minim 1 unitate monetara
        double comision = Math.max(1.0, suma * 0.002);
        double totalNecesar = suma + comision;

        if (cont.getSold() < totalNecesar) {
            throw new FonduriInsuficienteException(iban, totalNecesar, cont.getSold());
        }

        cont.retrage(totalNecesar);

        String idTrx = genereazaIdTranzactie();
        Tranzactie trx = new Tranzactie(idTrx, LocalDateTime.now(), iban, iban,
                TipTranzactie.RETRAGERE, suma, cont.getMoneda(), comision,
                StatusTranzactie.FINALIZATA, descriere != null && !descriere.isBlank() ? descriere : "Retragere numerar");

        tranzactiiList.add(trx);
        actualizeazaMatriceStatistica(trx);
        return trx;
    }

    public Tranzactie efectueazaTransfer(String ibanSursa, String ibanDestinatie, double suma, String descriere)
            throws BancarException {

        if (suma <= 0) {
            throw new TranzactieInvalidaException("Suma de transferat trebuie sa fie strict pozitiva.");
        }

        if (ibanSursa.equalsIgnoreCase(ibanDestinatie)) {
            throw new TranzactieInvalidaException("Contul sursa si contul destinatie nu pot fi identice pentru transfer bancar.");
        }

        ContBancar contSursa = gasesteCont(ibanSursa);
        ContBancar contDestinatie = gasesteCont(ibanDestinatie);

        if (!contSursa.isActiv()) {
            throw new ContBlocatException(ibanSursa);
        }
        if (!contDestinatie.isActiv()) {
            throw new ContBlocatException(ibanDestinatie);
        }

        // Comision transfer bancar: 0.1% pentru aceeasi moneda, 0.3% pentru conversie valutara
        boolean necesitaConversie = contSursa.getMoneda() != contDestinatie.getMoneda();
        double procentComision = necesitaConversie ? 0.003 : 0.001;
        double comision = Math.max(0.5, suma * procentComision);
        double totalNecesarSursa = suma + comision;

        if (contSursa.getSold() < totalNecesarSursa) {
            throw new FonduriInsuficienteException(ibanSursa, totalNecesarSursa, contSursa.getSold());
        }

        // Calcul suma creditata in contul destinatie (utilizand masivul fundamental de rate)
        double sumaDestinatie = suma;
        if (necesitaConversie) {
            sumaDestinatie = converteste(suma, contSursa.getMoneda(), contDestinatie.getMoneda());
        }

        // Operare in memorie
        contSursa.retrage(totalNecesarSursa);
        contDestinatie.depune(sumaDestinatie);

        String descriereFinala = (descriere != null && !descriere.isBlank()) ? descriere : "Transfer bancar interconturi";
        if (necesitaConversie) {
            descriereFinala += String.format(" (Conversie: %.2f %s -> %.2f %s)",
                    suma, contSursa.getMoneda(), sumaDestinatie, contDestinatie.getMoneda());
        }

        String idTrx = genereazaIdTranzactie();
        Tranzactie trx = new Tranzactie(idTrx, LocalDateTime.now(), ibanSursa, ibanDestinatie,
                TipTranzactie.TRANSFER, suma, contSursa.getMoneda(), comision,
                StatusTranzactie.FINALIZATA, descriereFinala);

        tranzactiiList.add(trx);
        actualizeazaMatriceStatistica(trx);
        return trx;
    }

    public Tranzactie efectueazaPlataFactura(String ibanSursa, String denumireFurnizor, double suma, String numarFactura)
            throws BancarException {

        if (suma <= 0) {
            throw new TranzactieInvalidaException("Suma facturii trebuie sa fie strict pozitiva.");
        }

        ContBancar cont = gasesteCont(ibanSursa);
        if (!cont.isActiv()) {
            throw new ContBlocatException(ibanSursa);
        }

        if (cont.getSold() < suma) {
            throw new FonduriInsuficienteException(ibanSursa, suma, cont.getSold());
        }

        cont.retrage(suma);

        String descriere = "Plata factura utilitati: " + denumireFurnizor + " (Factura #" + numarFactura + ")";
        String idTrx = genereazaIdTranzactie();
        Tranzactie trx = new Tranzactie(idTrx, LocalDateTime.now(), ibanSursa, "TREZORERIE-" + denumireFurnizor.toUpperCase().replaceAll("\\s+", "-"),
                TipTranzactie.PLATA_FACTURA, suma, cont.getMoneda(), 0.0,
                StatusTranzactie.FINALIZATA, descriere);

        tranzactiiList.add(trx);
        actualizeazaMatriceStatistica(trx);
        return trx;
    }

    public List<Tranzactie> filtreazaTranzactiiCont(String iban) {
        List<Tranzactie> rezultat = new ArrayList<>();
        for (Tranzactie t : tranzactiiList) {
            if (iban.equalsIgnoreCase(t.getIbanSursa()) || iban.equalsIgnoreCase(t.getIbanDestinatie())) {
                rezultat.add(t);
            }
        }
        Collections.sort(rezultat);
        return rezultat;
    }

    public double calculeazaTotalFonduri(Moneda monedaReferinta) {
        double total = 0.0;
        for (ContBancar cont : conturiMap.values()) {
            total += converteste(cont.getSold(), cont.getMoneda(), monedaReferinta);
        }
        return total;
    }

    // =========================================================================
    // VALIDARI DE DATE (Cerinta 5 din barem)
    // =========================================================================

    public void valideazaIban(String iban) throws FormatDateIncorectException {
        if (iban == null || iban.trim().length() < 16) {
            throw new FormatDateIncorectException("IBAN-ul este invalid. Lungimea minima trebuie sa fie de cel putin 16 caractere.");
        }
        if (!iban.matches("^[A-Z]{2}[0-9A-Z]+$")) {
            throw new FormatDateIncorectException("Formatul IBAN-ului este incorect (trebuie sa inceapa cu 2 litere urmate de cifre/litere).");
        }
    }

    public void valideazaClient(Client client) throws FormatDateIncorectException {
        if (client == null) {
            throw new FormatDateIncorectException("Datele clientului lipsesc.");
        }
        if (client.getNume() == null || client.getNume().trim().length() < 3) {
            throw new FormatDateIncorectException("Numele clientului trebuie sa contina minim 3 caractere.");
        }
        if (client.getCnp() == null || !client.getCnp().matches("^\\d{13}$")) {
            throw new FormatDateIncorectException("CNP invalid. Trebuie sa contina exact 13 cifre numerice.");
        }
        if (client.getEmail() == null || !client.getEmail().contains("@") || !client.getEmail().contains(".")) {
            throw new FormatDateIncorectException("Adresa de email este invalida.");
        }
        if (client.getTelefon() == null || client.getTelefon().trim().length() < 10) {
            throw new FormatDateIncorectException("Numarul de telefon este invalid (minim 10 caractere).");
        }
    }

    private synchronized String genereazaIdTranzactie() {
        contorTranzactii++;
        return "TRX-" + contorTranzactii;
    }

    public void seteazaContorTranzactii(long maxId) {
        if (maxId > contorTranzactii) {
            this.contorTranzactii = maxId;
        }
    }
}
