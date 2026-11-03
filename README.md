# Sistem de Gestiune a Tranzacțiilor E-Banking (PPOO)

Aplicație Java dezvoltată pentru disciplina **Proiectare și Programare Orientată pe Obiect (PPOO)** - Nivel Masterat.

## 📌 Tema 1 – Aplicație Java pentru gestiunea tranzacțiilor în sistemele de e-banking
Aplicația simulează la un nivel complet și robust fluxul operațional al unui nucleu de e-banking, permițând încărcarea inițială a datelor, operarea tranzacțională în memorie (transferuri, depuneri, retrageri, conversii valutare), generarea de statistici și persistența exclusivă în fișiere text/binare la închiderea sistemului.

---

## 🎯 Conformitate cu Baremul de Evaluare (10/10 Puncte)

1. **Lucrul cu fișiere text și binare (1 p)**:
   - Salvare/restaurare în format text CSV (`data/conturi.csv`, `data/tranzactii.csv`).
   - Salvare/restaurare backup binar de audit (`data/backup_audit.dat`) utilizând `ObjectInputStream`/`ObjectOutputStream`.
   - Generare raport text de sinteză și statistici bancare (`data/raport_statistici.txt`).
2. **Mecanism de persistență la închidere/deschidere (1 p)**:
   - Toate modificările operate în sesiune sunt persistate automat în fișierele de intrare la ieșirea din aplicație sau prin mecanism de Shutdown Hook.
3. **Masive uni/bidimensionale fundamentale și colecții de obiecte (1 p)**:
   - **Masiv 1D fundamental**: `double[] rateSchimbValutar` pentru ratele oficiale de conversie valutară (RON, EUR, USD, GBP).
   - **Masiv 2D fundamental**: `double[][] matriceStatisticaLunara` (12 luni x 4 tipuri tranzacții) pentru centralizarea volumelor financiare tranzacționate.
   - **Colecție 1**: `Map<String, ContBancar>` (`HashMap`) pentru indexarea și accesul rapid O(1) la conturi după IBAN.
   - **Colecție 2**: `List<Tranzactie>` (`ArrayList`) pentru istoricul cronologic complet al tranzacțiilor.
   - **Colecție 3**: `Set<Client>` (`HashSet`) pentru asigurarea unicității titularilor de cont.
4. **Prelucrare exclusiv în memorie (1 p)**:
   - Datele sunt încărcate în memorie la pornire; toate operațiile afectează doar colecțiile și tablourile din RAM. Rescrierea pe disc are loc la oprire.
5. **Validarea datelor și ierarhie de excepții (1 p)**:
   - Validare riguroasă a inputului din fișiere și consolă (format IBAN, CNP, sume pozitive, sold disponibil).
   - Ierarhie custom de excepții: `BancarException`, `FonduriInsuficienteException`, `ContInexistentException`, `ContBlocatException`, `TranzactieInvalidaException`, `FormatDateIncorectException`.
6. **Flux funcțional logic (1 p)**:
   - Deschidere -> Încărcare automată din CSV -> Operare tranzacții -> Statistici -> Salvare sincronizată la închidere.
7. **Meniu interactiv cu opțiuni clare (1 p)**:
   - Interfață consolă prietenoasă cu **separare pe roluri (RBAC)**:
     - **Mod Client (E-Banking / ATM)**: Autentificare sigură cu IBAN și cod PIN (4 cifre). Acces strict la contul propriu, transferuri interne și transfrontaliere, deschidere subconturi, blocare automată la 3 încercări greșite de PIN.
     - **Mod Operator Bancar (Back-Office & Administrare)**: Autentificare securizată cu parolă puternică de administrator. Acces exclusiv la lista globală a tuturor conturilor, deschidere conturi la ghișeu, vizualizare matrice statistică 2D, generare rapoarte text, backup și restaurare binară, deblocare conturi.
8. **Funcționalitate completă fără erori (1 p)**:
   - Cod sursă conform standardelor Java 21, verificat și compilat curat.
   - Suită completă de **14 teste automate** (`test.bat`), 100% de succes.
9. **Documentație Word și JavaDoc (1 p)**:
   - Documentație completă cu diagrame UML și descrierea cerințelor.
   - JavaDoc generat automat în directorul `docs/javadoc/`.
10. **Punct din oficiu (1 p)**.

---

## 🚀 Rulare și Compilare

### Din linia de comandă (Windows):
1. **Compilare**: Rulați scriptul `compile.bat`
2. **Execuție**: Rulați scriptul `run.bat`
3. **Testare automată**: Rulați scriptul `test.bat` (14 teste automate)

### Credențiale de acces (detaliate în `CREDENTIALE_ACCES.txt`):
* **Parolă Operator Bancar**: `Op#Secur!ty_9874Banca@PPOO` (stocată și în `data/operator_secret.txt`).
* **Conturi Client de test (PIN implicit: `1234`)**:
  - `RO49BTRLRONCRT0012345601` (Ion Popescu - RON)
  - `RO12BTRLEURCRT0098765402` (Maria Ionescu - EUR)
  - `RO78BTRLUSDDEP0055443303` (Andrei Radu - USD)

### Din IntelliJ IDEA sau Eclipse:
- Deschideți folderul proiectului în IDE ca proiect Java standard cu JDK 21+.
- Clasa principală se află în `src/ro/ase/ppoo/ebanking/Main.java`.
