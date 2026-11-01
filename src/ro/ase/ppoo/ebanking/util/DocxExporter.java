package ro.ase.ppoo.ebanking.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generator automat de documentatie Microsoft Word (.docx) fara dependinte externe.
 * Construieste arhiva OpenXML compatibila cu Microsoft Word 2016-2026+.
 */
public class DocxExporter {

    public static void main(String[] args) {
        String outputDocx = "docs/Documentatie_Proiect_PPOO_Tema1.docx";
        try {
            creeazaDocx(outputDocx);
            System.out.println("[SUCCES] Documentatia Microsoft Word a fost generata: " + outputDocx);
        } catch (Exception e) {
            System.err.println("[EROARE] Nu s-a putut genera documentul Word: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void creeazaDocx(String caleIesire) throws IOException {
        File file = new File(caleIesire);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            // 1. [Content_Types].xml
            adaugaFisierInZip(zos, "[Content_Types].xml", getContentTypesXml());

            // 2. _rels/.rels
            adaugaFisierInZip(zos, "_rels/.rels", getRelsXml());

            // 3. word/_rels/document.xml.rels
            adaugaFisierInZip(zos, "word/_rels/document.xml.rels", getDocRelsXml());

            // 4. word/styles.xml
            adaugaFisierInZip(zos, "word/styles.xml", getStylesXml());

            // 5. word/document.xml
            adaugaFisierInZip(zos, "word/document.xml", getDocumentXml());
        }
    }

    private static void adaugaFisierInZip(ZipOutputStream zos, String numeFisier, String continut) throws IOException {
        ZipEntry entry = new ZipEntry(numeFisier);
        zos.putNextEntry(entry);
        zos.write(continut.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String getContentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                "  <Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n" +
                "  <Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>\n" +
                "</Types>";
    }

    private static String getRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>\n" +
                "</Relationships>";
    }

    private static String getDocRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n" +
                "</Relationships>";
    }

