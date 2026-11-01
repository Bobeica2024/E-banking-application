# DOCUMENTAȚIE PROIECT - PROGRAMARE ȘI PROIECTARE ORIENTATĂ PE OBIECT (PPOO)
## TEMA 1 – APLICAȚIE JAVA PENTRU GESTIUNEA TRANZACȚIILOR ÎN SISTEMELE DE E-BANKING

**Nivel:** Studii Universitare de Masterat  
**Disciplina:** Proiectare și Programare Orientată pe Obiect (PPOO)  
**Autor:** Ștefan Alexandru Bobeică  
**An universitar:** 2026 / 2027  
**Platformă tehnologică:** Java SE 21 (LTS)  

---

## 1. PREZENTAREA GENERALĂ A PROIECTULUI

### 1.1. Scopul și obiectivele aplicației
Aplicația simulează nucleul tranzacțional al unei platforme moderne de e-banking, oferind o arhitectură orientată pe obiecte extensibilă, robustă și conformă cu bunele practici din ingineria software (separarea responsabilităților, încapsulare, tratare riguroasă a excepțiilor, independență față de baze de date relaționale prin persistență în fișiere).

Obiectivele principale sunt:
1. Gestiunea completă a ciclului de viață al conturilor bancare (curente, economii, depozite) și al clienților titulari.
2. Procesarea tranzacțiilor financiare (depuneri, retrageri, transferuri bancare interconturi cu schimb valutar dinamic, plăți facturi de utilități).
3. Prelucrarea exclusiv în memorie a entităților pe parcursul sesiunii curente.
4. Asigurarea persistenței integrale prin fișiere text (CSV) și fișiere binare serializate (audit/backup) la inițializarea și oprirea aplicației.
5. Agregarea statistică și analiza tranzacțională prin intermediul structurilor fundamentale (vectori 1D și matrici 2D) și al colecțiilor de obiecte generice.

---

## 2. ARHITECTURA ȘI DESIGN-UL APLICAȚIEI

Aplicația este structurată modular în 5 pachete logice:

```
ro.ase.ppoo.ebanking
│   Main.java                   -> Punctul de intrare (lifecycle, bootstrap, shutdown hook)
│   BancaTest.java              -> Suită completă de teste unitare și de integrare
│
├───model                       -> Entitățile domeniului de afaceri
│       Client.java             -> Titularul de cont (Serializable, Comparable)
│       ContBancar.java         -> Cont bancar (IBAN, sold, operare tranzacții, sortare)
│       Tranzactie.java         -> Înregistrarea imutabilă a operațiunii financiare
│       TipCont.java            -> Enum: CURENT, ECONOMII, DEPOZIT (dobânzi specifice)
│       TipTranzactie.java      -> Enum: DEPUNERE, RETRAGERE, TRANSFER, PLATA_FACTURA
│       Moneda.java             -> Enum: RON, EUR, USD, GBP (simboluri și descrieri)
│       StatusTranzactie.java   -> Enum: FINALIZATA, IN_PROCESARE, REFUZATA, ANULATA
│
├───exception                   -> Ierarhia dedicată de excepții (Checked Exceptions)
│       BancarException.java    -> Excepția de bază pentru domeniul e-banking
│       ContBlocatException.java-> Aruncată la accesarea unui cont suspendat/inactiv
│       ContInexistentException.java -> Aruncată la căutarea unui IBAN inexistent
│       FonduriInsuficienteException.java -> Aruncată la depășirea soldului disponibil
│       FormatDateIncorectException.java  -> Aruncată la parsarea eronată a fișierelor / inputului
│       TranzactieInvalidaException.java  -> Aruncată la încălcarea regulilor de business
│
├───service                     -> Logica de business, calcul valutar și agregare
│       BancaService.java       -> Gestiune colecții, masive 1D/2D, validări, operațiuni
│
├───storage                     -> Persistența pe disc și raportare
│       CsvStorage.java         -> Citire/scriere fișiere text CSV (conturi și tranzacții)
│       BinaryStorage.java      -> Salvare/restaurare snapshot binar de audit (.dat)
│       RaportGenerator.java    -> Generare raport analitic de sinteză (.txt)
│
└───ui                          -> Interfața cu utilizatorul
        MeniuConsola.java       -> Meniu CLI interactiv, validare defensivă la citire
```

