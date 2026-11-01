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
 * - 3 colectii de obiecte (Map de ContBancar, List de Tranzactie, Set de Client)
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

    /**
     * Specificatie de validare pentru formatul IBAN al unei tari conform registrului oficial SWIFT / ISO 13616.
     */
    public record RegulaIban(String denumireTara, int lungimeFixa, String regex, String formatAsteptat) {}

    private static final Map<String, RegulaIban> REGULI_IBAN_TARI = new HashMap<>();

    static {
        // =====================================================================
        // TOATE CELE 27 DE STATE MEMBRE ALE UNIUNII EUROPENE (UE)
        // =====================================================================
        REGULI_IBAN_TARI.put("RO", new RegulaIban("Romania", 24, "^RO[0-9]{2}[A-Z]{4}[A-Z0-9]{16}$", "RO + 2 cifre control + 4 litere cod banca + 16 caractere cont"));
        REGULI_IBAN_TARI.put("AT", new RegulaIban("Austria", 20, "^AT[0-9]{18}$", "AT + 2 cifre + 5 cifre cod banca + 11 cifre cont"));
        REGULI_IBAN_TARI.put("BE", new RegulaIban("Belgia", 16, "^BE[0-9]{14}$", "BE + 2 cifre + 12 cifre cont"));
        REGULI_IBAN_TARI.put("BG", new RegulaIban("Bulgaria", 22, "^BG[0-9]{2}[A-Z]{4}[0-9]{4}[A-Z0-9]{10}$", "BG + 2 cifre + 4 litere banca + 4 cifre + 10 alfanumerice"));
        REGULI_IBAN_TARI.put("CY", new RegulaIban("Cipru", 28, "^CY[0-9]{10}[A-Z0-9]{16}$", "CY + 2 cifre + 8 cifre banca/sucursala + 16 alfanumerice"));
        REGULI_IBAN_TARI.put("CZ", new RegulaIban("Cehia", 24, "^CZ[0-9]{22}$", "CZ + 2 cifre + 20 cifre cont"));
        REGULI_IBAN_TARI.put("DE", new RegulaIban("Germania", 22, "^DE[0-9]{20}$", "DE + 2 cifre control + 8 cifre BLZ + 10 cifre cont (exclusiv cifre)"));
        REGULI_IBAN_TARI.put("DK", new RegulaIban("Danemarca", 18, "^DK[0-9]{16}$", "DK + 2 cifre control + 14 cifre cont"));
        REGULI_IBAN_TARI.put("EE", new RegulaIban("Estonia", 20, "^EE[0-9]{18}$", "EE + 2 cifre + 16 cifre bancare"));
        REGULI_IBAN_TARI.put("ES", new RegulaIban("Spania", 24, "^ES[0-9]{22}$", "ES + 2 cifre + 20 cifre bancare"));
        REGULI_IBAN_TARI.put("FI", new RegulaIban("Finlanda", 18, "^FI[0-9]{16}$", "FI + 2 cifre control + 14 cifre cont"));
        REGULI_IBAN_TARI.put("FR", new RegulaIban("Franta", 27, "^FR[0-9]{12}[A-Z0-9]{11}[0-9]{2}$", "FR + 2 cifre + 10 cifre banca/filiala + 11 caractere cont + 2 cifre RIB"));
        REGULI_IBAN_TARI.put("GR", new RegulaIban("Grecia", 27, "^GR[0-9]{9}[A-Z0-9]{16}$", "GR + 2 cifre + 7 cifre banca + 16 alfanumerice"));
        REGULI_IBAN_TARI.put("HR", new RegulaIban("Croatia", 21, "^HR[0-9]{19}$", "HR + 2 cifre + 17 cifre cont bancar"));
        REGULI_IBAN_TARI.put("HU", new RegulaIban("Ungaria", 28, "^HU[0-9]{26}$", "HU + 2 cifre + 24 cifre cont"));
        REGULI_IBAN_TARI.put("IE", new RegulaIban("Irlanda", 22, "^IE[0-9]{2}[A-Z]{4}[0-9]{14}$", "IE + 2 cifre + 4 litere banca + 6 cifre sort code + 8 cifre cont"));
        REGULI_IBAN_TARI.put("IT", new RegulaIban("Italia", 27, "^IT[0-9]{2}[A-Z][0-9]{10}[A-Z0-9]{12}$", "IT + 2 cifre + 1 litera CIN + 10 cifre ABI/CAB + 12 alfanumerice"));
        REGULI_IBAN_TARI.put("LT", new RegulaIban("Lituania", 20, "^LT[0-9]{18}$", "LT + 2 cifre + 16 cifre cont"));
        REGULI_IBAN_TARI.put("LU", new RegulaIban("Luxemburg", 20, "^LU[0-9]{4}[A-Z0-9]{14}$", "LU + 2 cifre + 3 cifre banca + 13 alfanumerice"));
        REGULI_IBAN_TARI.put("LV", new RegulaIban("Letonia", 21, "^LV[0-9]{2}[A-Z]{4}[A-Z0-9]{13}$", "LV + 2 cifre + 4 litere banca + 13 alfanumerice"));
        REGULI_IBAN_TARI.put("MT", new RegulaIban("Malta", 31, "^MT[0-9]{2}[A-Z]{4}[0-9]{5}[A-Z0-9]{18}$", "MT + 2 cifre + 4 litere banca + 5 cifre sucursala + 18 alfanumerice"));
        REGULI_IBAN_TARI.put("NL", new RegulaIban("Olanda", 18, "^NL[0-9]{2}[A-Z]{4}[0-9]{10}$", "NL + 2 cifre + 4 litere cod banca + 10 cifre cont"));
        REGULI_IBAN_TARI.put("PL", new RegulaIban("Polonia", 28, "^PL[0-9]{26}$", "PL + 2 cifre + 24 cifre cont"));
        REGULI_IBAN_TARI.put("PT", new RegulaIban("Portugalia", 25, "^PT[0-9]{23}$", "PT + 2 cifre + 21 cifre cont"));
        REGULI_IBAN_TARI.put("SE", new RegulaIban("Suedia", 24, "^SE[0-9]{22}$", "SE + 2 cifre control + 20 cifre cont"));
        REGULI_IBAN_TARI.put("SI", new RegulaIban("Slovenia", 19, "^SI[0-9]{17}$", "SI + 2 cifre + 15 cifre cont"));
        REGULI_IBAN_TARI.put("SK", new RegulaIban("Slovacia", 24, "^SK[0-9]{22}$", "SK + 2 cifre + 20 cifre cont"));

        // =====================================================================
        // TOATE STATELE DIN BALCANI SI TARI VECINE (Non-UE sau Europene)
        // =====================================================================
        REGULI_IBAN_TARI.put("RS", new RegulaIban("Serbia", 22, "^RS[0-9]{20}$", "RS + 2 cifre control + 3 cifre banca + 13 cifre cont + 2 cifre control (doar cifre)"));
        REGULI_IBAN_TARI.put("BA", new RegulaIban("Bosnia si Hertegovina", 20, "^BA[0-9]{18}$", "BA + 2 cifre control + 3 cifre banca + 13 cifre cont + 2 cifre control"));
        REGULI_IBAN_TARI.put("ME", new RegulaIban("Muntenegru", 22, "^ME[0-9]{20}$", "ME + 2 cifre control + 3 cifre banca + 13 cifre cont + 2 cifre control"));
        REGULI_IBAN_TARI.put("MK", new RegulaIban("Macedonia de Nord", 19, "^MK[0-9]{2}[A-Z0-9]{15}$", "MK + 2 cifre control + 3 cifre banca + 10 alfanumerice + 2 cifre control"));
        REGULI_IBAN_TARI.put("AL", new RegulaIban("Albania", 28, "^AL[0-9]{10}[A-Z0-9]{16}$", "AL + 2 cifre control + 8 cifre banca/sucursala + 16 alfanumerice"));
        REGULI_IBAN_TARI.put("XK", new RegulaIban("Kosovo", 20, "^XK[0-9]{18}$", "XK + 2 cifre control + 4 cifre banca + 10 cifre cont + 2 cifre control"));
        REGULI_IBAN_TARI.put("TR", new RegulaIban("Turcia", 26, "^TR[0-9]{7}[A-Z0-9]{17}$", "TR + 2 cifre control + 5 cifre cod banca + 1 caracter rezervat + 16 caractere cont"));
        REGULI_IBAN_TARI.put("MD", new RegulaIban("Republica Moldova", 24, "^MD[0-9]{2}[A-Z0-9]{2}[A-Z0-9]{18}$", "MD + 2 cifre control + 2 caractere cod banca + 18 caractere cont"));
        REGULI_IBAN_TARI.put("UA", new RegulaIban("Ucraina", 29, "^UA[0-9]{8}[A-Z0-9]{19}$", "UA + 2 cifre control + 6 cifre cod bancar MFO + 19 caractere cont"));

        // =====================================================================
        // ALTE STATE EUROPENE NON-UE (Elvetia, Marea Britanie, Norvegia, Islanda)
        // =====================================================================
        REGULI_IBAN_TARI.put("CH", new RegulaIban("Elvetia", 21, "^CH[0-9]{7}[A-Z0-9]{12}$", "CH + 2 cifre control + 5 cifre cod banca + 12 alfanumerice cont"));
        REGULI_IBAN_TARI.put("GB", new RegulaIban("Marea Britanie", 22, "^GB[0-9]{2}[A-Z]{4}[0-9]{14}$", "GB + 2 cifre + 4 litere banca + 6 cifre sort code + 8 cifre cont"));
        REGULI_IBAN_TARI.put("NO", new RegulaIban("Norvegia", 15, "^NO[0-9]{13}$", "NO + 2 cifre control + 11 cifre cont"));
        REGULI_IBAN_TARI.put("IS", new RegulaIban("Islanda", 26, "^IS[0-9]{24}$", "IS + 2 cifre control + 22 cifre cont"));

        // =====================================================================
        // ORIENTUL MIJLOCIU & AFRICA DE NORD (Afaceri & Comunitati romanesti)
        // =====================================================================
        REGULI_IBAN_TARI.put("AE", new RegulaIban("Emiratele Arabe Unite", 23, "^AE[0-9]{21}$", "AE + 2 cifre control + 3 cifre cod banca + 16 cifre cont (Dubai/Abu Dhabi)"));
        REGULI_IBAN_TARI.put("IL", new RegulaIban("Israel", 23, "^IL[0-9]{21}$", "IL + 2 cifre control + 3 cifre banca + 3 cifre filiala + 13 cifre cont"));
        REGULI_IBAN_TARI.put("SA", new RegulaIban("Arabia Saudita", 24, "^SA[0-9]{4}[A-Z0-9]{18}$", "SA + 2 cifre control + 2 cifre banca + 18 caractere cont"));
        REGULI_IBAN_TARI.put("QA", new RegulaIban("Qatar", 29, "^QA[0-9]{2}[A-Z]{4}[A-Z0-9]{21}$", "QA + 2 cifre control + 4 litere banca + 21 alfanumerice cont"));
        REGULI_IBAN_TARI.put("KW", new RegulaIban("Kuweit", 30, "^KW[0-9]{2}[A-Z]{4}[A-Z0-9]{22}$", "KW + 2 cifre control + 4 litere banca + 22 caractere cont"));
        REGULI_IBAN_TARI.put("JO", new RegulaIban("Iordania", 30, "^JO[0-9]{2}[A-Z]{4}[0-9]{4}[A-Z0-9]{18}$", "JO + 2 cifre control + 4 litere banca + 4 cifre sucursala + 18 caractere cont"));
        REGULI_IBAN_TARI.put("EG", new RegulaIban("Egipt", 29, "^EG[0-9]{27}$", "EG + 2 cifre control + 4 cifre banca + 4 cifre sucursala + 17 cifre cont"));
        REGULI_IBAN_TARI.put("TN", new RegulaIban("Tunisia", 24, "^TN[0-9]{22}$", "TN + 2 cifre control + 2 cifre banca + 3 cifre filiala + 13 cifre cont + 2 cifre control"));

        // =====================================================================
        // AMERICA LATINA (State cu standard oficial IBAN sau structura nationala)
        // =====================================================================
        REGULI_IBAN_TARI.put("BR", new RegulaIban("Brazilia", 29, "^BR[0-9]{25}[A-Z0-9]{2}$", "BR + 2 cifre control + 8 cifre ISPB + 5 cifre filiala + 10 cifre cont + 1 litera + 1 alfanumeric"));
        REGULI_IBAN_TARI.put("MX", new RegulaIban("Mexic", 22, "^MX[0-9]{20}$", "MX + 2 cifre control + 18 cifre CLABE"));
        REGULI_IBAN_TARI.put("AR", new RegulaIban("Argentina", 26, "^AR[0-9]{24}$", "AR + 2 cifre control + 22 cifre CBU"));
        REGULI_IBAN_TARI.put("CL", new RegulaIban("Chile", 24, "^CL[0-9]{2}[A-Z0-9]{4}[0-9]{16}$", "CL + 2 cifre control + 4 caractere banca + 16 cifre cont"));
        REGULI_IBAN_TARI.put("CO", new RegulaIban("Columbia", 24, "^CO[0-9]{22}$", "CO + 2 cifre control + 20 cifre cont bancar"));
        REGULI_IBAN_TARI.put("UY", new RegulaIban("Uruguay", 28, "^UY[0-9]{26}$", "UY + 2 cifre control + 4 cifre banca + 22 cifre cont"));
        REGULI_IBAN_TARI.put("PE", new RegulaIban("Peru", 24, "^PE[0-9]{22}$", "PE + 2 cifre control + 20 cifre CCI"));
        REGULI_IBAN_TARI.put("CR", new RegulaIban("Costa Rica", 22, "^CR[0-9]{20}$", "CR + 2 cifre control + 4 cifre banca + 14 cifre cont"));
        REGULI_IBAN_TARI.put("DO", new RegulaIban("Republica Dominicana", 28, "^DO[0-9]{2}[A-Z0-9]{4}[0-9]{20}$", "DO + 2 cifre control + 4 caractere banca + 20 cifre cont"));
        REGULI_IBAN_TARI.put("GT", new RegulaIban("Guatemala", 28, "^GT[0-9]{2}[A-Z0-9]{4}[A-Z0-9]{20}$", "GT + 2 cifre control + 4 caractere banca + 20 caractere cont"));
        REGULI_IBAN_TARI.put("PA", new RegulaIban("Panama", 24, "^PA[0-9]{2}[A-Z0-9]{4}[0-9]{16}$", "PA + 2 cifre control + 4 caractere banca + 16 cifre cont"));
        REGULI_IBAN_TARI.put("SV", new RegulaIban("El Salvador", 28, "^SV[0-9]{2}[A-Z]{4}[0-9]{20}$", "SV + 2 cifre control + 4 litere banca + 20 cifre cont"));
        REGULI_IBAN_TARI.put("NI", new RegulaIban("Nicaragua", 28, "^NI[0-9]{2}[A-Z]{4}[0-9]{20}$", "NI + 2 cifre control + 4 litere banca + 20 cifre cont"));

        // =====================================================================
        // ASIA DE SUD (Pakistan - oficial IBAN)
        // =====================================================================
        REGULI_IBAN_TARI.put("PK", new RegulaIban("Pakistan", 24, "^PK[0-9]{2}[A-Z]{4}[A-Z0-9]{16}$", "PK + 2 cifre control + 4 litere banca + 16 alfanumerice cont"));
    }

    /**
     * Valideaza formatul codului IBAN conform standardului international ISO 13616
     * si standardelor specifice nationale (Romania, Germania, Franta, Elvetia, Tarile Nordice, SUA, Canada etc.).
     *
     * @param iban codul IBAN de validat
     * @throws FormatDateIncorectException daca IBAN-ul nu respecta regulile de format
     */
    public void valideazaIban(String iban) throws FormatDateIncorectException {
        if (iban == null) {
            throw new FormatDateIncorectException("IBAN-ul nu poate fi nul.");
        }
        String cleanIban = iban.trim().toUpperCase().replaceAll("\\s+", "");

        // Verificare limite conform standardului international ISO 13616 (15..34 caractere)
        if (cleanIban.length() < 15 || cleanIban.length() > 34) {
            throw new FormatDateIncorectException(String.format(
                    "Lungimea IBAN-ului este invalida (%d caractere). Conform standardului ISO 13616, un IBAN trebuie sa aiba intre 15 si 34 de caractere.",
                    cleanIban.length()));
        }

        // Structura internationala generala: 2 litere (cod tara) + 2 cifre (control) + caractere alfanumerice
        if (!cleanIban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$")) {
            throw new FormatDateIncorectException(
                    "Format IBAN invalid: trebuie sa inceapa cu 2 litere (cod tara), urmate de 2 cifre de control si caractere alfanumerice.");
        }

        String codTara = cleanIban.substring(0, 2);

        // 1. Verificare pentru tarile din registrul SEPA / European
        RegulaIban regula = REGULI_IBAN_TARI.get(codTara);
        if (regula != null) {
            if (cleanIban.length() != regula.lungimeFixa()) {
                throw new FormatDateIncorectException(String.format(
                        "IBAN-ul pentru %s (%s) trebuie sa aiba exact %d caractere. Cel introdus are %d caractere.",
                        regula.denumireTara(), codTara, regula.lungimeFixa(), cleanIban.length()));
            }
            if (regula.regex() != null && !cleanIban.matches(regula.regex())) {
                throw new FormatDateIncorectException(String.format(
                        "Format IBAN invalid pentru %s (%s): structura ceruta este %s.",
                        regula.denumireTara(), codTara, regula.formatAsteptat()));
            }
            return;
        }

        // 2. State non-SEPA ce utilizeaza format extins ISO 13616 pentru transferuri transfrontaliere:
        // SUA (US), Canada (CA), Australia (AU), Noua Zeelanda (NZ)
        switch (codTara) {
            case "US" -> { // Statele Unite ale Americii (ABA Routing + Numar Cont)
                if (cleanIban.length() < 16 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru SUA (US) in format international cross-border ISO 13616 trebuie sa aiba intre 16 si 34 de caractere (US + 2 cifre control + 9 cifre routing ABA + cont).");
                }
            }
            case "CA" -> { // Canada (Transit + Numar Cont)
                if (cleanIban.length() < 15 || cleanIban.length() > 30) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Canada (CA) in format international ISO 13616 trebuie sa aiba intre 15 si 30 de caractere (CA + 2 cifre control + tranzit + cont).");
                }
            }
            case "AU" -> { // Australia (BSB Code + Numar Cont)
                if (cleanIban.length() < 15 || cleanIban.length() > 30) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Australia (AU) in format international ISO 13616 trebuie sa aiba intre 15 si 30 de caractere (AU + 2 cifre control + 6 cifre BSB + cont).");
                }
            }
            case "NZ" -> { // Noua Zeelanda (Cod Banca/Sucursala + Numar Cont)
                if (cleanIban.length() < 15 || cleanIban.length() > 30) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Noua Zeelanda (NZ) in format international ISO 13616 trebuie sa aiba intre 15 si 30 de caractere (NZ + 2 cifre control + cod banca/sucursala + cont).");
                }
            }
            case "NP" -> { // Nepal (Muncitori / remiteri de fonduri)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Nepal (NP) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "IN" -> { // India (Transferuri / afaceri IT & forta de munca)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru India (IN) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere (IN + 2 cifre control + cod IFSC + cont).");
                }
            }
            case "LK" -> { // Sri Lanka
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Sri Lanka (LK) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "VN" -> { // Vietnam
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Vietnam (VN) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "PH" -> { // Filipine
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Filipine (PH) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "BD" -> { // Bangladesh (Muncitori straini in Romania)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Bangladesh (BD) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "CN" -> { // China (Comert international si transferuri bancare B2B)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru China (CN) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere (CN + 2 cifre control + CNAPS + cont).");
                }
            }
            case "JP" -> { // Japonia (Partener comercial si investitii)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Japonia (JP) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere (JP + 2 cifre control + cod Zengin + cont).");
                }
            }
            case "KR" -> { // Coreea de Sud (Investitii industriale in Romania)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Coreea de Sud (KR) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "SG" -> { // Singapore (Centru financiar global)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Singapore (SG) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "HK" -> { // Hong Kong (Hub financiar)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Hong Kong (HK) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "MY" -> { // Malaezia
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Malaezia (MY) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "TH" -> { // Thailanda
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Thailanda (TH) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "ID" -> { // Indonezia (Comert international / forta de munca)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Indonezia (ID) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
            case "TW" -> { // Taiwan (Sector tehnologic IT)
                if (cleanIban.length() < 15 || cleanIban.length() > 34) {
                    throw new FormatDateIncorectException(
                            "IBAN-ul pentru Taiwan (TW) in format international cross-border ISO 13616 trebuie sa aiba intre 15 si 34 de caractere.");
                }
            }
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
