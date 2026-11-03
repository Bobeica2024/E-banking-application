package ro.ase.ppoo.ebanking.i18n;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Serviciu de internaționalizare (i18n) și localizare pentru aplicația e-banking.
 * Oferă suport bilingv complet:
 * 1. Limba Română (cu diacritice oficiale complete: ă, â, î, ș, ț, Ă, Â, Î, Ș, Ț).
 * 2. Limba Engleză (standard internațional).
 *
 * Permite comutarea dinamică a limbii în timpul execuției fără repornirea aplicației.
 */
public final class I18n {

    private static Limba limbaCurenta = Limba.ROMANA;

    private static final Map<Limba, Map<String, String>> MESAJE = new EnumMap<>(Limba.class);

    static {
        Map<String, String> ro = new HashMap<>();
        Map<String, String> en = new HashMap<>();

        // =====================================================================
        // MENIU PRINCIPAL SI NAVIGARE
        // =====================================================================
        ro.put("MENU_TITLE", "SISTEM E-BANKING PPOO - MENIU PRINCIPAL");
        en.put("MENU_TITLE", "PPOO E-BANKING SYSTEM - MAIN MENU");

        // Poarta de acces
        ro.put("GATE_TITLE", "SISTEM E-BANKING PPOO - POARTĂ DE ACCES");
        en.put("GATE_TITLE", "PPOO E-BANKING SYSTEM - ACCESS GATEWAY");

        ro.put("GATE_OPT_1", "1. Autentificare Client (IBAN + Cod PIN)");
        en.put("GATE_OPT_1", "1. Client Login (IBAN + PIN code)");

        ro.put("GATE_OPT_2", "2. Autentificare Operator Bancar (Consolă Administrare & Statistici)");
        en.put("GATE_OPT_2", "2. Bank Operator Login (Admin & Statistics Console)");

        ro.put("GATE_OPT_3", "3. Schimbare limbă / Change language (Română / English)");
        en.put("GATE_OPT_3", "3. Change language / Schimbare limbă (English / Română)");

        ro.put("GATE_OPT_0", "0. Salvare date în fișiere și Ieșire din aplicație");
        en.put("GATE_OPT_0", "0. Save data to files and Exit application");

        // Portal Client
        ro.put("CLIENT_MENU_TITLE", "PORTAL CLIENT E-BANKING - %s (%s)");
        en.put("CLIENT_MENU_TITLE", "E-BANKING CLIENT PORTAL - %s (%s)");

        ro.put("CLIENT_OPT_1", "1. Vizualizare sold și detalii cont curent");
        en.put("CLIENT_OPT_1", "1. View balance and current account details");

        ro.put("CLIENT_OPT_2", "2. Depunere numerar");
        en.put("CLIENT_OPT_2", "2. Cash deposit");

        ro.put("CLIENT_OPT_3", "3. Retragere numerar");
        en.put("CLIENT_OPT_3", "3. Cash withdrawal");

        ro.put("CLIENT_OPT_4", "4. Transfer bancar (intern sau internațional către conturi externe)");
        en.put("CLIENT_OPT_4", "4. Bank transfer (internal or international cross-border)");

        ro.put("CLIENT_OPT_5", "5. Plată factură utilități");
        en.put("CLIENT_OPT_5", "5. Utility bill payment");

        ro.put("CLIENT_OPT_6", "6. Istoric tranzacții personale");
        en.put("CLIENT_OPT_6", "6. Personal transaction history");

        ro.put("CLIENT_OPT_7", "7. Deschidere cont suplimentar (altă monedă / economii)");
        en.put("CLIENT_OPT_7", "7. Open additional sub-account (currency / savings)");

        ro.put("CLIENT_OPT_8", "8. Resetare / Schimbare cod PIN");
        en.put("CLIENT_OPT_8", "8. Reset / Change PIN code");

        ro.put("CLIENT_OPT_0", "0. Delogare (Revenire la meniul principal)");
        en.put("CLIENT_OPT_0", "0. Logout (Return to main menu)");

        // Consola Operator
        ro.put("OPERATOR_MENU_TITLE", "CONSOLĂ OPERATOR BANCAR & BACK-OFFICE");
        en.put("OPERATOR_MENU_TITLE", "BANK OPERATOR & BACK-OFFICE CONSOLE");

        ro.put("OPERATOR_OPT_1", "1. Vizualizare listă toate conturile bancare și soldurile");
        en.put("OPERATOR_OPT_1", "1. View all bank accounts list and balances");

        ro.put("OPERATOR_OPT_2", "2. Deschidere cont bancar nou la ghișeu");
        en.put("OPERATOR_OPT_2", "2. Open new bank account at teller");

        ro.put("OPERATOR_OPT_3", "3. Vizualizare statistici lunare (Matrice 2D fundamentală)");
        en.put("OPERATOR_OPT_3", "3. View monthly statistics (Fundamental 2D matrix)");

        ro.put("OPERATOR_OPT_4", "4. Generare raport statistic în fișier text");
        en.put("OPERATOR_OPT_4", "4. Generate statistical report in text file");

        ro.put("OPERATOR_OPT_5", "5. Salvare copie de siguranță binară (Backup .dat)");
        en.put("OPERATOR_OPT_5", "5. Export binary backup copy (.dat)");

        ro.put("OPERATOR_OPT_6", "6. Restaurare bază de date din backup binar");
        en.put("OPERATOR_OPT_6", "6. Restore database from binary backup");

        ro.put("OPERATOR_OPT_7", "7. Blocare / Deblocare cont bancar");
        en.put("OPERATOR_OPT_7", "7. Lock / Unlock bank account");

        ro.put("OPERATOR_OPT_8", "8. Schimbare parolă Operator");
        en.put("OPERATOR_OPT_8", "8. Change Operator password");

        ro.put("OPERATOR_OPT_0", "0. Delogare Operator (Revenire la meniul principal)");
        en.put("OPERATOR_OPT_0", "0. Logout Operator (Return to main menu)");

        ro.put("PROMPT_OLD_PASS", "Introduceți parola actuală de Operator: ");
        en.put("PROMPT_OLD_PASS", "Enter current Operator password: ");

        ro.put("PROMPT_NEW_PASS", "Introduceți noua parolă securizată: ");
        en.put("PROMPT_NEW_PASS", "Enter new secure password: ");

        ro.put("PROMPT_CONFIRM_PASS", "Confirmați noua parolă: ");
        en.put("PROMPT_CONFIRM_PASS", "Confirm new password: ");

        ro.put("SUCCESS_PASS_CHANGED", "\n[SUCCES] Noua parolă a fost actualizată cu succes!\n");
        en.put("SUCCESS_PASS_CHANGED", "\n[SUCCESS] New password has been successfully updated!\n");

        ro.put("ERR_PASS_MISMATCH", "[EROARE] Parolele introduse nu coincid! Reîncercați.\n");
        en.put("ERR_PASS_MISMATCH", "[ERROR] Entered passwords do not match! Please try again.\n");

        ro.put("PROMPT_PIN", "Introduceți codul PIN al contului (4 cifre): ");
        en.put("PROMPT_PIN", "Enter account PIN code (4 digits): ");

        ro.put("PROMPT_OPERATOR_PASS", "Introduceți parola securizată de Operator: ");
        en.put("PROMPT_OPERATOR_PASS", "Enter secure Operator password: ");

        ro.put("ERR_PIN_FORMAT", "[EROARE] Codul PIN trebuie să conțină exact 4 cifre numerice!\n");
        en.put("ERR_PIN_FORMAT", "[ERROR] PIN code must contain exactly 4 numeric digits!\n");

        ro.put("ERR_OPERATOR_PASS_INCORRECT", "[SECURITATE] Parolă de operator incorectă! Accesul la consola administrativă a fost refuzat.");
        en.put("ERR_OPERATOR_PASS_INCORRECT", "[SECURITY] Incorrect operator password! Access to admin console was denied.");

        ro.put("SUCCESS_PIN_CHANGED", "\n[SUCCES] Noul cod PIN a fost actualizat cu succes!\n");
        en.put("SUCCESS_PIN_CHANGED", "\n[SUCCESS] New PIN code has been successfully updated!\n");

        ro.put("PROMPT_NEW_PIN", "Introduceți noul cod PIN (4 cifre): ");
        en.put("PROMPT_NEW_PIN", "Enter new PIN code (4 digits): ");

        ro.put("PROMPT_CONFIRM_PIN", "Confirmați noul cod PIN: ");
        en.put("PROMPT_CONFIRM_PIN", "Confirm new PIN code: ");

        ro.put("ERR_PIN_MISMATCH", "[EROARE] Codurile PIN introduse nu coincid! Reîncercați.\n");
        en.put("ERR_PIN_MISMATCH", "[ERROR] Entered PIN codes do not match! Please try again.\n");

        ro.put("SUCCESS_LOGIN_CLIENT", "\n[AUTENTIFICARE REUȘITĂ] Bun venit, %s! Sunteți conectat la contul %s.\n");
        en.put("SUCCESS_LOGIN_CLIENT", "\n[LOGIN SUCCESSFUL] Welcome, %s! You are connected to account %s.\n");

        ro.put("SUCCESS_LOGIN_OPERATOR", "\n[AUTENTIFICARE REUȘITĂ] Acces autorizat în consola Operatorului Bancar.\n");
        en.put("SUCCESS_LOGIN_OPERATOR", "\n[LOGIN SUCCESSFUL] Authorized access granted to Bank Operator Console.\n");

        ro.put("LOGOUT_CLIENT_MSG", "\n[DELOGARE] V-ați deconectat cu succes din contul de client.\n");
        en.put("LOGOUT_CLIENT_MSG", "\n[LOGOUT] Successfully logged out of client account.\n");

        ro.put("LOGOUT_OPERATOR_MSG", "\n[DELOGARE] Sesiunea de operator bancar a fost închisă.\n");
        en.put("LOGOUT_OPERATOR_MSG", "\n[LOGOUT] Operator session successfully closed.\n");

        ro.put("OPT_1", "1. Vizualizare listă conturi bancare și solduri");
        en.put("OPT_1", "1. View bank accounts list and balances");

        ro.put("OPT_2", "2. Deschidere cont bancar nou");
        en.put("OPT_2", "2. Open new bank account");

        ro.put("OPT_3", "3. Depunere numerar");
        en.put("OPT_3", "3. Cash deposit");

        ro.put("OPT_4", "4. Retragere numerar");
        en.put("OPT_4", "4. Cash withdrawal");

        ro.put("OPT_5", "5. Transfer bancar interconturi (cu conversie valutară)");
        en.put("OPT_5", "5. Inter-account bank transfer (with currency conversion)");

        ro.put("OPT_6", "6. Plată factură utilități");
        en.put("OPT_6", "6. Utility bill payment");

        ro.put("OPT_7", "7. Istoric tranzacții (global sau filtrat după IBAN)");
        en.put("OPT_7", "7. Transaction history (global or filtered by IBAN)");

        ro.put("OPT_8", "8. Vizualizare statistici lunare (Matrice 2D fundamentală)");
        en.put("OPT_8", "8. View monthly statistics (Fundamental 2D matrix)");

        ro.put("OPT_9", "9. Generare raport statistic în fișier text");
        en.put("OPT_9", "9. Generate statistical report in text file");

        ro.put("OPT_10", "10. Salvare copie de siguranță binară (Backup .dat)");
        en.put("OPT_10", "10. Export binary backup copy (Audit .dat)");

        ro.put("OPT_11", "11. Restaurare bază de date din backup binar");
        en.put("OPT_11", "11. Restore database from binary backup");

        ro.put("OPT_12", "12. Blocare / Deblocare cont bancar");
        en.put("OPT_12", "12. Lock / Unlock bank account");

        ro.put("OPT_13", "13. Schimbare limbă / Change language (Română / English)");
        en.put("OPT_13", "13. Change language / Schimbare limbă (English / Română)");

        ro.put("OPT_0", " 0. Salvare date în fișiere și Ieșire din aplicație");
        en.put("OPT_0", " 0. Save data to files and Exit application");

        ro.put("PROMPT_SELECT_OPT", "Selectați opțiunea dorită: ");
        en.put("PROMPT_SELECT_OPT", "Select desired option: ");

        ro.put("PRESS_ENTER", "Apăsați ENTER pentru a reveni la meniul principal...");
        en.put("PRESS_ENTER", "Press ENTER to return to the main menu...");

        ro.put("CANCEL_NOTE", "(Notă: Puteți tasta '0' în orice moment pentru a anula operațiunea)\n");
        en.put("CANCEL_NOTE", "(Note: You can type '0' at any time to cancel the operation)\n");

        ro.put("OP_CANCELLED", "Operațiunea a fost anulată de utilizator.");
        en.put("OP_CANCELLED", "Operation was cancelled by user.");

        // =====================================================================
        // ERORI GENERALE DE INPUT CONSOLA
        // =====================================================================
        ro.put("ERR_FIELD_EMPTY", "[EROARE] Câmpul nu poate fi lăsat gol! Reintroduceți sau tastați 0 pentru a anula: ");
        en.put("ERR_FIELD_EMPTY", "[ERROR] Field cannot be left empty! Please re-enter or type 0 to cancel: ");

        ro.put("ERR_NOT_INT", "[EROARE] Format invalid! Introduceți un număr întreg.");
        en.put("ERR_NOT_INT", "[ERROR] Invalid format! Please enter an integer number.");

        ro.put("ERR_INT_RANGE", "[EROARE] Valoarea trebuie să fie un număr între %d și %d!\n");
        en.put("ERR_INT_RANGE", "[ERROR] Value must be an integer between %d and %d!\n");

        ro.put("ERR_NUMERIC_FORMAT", "[EROARE] Format numeric invalid! Exemplu: 500 sau 1250.75");
        en.put("ERR_NUMERIC_FORMAT", "[ERROR] Invalid numeric format! Example: 500 or 1250.75");

        ro.put("ERR_BANKING_HEADER", "[EROARE BANCARĂ] ");
        en.put("ERR_BANKING_HEADER", "[BANKING ERROR] ");

        ro.put("ERR_SYSTEM_HEADER", "[EROARE DE SISTEM] A apărut o eroare neașteptată: ");
        en.put("ERR_SYSTEM_HEADER", "[SYSTEM ERROR] An unexpected error occurred: ");

        ro.put("STATUS_CANCELLED_HEADER", "[ANULAT] ");
        en.put("STATUS_CANCELLED_HEADER", "[CANCELLED] ");

        // =====================================================================
        // OPTIUNEA 1: VIZUALIZARE CONTURI SI SOLDURI
        // =====================================================================
        ro.put("ACCOUNTS_TITLE", "--- LISTĂ CONTURI BANCARE ÎNREGISTRATE ---");
        en.put("ACCOUNTS_TITLE", "--- REGISTERED BANK ACCOUNTS LIST ---");

        ro.put("NO_ACCOUNTS", "Nu există niciun cont înregistrat în sistem.");
        en.put("NO_ACCOUNTS", "No bank accounts are registered in the system.");

        ro.put("TOTAL_FUNDS_RON", "Total fonduri bancă (echivalent RON): %,12.2f lei\n");
        en.put("TOTAL_FUNDS_RON", "Total bank funds (RON equivalent): %,12.2f lei\n");

        ro.put("TOTAL_FUNDS_EUR", "Total fonduri bancă (echivalent EUR): %,12.2f €\n");
        en.put("TOTAL_FUNDS_EUR", "Total bank funds (EUR equivalent): %,12.2f €\n");

        // =====================================================================
        // OPTIUNEA 2: DESCHIDERE CONT NOU
        // =====================================================================
        ro.put("OPEN_ACC_TITLE", "--- DESCHIDERE CONT BANCAR NOU ---");
        en.put("OPEN_ACC_TITLE", "--- OPEN NEW BANK ACCOUNT ---");

        ro.put("PROMPT_IBAN_NEW", "Introduceți IBAN-ul contului (24 caractere pt RO, max 34 internațional): ");
        en.put("PROMPT_IBAN_NEW", "Enter account IBAN (24 chars for RO, max 34 international): ");

        ro.put("PROMPT_HOLDER_NAME", "Nume și prenume titular: ");
        en.put("PROMPT_HOLDER_NAME", "Account holder full name: ");

        ro.put("PROMPT_CNP", "CNP titular (13 cifre): ");
        en.put("PROMPT_CNP", "Personal ID / CNP code (13 digits): ");

        ro.put("PROMPT_EMAIL", "Adresă e-mail: ");
        en.put("PROMPT_EMAIL", "Email address: ");

        ro.put("PROMPT_PHONE", "Număr de telefon (minim 10 cifre): ");
        en.put("PROMPT_PHONE", "Phone number (min 10 digits): ");

        ro.put("ACC_TYPES_MENU", "Tipuri de cont disponibile: 1. CURENT | 2. ECONOMII | 3. DEPOZIT | 0. Anulare");
        en.put("ACC_TYPES_MENU", "Available account types: 1. CHECKING | 2. SAVINGS | 3. DEPOSIT | 0. Cancel");

        ro.put("PROMPT_SELECT_ACC_TYPE", "Selectați tipul contului (1-3 sau 0 pt anulare): ");
        en.put("PROMPT_SELECT_ACC_TYPE", "Select account type (1-3 or 0 to cancel): ");

        ro.put("CURRENCIES_MENU", "Monede disponibile: 1. RON | 2. EUR | 3. USD | 4. GBP | 0. Anulare");
        en.put("CURRENCIES_MENU", "Available currencies: 1. RON | 2. EUR | 3. USD | 4. GBP | 0. Cancel");

        ro.put("PROMPT_SELECT_CURRENCY", "Selectați moneda (1-4 sau 0 pt anulare): ");
        en.put("PROMPT_SELECT_CURRENCY", "Select currency (1-4 or 0 to cancel): ");

        ro.put("PROMPT_INIT_BALANCE", "Sold inițial depus (minim 0, sau apăsați ENTER pt 0): ");
        en.put("PROMPT_INIT_BALANCE", "Initial deposit balance (min 0, or press ENTER for 0): ");

        ro.put("SUCCESS_OPEN_ACC", "\n[SUCCES] Contul a fost deschis cu succes în memorie pentru %s!\nIBAN: %s | Tip: %s | Monedă: %s | Sold inițial: %,.2f %s\n");
        en.put("SUCCESS_OPEN_ACC", "\n[SUCCESS] Bank account successfully opened in memory for %s!\nIBAN: %s | Type: %s | Currency: %s | Initial balance: %,.2f %s\n");

        // =====================================================================
        // OPTIUNEA 3: DEPUNERE NUMERAR
        // =====================================================================
        ro.put("DEPOSIT_TITLE", "--- DEPUNERE NUMERAR ---");
        en.put("DEPOSIT_TITLE", "--- CASH DEPOSIT ---");

        ro.put("PROMPT_IBAN_ACCOUNT", "Introduceți IBAN-ul contului: ");
        en.put("PROMPT_IBAN_ACCOUNT", "Enter account IBAN: ");

        ro.put("PROMPT_AMOUNT_DEPOSIT", "Sumă de depus: ");
        en.put("PROMPT_AMOUNT_DEPOSIT", "Deposit amount: ");

        ro.put("PROMPT_DESC_DEPOSIT", "Detalii / Descriere depunere (apăsați ENTER pt 'Depunere numerar'): ");
        en.put("PROMPT_DESC_DEPOSIT", "Deposit details / description (press ENTER for 'Cash deposit'): ");

        ro.put("DEFAULT_DESC_DEPOSIT", "Depunere numerar");
        en.put("DEFAULT_DESC_DEPOSIT", "Cash deposit");

        ro.put("SUCCESS_DEPOSIT", "\n[SUCCES] Depunere finalizată cu succes!\nTranzacție ID: %s | Sumă depusă: %,.2f %s | Sold actual: %,.2f %s\n");
        en.put("SUCCESS_DEPOSIT", "\n[SUCCESS] Cash deposit completed successfully!\nTransaction ID: %s | Amount deposited: %,.2f %s | Current balance: %,.2f %s\n");

        // =====================================================================
        // OPTIUNEA 4: RETRAGERE NUMERAR
        // =====================================================================
        ro.put("WITHDRAW_TITLE", "--- RETRAGERE NUMERAR ---");
        en.put("WITHDRAW_TITLE", "--- CASH WITHDRAWAL ---");

        ro.put("PROMPT_AMOUNT_WITHDRAW", "Sumă de retras: ");
        en.put("PROMPT_AMOUNT_WITHDRAW", "Withdrawal amount: ");

        ro.put("PROMPT_DESC_WITHDRAW", "Detalii / Descriere retragere (apăsați ENTER pt 'Retragere numerar'): ");
        en.put("PROMPT_DESC_WITHDRAW", "Withdrawal details / description (press ENTER for 'Cash withdrawal'): ");

        ro.put("DEFAULT_DESC_WITHDRAW", "Retragere numerar");
        en.put("DEFAULT_DESC_WITHDRAW", "Cash withdrawal");

        ro.put("SUCCESS_WITHDRAW", "\n[SUCCES] Retragere finalizată cu succes!\nTranzacție ID: %s | Sumă: %,.2f %s | Comision: %,.2f %s | Sold rămas: %,.2f %s\n");
        en.put("SUCCESS_WITHDRAW", "\n[SUCCESS] Cash withdrawal completed successfully!\nTransaction ID: %s | Amount: %,.2f %s | Fee: %,.2f %s | Remaining balance: %,.2f %s\n");

        // =====================================================================
        // OPTIUNEA 5: TRANSFER BANCAR
        // =====================================================================
        ro.put("TRANSFER_TITLE", "--- TRANSFER BANCAR INTERCONTURI ---");
        en.put("TRANSFER_TITLE", "--- INTER-ACCOUNT BANK TRANSFER ---");

        ro.put("PROMPT_IBAN_SOURCE", "IBAN cont debitor (Sursă): ");
        en.put("PROMPT_IBAN_SOURCE", "Debtor account IBAN (Source): ");

        ro.put("PROMPT_IBAN_DEST", "IBAN cont creditor (Destinație): ");
        en.put("PROMPT_IBAN_DEST", "Creditor account IBAN (Destination): ");

        ro.put("ERR_SAME_ACCOUNT", "[EROARE] Contul destinație nu poate fi identic cu contul sursă!\n-> Vă rugăm să introduceți un IBAN de destinație diferit sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_SAME_ACCOUNT", "[ERROR] Destination account cannot be identical to source account!\n-> Please enter a different destination IBAN or type 0 to cancel the operation.\n");

        ro.put("PROMPT_AMOUNT_TRANSFER", "Sumă de transferat (în %s): ");
        en.put("PROMPT_AMOUNT_TRANSFER", "Transfer amount (in %s): ");

        ro.put("PROMPT_DESC_TRANSFER", "Detalii transfer (apăsați ENTER pt 'Transfer bancar interconturi'): ");
        en.put("PROMPT_DESC_TRANSFER", "Transfer details (press ENTER for 'Inter-account bank transfer'): ");

        ro.put("DEFAULT_DESC_TRANSFER", "Transfer bancar interconturi");
        en.put("DEFAULT_DESC_TRANSFER", "Inter-account bank transfer");

        ro.put("SUCCESS_TRANSFER", "\n[SUCCES] Transfer bancar procesat!\nTranzacție: %s | Comision: %,.2f %s\nSold cont sursă (%s): %,.2f %s | Sold cont destinație (%s): %,.2f %s\n");
        en.put("SUCCESS_TRANSFER", "\n[SUCCESS] Bank transfer processed successfully!\nTransaction: %s | Fee: %,.2f %s\nSource balance (%s): %,.2f %s | Destination balance (%s): %,.2f %s\n");

        // =====================================================================
        // OPTIUNEA 6: PLATA FACTURA
        // =====================================================================
        ro.put("BILL_TITLE", "--- PLATĂ FACTURĂ UTILITĂȚI ---");
        en.put("BILL_TITLE", "--- UTILITY BILL PAYMENT ---");

        ro.put("PROMPT_IBAN_PAYER", "IBAN cont plătitor: ");
        en.put("PROMPT_IBAN_PAYER", "Payer account IBAN: ");

        ro.put("PROMPT_PROVIDER", "Denumire furnizor (ex: Enel, Digi, Engie, Hidroelectrica): ");
        en.put("PROMPT_PROVIDER", "Utility provider name (e.g.: Enel, Digi, Engie, Power Grid): ");

        ro.put("PROMPT_BILL_NUM", "Număr factură: ");
        en.put("PROMPT_BILL_NUM", "Invoice / Bill number: ");

        ro.put("PROMPT_BILL_AMOUNT", "Sumă factură: ");
        en.put("PROMPT_BILL_AMOUNT", "Bill amount: ");

        ro.put("SUCCESS_BILL", "\n[SUCCES] Factura a fost achitată!\nID Tranzacție: %s | Furnizor: %s | Sumă: %,.2f %s | Sold rămas: %,.2f %s\n");
        en.put("SUCCESS_BILL", "\n[SUCCESS] Utility bill has been paid!\nTransaction ID: %s | Provider: %s | Amount: %,.2f %s | Remaining balance: %,.2f %s\n");

        // =====================================================================
        // OPTIUNEA 7: ISTORIC TRANZACTII
        // =====================================================================
        ro.put("HISTORY_TITLE", "--- ISTORIC TRANZACȚII BANCAR ---");
        en.put("HISTORY_TITLE", "--- BANK TRANSACTION HISTORY ---");

        ro.put("HISTORY_OPT_ALL", "  1. Afișare toate tranzacțiile din sistem");
        en.put("HISTORY_OPT_ALL", "  1. Display all transactions in system");

        ro.put("HISTORY_OPT_FILTER", "  2. Filtrare tranzacții după un IBAN");
        en.put("HISTORY_OPT_FILTER", "  2. Filter transactions by IBAN");

        ro.put("HISTORY_OPT_CANCEL", "  0. Anulare / Revenire la meniul principal");
        en.put("HISTORY_OPT_CANCEL", "  0. Cancel / Return to main menu");

        ro.put("PROMPT_HISTORY_MODE", "Selectați modul de afișare (0-2): ");
        en.put("PROMPT_HISTORY_MODE", "Select display mode (0-2): ");

        ro.put("HISTORY_CANCELLED", "Vizualizarea istoricului a fost anulată.");
        en.put("HISTORY_CANCELLED", "Transaction history viewing was cancelled.");

        ro.put("PROMPT_IBAN_FILTER", "Introduceți IBAN-ul pentru filtrare: ");
        en.put("PROMPT_IBAN_FILTER", "Enter IBAN for filtering: ");

        ro.put("NO_TRANSACTIONS", "\nNu există nicio tranzacție înregistrată pentru criteriul selectat.");
        en.put("NO_TRANSACTIONS", "\nNo transactions found matching the selected criteria.");

        ro.put("HISTORY_RESULTS", "\nRezultate (%d tranzacții):\n");
        en.put("HISTORY_RESULTS", "\nResults (%d transactions):\n");

        // =====================================================================
        // OPTIUNEA 8: MATRICE 2D STATISTICA
        // =====================================================================
        ro.put("MATRIX_TITLE", "--- VIZUALIZARE MATRICE 2D STATISTICĂ TRANZACȚII (ECHIVALENT RON) ---");
        en.put("MATRIX_TITLE", "--- VIEW 2D TRANSACTION STATISTICAL MATRIX (RON EQUIVALENT) ---");

        ro.put("MATRIX_COL_MONTH", "Lună");
        en.put("MATRIX_COL_MONTH", "Month");

        ro.put("MATRIX_COL_DEP", "DEPUNERI");
        en.put("MATRIX_COL_DEP", "DEPOSITS");

        ro.put("MATRIX_COL_WITH", "RETRAGERI");
        en.put("MATRIX_COL_WITH", "WITHDRAWALS");

        ro.put("MATRIX_COL_TRANSF", "TRANSFERURI");
        en.put("MATRIX_COL_TRANSF", "TRANSFERS");

        ro.put("MATRIX_COL_BILLS", "FACTURI");
        en.put("MATRIX_COL_BILLS", "BILLS");

        ro.put("MATRIX_NOTE", "* Notă: Formatul celulei este 'Sumă (Număr operațiuni)'");
        en.put("MATRIX_NOTE", "* Note: Cell format is 'Amount (Number of operations)'");

        // =====================================================================
        // OPTIUNEA 9: RAPORT STATISTIC TEXT
        // =====================================================================
        ro.put("REPORT_TITLE", "--- GENERARE RAPORT STATISTIC FINANCIAR ---");
        en.put("REPORT_TITLE", "--- GENERATE FINANCIAL STATISTICAL REPORT ---");

        ro.put("SUCCESS_REPORT", "[SUCCES] Raportul statistic a fost generat în fișierul: %s\n");
        en.put("SUCCESS_REPORT", "[SUCCESS] Statistical report successfully generated in file: %s\n");

        ro.put("ERR_REPORT", "[EROARE] Nu s-a putut genera raportul: %s\n");
        en.put("ERR_REPORT", "[ERROR] Could not generate report: %s\n");

        // =====================================================================
        // OPTIUNEA 10 & 11: BACKUP BINAR
        // =====================================================================
        ro.put("BACKUP_EXPORT_TITLE", "--- EXPORT COPIE DE SIGURANȚĂ BINARĂ (AUDIT) ---");
        en.put("BACKUP_EXPORT_TITLE", "--- EXPORT BINARY AUDIT BACKUP COPY ---");

        ro.put("SUCCESS_BACKUP_EXPORT", "[SUCCES] Backup-ul binar complet a fost salvat în: %s\n");
        en.put("SUCCESS_BACKUP_EXPORT", "[SUCCESS] Complete binary backup saved to: %s\n");

        ro.put("ERR_BACKUP_EXPORT", "[EROARE] Nu s-a putut crea backup-ul binar: %s\n");
        en.put("ERR_BACKUP_EXPORT", "[ERROR] Could not create binary backup: %s\n");

        ro.put("BACKUP_RESTORE_TITLE", "--- RESTAURARE DATE DIN BACKUP BINAR ---");
        en.put("BACKUP_RESTORE_TITLE", "--- RESTORE DATABASE FROM BINARY BACKUP ---");

        ro.put("WARN_BACKUP_RESTORE", "ATENȚIE: Toate datele curente din memorie vor fi înlocuite cu cele din backup. Continuați? (da / nu sau 0 pt anulare): ");
        en.put("WARN_BACKUP_RESTORE", "WARNING: All current memory data will be overwritten with backup data. Continue? (yes / no or 0 to cancel): ");

        ro.put("BACKUP_RESTORE_CANCELLED", "Restaurarea din backup a fost anulată.");
        en.put("BACKUP_RESTORE_CANCELLED", "Database restoration from backup was cancelled.");

        ro.put("BACKUP_RESTORE_NOT_CONFIRMED", "Răspuns diferit de 'da'. Operațiune anulată.");
        en.put("BACKUP_RESTORE_NOT_CONFIRMED", "Response different from 'yes'. Operation cancelled.");

        ro.put("SUCCESS_BACKUP_RESTORE", "[SUCCES] Baza de date din memorie a fost restaurată cu succes din backup!\n");
        en.put("SUCCESS_BACKUP_RESTORE", "[SUCCESS] In-memory database successfully restored from backup!\n");

        ro.put("ERR_BACKUP_RESTORE", "[EROARE] Restaurarea din backup a eșuat: %s\n");
        en.put("ERR_BACKUP_RESTORE", "[ERROR] Database restoration from backup failed: %s\n");

        // =====================================================================
        // OPTIUNEA 12: BLOCARE / DEBLOCARE CONT
        // =====================================================================
        ro.put("LOCK_TITLE", "--- BLOCARE / DEBLOCARE CONT BANCAR ---");
        en.put("LOCK_TITLE", "--- LOCK / UNLOCK BANK ACCOUNT ---");

        ro.put("CURRENT_STATE", "Starea actuală a contului: %s\n");
        en.put("CURRENT_STATE", "Current account status: %s\n");

        ro.put("PROMPT_LOCK_ACTION", "Doriți să %s contul? (da/nu sau 0 pentru anulare): ");
        en.put("PROMPT_LOCK_ACTION", "Do you want to %s the account? (yes/no or 0 to cancel): ");

        ro.put("ACTION_LOCK", "BLOCAȚI");
        en.put("ACTION_LOCK", "LOCK");

        ro.put("ACTION_UNLOCK", "ACTIVATI");
        en.put("ACTION_UNLOCK", "UNLOCK");

        ro.put("STATE_ACTIVE", "ACTIV");
        en.put("STATE_ACTIVE", "ACTIVE");

        ro.put("STATE_LOCKED", "BLOCAT");
        en.put("STATE_LOCKED", "LOCKED");

        ro.put("SUCCESS_LOCK_UPDATE", "[SUCCES] Starea contului a fost actualizată: %s\n");
        en.put("SUCCESS_LOCK_UPDATE", "[SUCCESS] Account status updated to: %s\n");

        ro.put("NO_CHANGE", "Nicio modificare efectuată.");
        en.put("NO_CHANGE", "No changes performed.");

        // =====================================================================
        // OPTIUNEA 13: SCHIMBARE LIMBA
        // =====================================================================
        ro.put("LANG_TITLE", "--- SCHIMBARE LIMBĂ / CHANGE LANGUAGE ---");
        en.put("LANG_TITLE", "--- CHANGE LANGUAGE / SCHIMBARE LIMBĂ ---");

        ro.put("LANG_CURRENT", "Limba curentă / Current language: %s\n");
        en.put("LANG_CURRENT", "Current language / Limba curentă: %s\n");

        ro.put("LANG_OPT_1", "  1. Română (cu diacritice complete: ă, â, î, ș, ț)");
        en.put("LANG_OPT_1", "  1. Română (cu diacritice complete: ă, â, î, ș, ț)");

        ro.put("LANG_OPT_2", "  2. English (International / US)");
        en.put("LANG_OPT_2", "  2. English (International / US)");

        ro.put("LANG_OPT_0", "  0. Anulare / Cancel");
        en.put("LANG_OPT_0", "  0. Cancel / Anulare");

        ro.put("PROMPT_LANG_SELECT", "Selectați limba dorită / Select desired language (1-2 sau 0): ");
        en.put("PROMPT_LANG_SELECT", "Select desired language / Selectați limba dorită (1-2 or 0): ");

        ro.put("SUCCESS_LANG_RO", "[SUCCES] Limba sistemului a fost setată în Română (cu suport complet de diacritice)!");
        en.put("SUCCESS_LANG_RO", "[SUCCES] Limba sistemului a fost setată în Română (cu suport complet de diacritice)!");

        ro.put("SUCCESS_LANG_EN", "[SUCCESS] System language has been switched to English!");
        en.put("SUCCESS_LANG_EN", "[SUCCESS] System language has been switched to English!");

        // =====================================================================
        // OPTIUNEA 0: SALVARE SI IESIRE
        // =====================================================================
        ro.put("EXIT_TITLE", "--- SALVARE DATE ȘI ÎNCHIDERE APLICAȚIE ---");
        en.put("EXIT_TITLE", "--- SAVE DATA AND EXIT APPLICATION ---");

        ro.put("SAVING_ACCOUNTS", "Salvare conturi actualizate în %s...\n");
        en.put("SAVING_ACCOUNTS", "Saving updated accounts to %s...\n");

        ro.put("SAVING_TRANSACTIONS", "Salvare tranzacții actualizate în %s...\n");
        en.put("SAVING_TRANSACTIONS", "Saving updated transactions to %s...\n");

        ro.put("SUCCESS_SAVED", "[SUCCES] Toate modificările din memorie au fost persistate cu succes pe disc!\n");
        en.put("SUCCESS_SAVED", "[SUCCESS] All memory changes have been successfully persisted to disk!\n");

        ro.put("GOODBYE", "Vă mulțumim că ați utilizat Sistemul de E-Banking PPOO. La revedere!");
        en.put("GOODBYE", "Thank you for using the PPOO E-Banking System. Goodbye!");

        // =====================================================================
        // VALIDARI DEFENSIVE IMEDIATE SI MESAJE DE EROARE
        // =====================================================================
        ro.put("ERR_IBAN_INVALID", "[EROARE] Format IBAN invalid: %s\n-> Vă rugăm să reintroduceți un IBAN valid sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_IBAN_INVALID", "[ERROR] Invalid IBAN format: %s\n-> Please re-enter a valid IBAN or type 0 to cancel the operation.\n");

        ro.put("ERR_IBAN_NOT_FOUND", "[EROARE] Contul bancar cu IBAN-ul '%s' nu a fost găsit în evidențele băncii.\n-> Vă rugăm să reintroduceți un IBAN existent sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_IBAN_NOT_FOUND", "[ERROR] Bank account with IBAN '%s' was not found in bank records.\n-> Please re-enter an existing IBAN or type 0 to cancel the operation.\n");

        ro.put("ERR_IBAN_BLOCKED", "[EROARE] Contul bancar '%s' (%s) este BLOCAT. Tranzacțiile financiare nu sunt permise pe un cont blocat.\n-> Vă rugăm să introduceți un alt cont activ sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_IBAN_BLOCKED", "[ERROR] Bank account '%s' (%s) is LOCKED. Financial transactions are not allowed on a locked account.\n-> Please enter an active account or type 0 to cancel the operation.\n");

        ro.put("INFO_ACCOUNT_FOUND", "[INFO] Cont găsit: %s | Titular: %s | Sold: %,.2f %s (%s)\n");
        en.put("INFO_ACCOUNT_FOUND", "[INFO] Account found: %s | Holder: %s | Balance: %,.2f %s (%s)\n");

        ro.put("ERR_IBAN_EXISTS", "[EROARE] Există deja un cont înregistrat cu IBAN-ul '%s' (Titular: %s).\n-> Vă rugăm să introduceți un IBAN unic sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_IBAN_EXISTS", "[ERROR] An account is already registered with IBAN '%s' (Holder: %s).\n-> Please enter a unique IBAN or type 0 to cancel the operation.\n");

        ro.put("ERR_NAME_INVALID", "[EROARE] Numele titularului trebuie să conțină minim 3 caractere și să fie alcătuit din litere.\n-> Vă rugăm să reintroduceți numele sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_NAME_INVALID", "[ERROR] Account holder name must contain at least 3 characters and consist of letters.\n-> Please re-enter the name or type 0 to cancel the operation.\n");

        ro.put("ERR_CNP_INVALID", "[EROARE] CNP invalid! Trebuie să conțină exact 13 cifre numerice (exemplu: 1950315123456).\n-> Vă rugăm să reintroduceți CNP-ul sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_CNP_INVALID", "[ERROR] Invalid ID code! Must contain exactly 13 digits (example: 1950315123456).\n-> Please re-enter the personal ID code or type 0 to cancel the operation.\n");

        ro.put("ERR_EMAIL_INVALID", "[EROARE] Adresa de e-mail este invalidă! Format așteptat: utilizator@domeniu.ext\n-> Vă rugăm să reintroduceți adresa de e-mail sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_EMAIL_INVALID", "[ERROR] Email address is invalid! Expected format: user@domain.ext\n-> Please re-enter the email address or type 0 to cancel the operation.\n");

        ro.put("ERR_PHONE_INVALID", "[EROARE] Numărul de telefon este invalid! Trebuie să conțină minim 10 cifre numerice (exemplu: 0722123456).\n-> Vă rugăm să reintroduceți numărul sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_PHONE_INVALID", "[ERROR] Phone number is invalid! Must contain at least 10 digits (example: 0722123456).\n-> Please re-enter the phone number or type 0 to cancel the operation.\n");

        ro.put("ERR_AMOUNT_POSITIVE_DEP", "[EROARE] Suma de depus trebuie să fie un număr strict pozitiv (> 0)!\n-> Vă rugăm să reintroduceți suma sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_AMOUNT_POSITIVE_DEP", "[ERROR] Deposit amount must be a strictly positive number (> 0)!\n-> Please re-enter the amount or type 0 to cancel the operation.\n");

        ro.put("ERR_AMOUNT_POSITIVE_WITH", "[EROARE] Suma de retras trebuie să fie strict pozitivă (> 0)!\n-> Vă rugăm să reintroduceți suma sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_AMOUNT_POSITIVE_WITH", "[ERROR] Withdrawal amount must be a strictly positive number (> 0)!\n-> Please re-enter the amount or type 0 to cancel the operation.\n");

        ro.put("ERR_AMOUNT_POSITIVE_TRANSF", "[EROARE] Suma de transferat trebuie să fie strict pozitivă (> 0)!\n-> Vă rugăm să reintroduceți suma sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_AMOUNT_POSITIVE_TRANSF", "[ERROR] Transfer amount must be a strictly positive number (> 0)!\n-> Please re-enter the amount or type 0 to cancel the operation.\n");

        ro.put("ERR_AMOUNT_POSITIVE_BILL", "[EROARE] Suma facturii trebuie să fie strict pozitivă (> 0)!\n-> Vă rugăm să reintroduceți suma sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_AMOUNT_POSITIVE_BILL", "[ERROR] Bill amount must be a strictly positive number (> 0)!\n-> Please re-enter the amount or type 0 to cancel the operation.\n");

        ro.put("INFO_CURRENT_BALANCE", "[INFO] Sold curent disponibil: %,.2f %s\n");
        en.put("INFO_CURRENT_BALANCE", "[INFO] Current available balance: %,.2f %s\n");

        ro.put("INFO_SOURCE_BALANCE", "[INFO] Sold disponibil în contul sursă: %,.2f %s\n");
        en.put("INFO_SOURCE_BALANCE", "[INFO] Available balance in source account: %,.2f %s\n");

        ro.put("ERR_INSUFFICIENT_FUNDS_WITHDRAW", "[EROARE] Fonduri insuficiente! Pentru a retrage %,.2f %s este necesar un total de %,.2f %s (inclusiv comision 0.2%%: %,.2f %s), dar soldul disponibil este de doar %,.2f %s.\n-> Vă rugăm să introduceți o sumă mai mică sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_INSUFFICIENT_FUNDS_WITHDRAW", "[ERROR] Insufficient funds! To withdraw %,.2f %s a total of %,.2f %s is required (including 0.2%% fee: %,.2f %s), but available balance is only %,.2f %s.\n-> Please enter a lower amount or type 0 to cancel the operation.\n");

        ro.put("ERR_INSUFFICIENT_FUNDS_TRANSFER", "[EROARE] Fonduri insuficiente în contul sursă! Necesar: %,.2f %s (suma %,.2f + comision %,.2f), sold disponibil: %,.2f %s.\n-> Vă rugăm să introduceți o sumă mai mică sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_INSUFFICIENT_FUNDS_TRANSFER", "[ERROR] Insufficient funds in source account! Required: %,.2f %s (amount %,.2f + fee %,.2f), available balance: %,.2f %s.\n-> Please enter a lower amount or type 0 to cancel the operation.\n");

        ro.put("ERR_INSUFFICIENT_FUNDS_BILL", "[EROARE] Fonduri insuficiente! Suma facturii (%,.2f %s) depășește soldul disponibil (%,.2f %s).\n-> Vă rugăm să introduceți o sumă validă sau tastați 0 pentru a anula operațiunea.\n");
        en.put("ERR_INSUFFICIENT_FUNDS_BILL", "[ERROR] Insufficient funds! Bill amount (%,.2f %s) exceeds available balance (%,.2f %s).\n-> Please enter a valid amount or type 0 to cancel the operation.\n");

        ro.put("ERR_INIT_BALANCE_NEGATIVE", "[EROARE] Soldul inițial nu poate fi negativ!\n-> Vă rugăm să reintroduceți o valoare >= 0 sau tastați 0 pt sold 0, ori 'cancel' pt anulare.\n");
        en.put("ERR_INIT_BALANCE_NEGATIVE", "[ERROR] Initial balance cannot be negative!\n-> Please re-enter a value >= 0 or type 0 for 0 balance, or 'cancel' to abort.\n");

        MESAJE.put(Limba.ROMANA, ro);
        MESAJE.put(Limba.ENGLEZA, en);
    }