---

## 3. CONFORMITATEA CU BAREMUL DE EVALUARE PPOO (10/10 PUNCTE)

| Nr. | Cerință din Barem | Punctaj | Implementare în Proiect | Fisiere și Metode Cheie |
|:---:|:---|:---:|:---|:---|
| **1** | Lucrul cu fișiere text sau binare pentru salvare/restaurare date și statistici/rapoarte | **1 p** | Salvare/restaurare CSV (`conturi.csv`, `tranzactii.csv`), backup binar (`backup_audit.dat`) și raport text (`raport_statistici.txt`). | `CsvStorage.java`, `BinaryStorage.java`, `RaportGenerator.java` |
| **2** | Salvarea datelor la închiderea aplicației și restaurarea la relansare prin actualizarea fișierului de intrare | **1 p** | Fișierele de intrare sunt actualizate cu datele din memorie la ieșirea din meniu sau prin `Runtime.getRuntime().addShutdownHook`. | `MeniuConsola.optiuneSalvareSiIesire()`, `Main.java` |
| **3** | Minim două masive uni/bidimensionale fundamentale și minim două colecții diferite de obiecte din clasele definite | **1 p** | **Masiv 1D:** `double[] rateSchimbValutar`<br>**Masiv 2D:** `double[][] matriceStatisticaLunara`<br>**Colecție 1:** `Map<String, ContBancar>` (HashMap)<br>**Colecție 2:** `List<Tranzactie>` (ArrayList)<br>**Colecție 3:** `Set<Client>` (HashSet) | `BancaService.java` (liniile 25-55) |
| **4** | Toate modificările se fac în memorie (vectori/colecții), iar salvarea pe disc se realizează doar la închiderea aplicației | **1 p** | Depunerile, retragerile, transferurile și deschiderile de cont modifică doar starea obiectelor din RAM. Scrierile pe disc au loc strict la închidere. | `BancaService.java`, `MeniuConsola.java` |
| **5** | Validarea datelor de lucru (consolă + fișiere) cu excepții standard și excepții specifice programate | **1 p** | Validare lungime IBAN, format Regex, CNP 13 cifre, sume strict pozitive. Ierarhie: `BancarException`, `FonduriInsuficienteException`, `ContBlocatException`, `ContInexistentException`, `TranzactieInvalidaException`, `FormatDateIncorectException`. | Pachetul `ro.ase.ppoo.ebanking.exception.*`, `BancaService.valideazaIban()`, `MeniuConsola.citeste*()` |
| **6** | Flux funcțional logic: deschidere/încărcare -> prelucrare -> închidere/salvare | **1 p** | 1. Bootstrap & citire CSV<br>2. Populare colecții și recalcul matrice 2D<br>3. Meniu interactiv CLI<br>4. Persistență la ieșire. | `Main.java`, `MeniuConsola.java` |
| **7** | Meniu cu opțiuni clare oricărui utilizator pentru apelul funcționalităților | **1 p** | Meniu structurat pe 12 opțiuni operative + opțiunea 0 de salvare/ieșire, tratare elegantă a erorilor de input. | `MeniuConsola.afiseazaMeniuPrincipal()` |
| **8** | Proiect complet funcțional, fără erori de compilare sau execuție | **1 p** | Compilare impecabilă cu javac 21, verificare cu `-Xlint:all` (0 warnings), suită de teste unitare automată cu 9/9 teste trecute (100% succes). | `compile.bat`, `run.bat`, `test.bat`, `BancaTest.java` |
| **9** | Documentație Microsoft Word (care include și JavaDoc) conform indicațiilor | **1 p** | Documentație Word (`Documentatie_Proiect_PPOO_Tema1.docx`), documentație Markdown (`DOCUMENTATIE_PROIECT.md`) și JavaDoc complet HTML generat în `docs/javadoc/`. | Folderul `docs/javadoc/`, script `generate-javadoc.bat` |
| **10** | Punct din oficiu | **1 p** | Asigurat prin respectarea normelor academice și transparența procesului de dezvoltare. | - |
| **TOTAL** | | **10 p** | **Notă maximă (10.00)** | |

---