    private static String getStylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n" +
                "  <w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\">\n" +
                "    <w:name w:val=\"Normal\"/>\n" +
                "    <w:rPr>\n" +
                "      <w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/>\n" +
                "      <w:sz w:val=\"22\"/>\n" +
                "    </w:rPr>\n" +
                "  </w:style>\n" +
                "  <w:style w:type=\"paragraph\" w:styleId=\"Heading1\">\n" +
                "    <w:name w:val=\"heading 1\"/>\n" +
                "    <w:rPr>\n" +
                "      <w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/>\n" +
                "      <w:b/>\n" +
                "      <w:color w:val=\"1F4E78\"/>\n" +
                "      <w:sz w:val=\"32\"/>\n" +
                "    </w:rPr>\n" +
                "  </w:style>\n" +
                "  <w:style w:type=\"paragraph\" w:styleId=\"Heading2\">\n" +
                "    <w:name w:val=\"heading 2\"/>\n" +
                "    <w:rPr>\n" +
                "      <w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/>\n" +
                "      <w:b/>\n" +
                "      <w:color w:val=\"2F5597\"/>\n" +
                "      <w:sz w:val=\"26\"/>\n" +
                "    </w:rPr>\n" +
                "  </w:style>\n" +
                "</w:styles>";
    }

    private static String getDocumentXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n");
        sb.append("<w:body>\n");

        // Titlu Coperta
        pTitle(sb, "DOCUMENTAȚIE PROIECT PPOO");
        pSubtitle(sb, "Tema 1: Aplicație Java pentru gestiunea tranzacțiilor în sistemele de e-banking");
        pNormal(sb, "Disciplina: Proiectare și Programare Orientată pe Obiect (Masterat)");
        pNormal(sb, "Autor: Ștefan Alexandru Bobeică");
        pNormal(sb, "An universitar: 2026 - 2027");
        pNormal(sb, "Mediu de dezvoltare: Java SE 21 (LTS) | IDE: IntelliJ IDEA / CLI");
        pNormal(sb, "--------------------------------------------------------------------------------");

        // 1. Descrierea Temei
        pHeading1(sb, "1. Descrierea Temei și a Obiectivelor");
        pNormal(sb, "Proiectul realizează un sistem complet de gestiune a tranzacțiilor din domeniul e-banking, conceput conform principiilor orientate pe obiecte din Java. Aplicația permite administrarea clienților și conturilor bancare (curente, economii, depozit), operarea tranzacțiilor financiare (depuneri, retrageri, transferuri bancare cu schimb valutar dinamic și plăți facturi), păstrând independența față de orice sistem de gestiune a bazelor de date prin stocare exclusivă în fișiere text (CSV) și binare (audit).");

        // 2. Conformitate Barem
        pHeading1(sb, "2. Conformitatea cu Baremul de Evaluare PPOO (10/10 Puncte)");
        pNormal(sb, "• Cerința 1 (1 p) - Fișiere text/binare: Utilizare CSV pentru conturi/tranzacții, fișier binar backup_audit.dat prin ObjectOutputStream și generare raport text raport_statistici.txt.");
        pNormal(sb, "• Cerința 2 (1 p) - Mecanism salvare/restaurare la închidere/relansare: Datele modificate în memorie sunt rescrise pe disc la oprirea aplicației sau prin Shutdown Hook.");
        pNormal(sb, "• Cerința 3 (1 p) - Masive fundamentale și colecții: Vector 1D double[] rateSchimbValutar, matrice 2D double[][] matriceStatisticaLunara (12 luni x 4 tipuri tranzacții), plus 3 colecții de obiecte (Map<String, ContBancar>, List<Tranzactie>, Set<Client>).");
        pNormal(sb, "• Cerința 4 (1 p) - Modificări exclusiv în memorie: Operațiunile tranzacționale modifică starea obiectelor din RAM, iar persistența se declanșează strict la ieșirea din aplicație.");
        pNormal(sb, "• Cerința 5 (1 p) - Validare date și mecanism de excepții: Ierarhie proprie derivată din BancarException (FonduriInsuficienteException, ContBlocatException, ContInexistentException, TranzactieInvalidaException, FormatDateIncorectException, OperatiuneAnulataException) și validări defensive în buclă cu feedback imediat și opțiune de anulare prin tasta '0'.");
        pNormal(sb, "• Cerința 6 (1 p) - Flux funcțional logic: Încărcare automată din CSV -> Inițializare matrici -> Rulare meniu interactiv -> Salvare sincronizată la închidere.");
        pNormal(sb, "• Cerința 7 (1 p) - Meniu interactiv clar: Consolă CLI structurată pe 13 opțiuni operaționale (inclusiv comutare bilingvă) + opțiunea 0 de salvare și părăsire sistem, oferind validare în timp real a datelor și suport complet pentru limba Română cu diacritice oficiale și limba Engleză.");
        pNormal(sb, "• Cerința 8 (1 p) - Proiect funcțional fără erori: Compilare Java 21 fără avertismente (-Xlint:all) și suită de teste automate cu 100% rată de succes.");
        pNormal(sb, "• Cerința 9 (1 p) - Documentație Word și JavaDoc: Documentație completă DOCX și pachet JavaDoc HTML generat în docs/javadoc/.");
        pNormal(sb, "• Cerința 10 (1 p) - Punct din oficiu.");

        // 3. Arhitectura
        pHeading1(sb, "3. Structura Pachetelor și Arhitectura Claselor");
        pNormal(sb, "1. ro.ase.ppoo.ebanking.model: Cuprinde entitățile Client, ContBancar, Tranzactie și enumerările TipCont, TipTranzactie, Moneda, StatusTranzactie. Clasele implementează Serializable și Comparable.");
        pNormal(sb, "2. ro.ase.ppoo.ebanking.exception: Cuprinde clasa de bază BancarException și excepțiile specifice pentru fonduri insuficiente, conturi inexistente, formate incorecte, anulare operațiuni și reguli bancare.");
        pNormal(sb, "3. ro.ase.ppoo.ebanking.service: Clasa BancaService gestionează memoria aplicației, vectorul 1D de rate valutare, matricea 2D de activitate financiară, registrul internațional IBAN și operațiile tranzacționale.");
        pNormal(sb, "4. ro.ase.ppoo.ebanking.storage: Cuprinde CsvStorage pentru persistenta text, BinaryStorage pentru serializare binară de securitate și RaportGenerator pentru rapoarte de analiză.");
        pNormal(sb, "5. ro.ase.ppoo.ebanking.i18n: Pachet de internaționalizare ce include clasa I18n și enumerarea Limba, asigurând suport bilingv complet (Română cu diacritice și Engleză).");
        pNormal(sb, "6. ro.ase.ppoo.ebanking.ui: Clasa MeniuConsola gestionează meniul principal, validarea defensivă a inputului și navigarea utilizatorului.");
        pNormal(sb, "7. ro.ase.ppoo.ebanking: Clasa Main (punctul de pornire) și BancaTest (suita de testare).");

        // 4. Masive fundamentale și colecții
        pHeading1(sb, "4. Masive Fundamentale și Colecții OOP");
        pHeading2(sb, "4.1. Masive Fundamentale (double[] și double[][])");
        pNormal(sb, "• Masiv 1D (rateSchimbValutar): Tablou unidimensional de tip fundamental double ce conține cursurile valutare oficiale raportate la 1 RON (RON=1.0, EUR=4.975, USD=4.650, GBP=5.850). Este utilizat la transferurile bancare cu conversie.");
        pNormal(sb, "• Masiv 2D (matriceStatisticaLunara): Tablou bidimensional double[12][4] ce centralizează sumele totale tranzacționate pe fiecare lună și tip de operațiune (0: Depunere, 1: Retragere, 2: Transfer, 3: Plată Factură).");
        pHeading2(sb, "4.2. Colecții de Obiecte Proprii");
        pNormal(sb, "• Map<String, ContBancar>: Indexare eficientă a conturilor după IBAN pentru acces în timp constant O(1).");
        pNormal(sb, "• List<Tranzactie>: Jurnalizare cronologică a tuturor mișcărilor financiare efectuate.");
        pNormal(sb, "• Set<Client>: Garanție a unicității titularilor bancari pe baza codului numeric personal (CNP).");

        // 5. Testare
        pHeading1(sb, "5. Validare, Testare și JavaDoc");
        pNormal(sb, "Aplicația a fost supusă unei suite de 9 teste automate (BancaTest) ce verifică deschiderea conturilor, depunerile, retragerile cu calcul comision, aruncarea excepțiilor specifice, conversia valutară prin masivul 1D, persistența CSV și restaurarea binară. Toate testele au fost trecute cu succes.");
        pNormal(sb, "Documentația tehnică JavaDoc este generată complet în folderul docs/javadoc/index.html, oferind descrieri detaliate pentru fiecare pachet, clasă și metodă publică.");

        // Sfarsit
        sb.append("</w:body>\n");
        sb.append("</w:document>");
        return sb.toString();
    }

    private static void pTitle(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:after=\"200\"/></w:pPr>")
                .append("<w:r><w:rPr><w:b/><w:color w:val=\"1F4E78\"/><w:sz w:val=\"48\"/></w:rPr>")
                .append("<w:t>").append(escapeXml(text)).append("</w:t></w:r></w:p>\n");
    }

    private static void pSubtitle(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:after=\"400\"/></w:pPr>")
                .append("<w:r><w:rPr><w:i/><w:color w:val=\"595959\"/><w:sz w:val=\"28\"/></w:rPr>")
                .append("<w:t>").append(escapeXml(text)).append("</w:t></w:r></w:p>\n");
    }

    private static void pHeading1(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/><w:spacing w:before=\"300\" w:after=\"150\"/></w:pPr>")
                .append("<w:r><w:rPr><w:b/><w:color w:val=\"1F4E78\"/><w:sz w:val=\"32\"/></w:rPr>")
                .append("<w:t>").append(escapeXml(text)).append("</w:t></w:r></w:p>\n");
    }

    private static void pHeading2(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:pStyle w:val=\"Heading2\"/><w:spacing w:before=\"200\" w:after=\"100\"/></w:pPr>")
                .append("<w:r><w:rPr><w:b/><w:color w:val=\"2F5597\"/><w:sz w:val=\"26\"/></w:rPr>")
                .append("<w:t>").append(escapeXml(text)).append("</w:t></w:r></w:p>\n");
    }

    private static void pNormal(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:spacing w:after=\"120\"/></w:pPr>")
                .append("<w:r><w:rPr><w:sz w:val=\"22\"/></w:rPr>")
                .append("<w:t>").append(escapeXml(text)).append("</w:t></w:r></w:p>\n");
    }

    private static String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