    private I18n() {
        // Constructor privat pentru a preveni instantierea
    }

    public static Limba getLimba() {
        return limbaCurenta;
    }

    public static void setLimba(Limba limba) {
        if (limba != null) {
            limbaCurenta = limba;
        }
    }

    /**
     * Returneaza textul asociat cheii in limba activa curenta.
     * Daca sunt furnizate argumente, textul este formatat conform acestora.
     */
    public static String t(String cheie, Object... args) {
        Map<String, String> dictionar = MESAJE.get(limbaCurenta);
        String valoare = (dictionar != null) ? dictionar.get(cheie) : null;
        if (valoare == null) {
            valoare = MESAJE.get(Limba.ROMANA).get(cheie);
        }
        if (valoare == null) {
            return "[" + cheie + "]";
        }
        if (args != null && args.length > 0) {
            Locale loc = (limbaCurenta == Limba.ROMANA) ? Locale.forLanguageTag("ro-RO") : Locale.US;
            return String.format(loc, valoare, args);
        }
        return valoare;
    }

    /**
     * Returneaza un Locale potrivit pentru formatarea numerelor si datelor calendaristice.
     */
    public static Locale getLocale() {
        return (limbaCurenta == Limba.ROMANA) ? Locale.forLanguageTag("ro-RO") : Locale.US;
    }
}