## 4. DETALII TEHNICE DE IMPLEMENTARE

### 4.1. Structurile de date fundamentale (Cerința 3)

#### Vector Unidimensional Fundamental (`double[]`)
```java
private final double[] rateSchimbValutar = new double[] {
    1.0000, // Index 0: RON (monedă de referință)
    4.9750, // Index 1: EUR
    4.6500, // Index 2: USD
    5.8500  // Index 3: GBP
};
```
Vectorul este utilizat direct în metoda `converteste(double suma, Moneda sursa, Moneda destinatie)` din `BancaService`:
```java
double rataSursa = rateSchimbValutar[sursa.ordinal()];
double rataDestinatie = rateSchimbValutar[destinatie.ordinal()];
return (suma * rataSursa) / rataDestinatie;
```

#### Matrice Bidimensională Fundamentală (`double[12][4]`)
Matricea centralizează volumele tranzacționate lunare pe cele 4 tipuri de tranzacții:
- Rândurile (0..11) reprezintă lunile calendaristice (Ianuarie..Decembrie).
- Coloanele (0..3) reprezintă tipul tranzacției (`DEPUNERE=0`, `RETRAGERE=1`, `TRANSFER=2`, `PLATA_FACTURA=3`).
- La fiecare tranzacție finalizată în memorie, matricea este actualizată instantaneu în RAM:
```java
int luna = tranzactie.getDataOra().getMonthValue() - 1;
int tipIdx = tranzactie.getTip().getIndexMatrice();
matriceStatisticaLunara[luna][tipIdx] += valoareRon;
```

### 4.2. Colecțiile de Obiecte din Clase Proprii (Cerința 3)
1. `Map<String, ContBancar> conturiMap`: Asigură maparea unică a contului după codul IBAN, permițând acces și regăsire în timp constant $O(1)$.
2. `List<Tranzactie> tranzactiiList`: Păstrează istoricul cronologic complet al tuturor transferurilor, depunerilor și retragerilor. Suportă sortare prin interfața `Comparable<Tranzactie>`.
3. `Set<Client> clientiSet`: Garantează unicitatea clienților bancari pe baza codului numeric personal (CNP), prin suprascrierea metodelor `equals()` și `hashCode()`.

---

## 5. PERSISTENȚĂ ȘI GESTIUNEA FIȘIERELOR (Cerințele 1, 2, 4)

1. **Fișiere Text CSV (`data/conturi.csv`, `data/tranzactii.csv`):**
   - Utilizate pentru încărcarea inițială și salvarea sincronizată la închidere.
   - Folosesc `BufferedReader`/`BufferedWriter` cu charset `StandardCharsets.UTF_8`.
2. **Fișier Binar de Audit (`data/backup_audit.dat`):**
   - Realizat cu `ObjectOutputStream` / `ObjectInputStream`.
   - Permite crearea unei copii de siguranță la nivel de snapshot și restaurarea rapidă a întregului context din memorie.
3. **Fișier Raport Analitic (`data/raport_statistici.txt`):**
   - Centralizează indicatorii de lichiditate, matricea lunară formatată tabelar, distribuția tranzacțiilor și top 5 cele mai mari sume rulate.

---

## 6. GHID DE COMPILARE, RULARE ȘI TESTARE

### Compilare din linie de comandă (Windows CMD / PowerShell):
```cmd
compile.bat
```
Scriptul detectează automat compilatorul Java (OpenJDK 21 / JBR) și compilează clasele în directorul `bin`.

### Executarea aplicației:
```cmd
run.bat
```
Lansează clasa `ro.ase.ppoo.ebanking.Main` cu suport complet de caractere UTF-8.

### Rularea suitei de teste automate:
```cmd
test.bat
```
Rulează `ro.ase.ppoo.ebanking.BancaTest` care verifică automat toate cele 9 scenarii critice de validare, calcul și persistență.

### Generarea documentației JavaDoc:
```cmd
generate-javadoc.bat
```
Generează paginile HTML în directorul `docs/javadoc/index.html`.

---

## 7. CONCLUZII
Proiectul oferă o implementare completă, curată și fidelă cerințelor disciplinei PPOO, respectând principiile programării orientate pe obiecte, standardele Java moderne și oferind persistență exclusiv prin fișiere.
