import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Proof of Concept MVP for the WalletLedger component.
 *
 * Goals for this file 1 Select one component idea and justify it with real
 * world factors
 *
 * 2 Implement several different methods to prove feasibility 3 Provide a main
 * method that shows client style usage and value
 *
 *
 * Author : Isiaka Kebe
 */
public final class WalletLedgerMVP {

    /*
     * Conceptual Evaluation
     *
     * I selected WalletLedger because it matches my interests in fintech
     * products and it is feasible to implement in one semester. It also
     * demonstrates real world constraints that matter in production systems.
     *
     * Real world factors considered - Time and scope I can implement and test
     * this quickly as an MVP - Reliability finance needs correct balances and
     * consistent history - Safety I must prevent negative balances and invalid
     * transactions - Precision I use integer cents to avoid floating point
     * rounding bugs - Reuse I can reuse this inside my Super Money Transfer app
     * later - Humility I am still learning the OSU discipline so I am proving
     * the idea first with a single file MVP before spreading it across
     * interfaces and abstract classes
     */

    public enum EntryType {
        CREDIT, DEBIT
    }

    private static final class LedgerEntry {
        private final String id;
        private final int amountCents;
        private final String currency;
        private final EntryType type;
        private final long createdAtMillis;

        private LedgerEntry(String id, int amountCents, String currency,
                EntryType type) {
            this.id = id;
            this.amountCents = amountCents;
            this.currency = currency;
            this.type = type;
            this.createdAtMillis = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "LedgerEntry{id='" + this.id + "', " + this.type + " "
                    + this.amountCents + " " + this.currency + ", createdAt="
                    + this.createdAtMillis + "}";
        }
    }

    /*
     * Representation
     *
     * - entriesById provides fast lookup and removal by id - insertion order is
     * preserved so history feels natural to a client - we store amounts in
     * cents to keep math exact
     */
    private final Map<String, LedgerEntry> entriesById = new LinkedHashMap<>();
    private long nextTransactionNumber = 1;

    /*
     * Kernel style methods from my design
     */

    public void addEntry(String id, int amountCents, String currency,
            EntryType type) {
        validateId(id);
        validateCurrency(currency);
        validateAmountPositive(amountCents);
        Objects.requireNonNull(type, "type must not be null");

        assert !this.entriesById.containsKey(id) : "Duplicate entry id: " + id;

        LedgerEntry entry = new LedgerEntry(id, amountCents, currency, type);
        this.entriesById.put(id, entry);
    }

    public void removeEntry(String id) {
        validateId(id);
        LedgerEntry removed = this.entriesById.remove(id);
        assert removed != null : "No entry found for id: " + id;
    }

    public int entryCount() {
        return this.entriesById.size();
    }

    public LedgerEntry removeAnyEntry() {
        assert !this.entriesById.isEmpty() : "Ledger is empty";
        String firstKey = this.entriesById.keySet().iterator().next();
        return this.entriesById.remove(firstKey);
    }

    public void clear() {
        this.entriesById.clear();
        this.nextTransactionNumber = 1;
    }

    /*
     * Secondary style methods from my design
     */

    public int balanceCents(String currency) {
        validateCurrency(currency);

        int balance = 0;

        List<LedgerEntry> temp = new ArrayList<>();
        while (!this.entriesById.isEmpty()) {
            LedgerEntry e = this.removeAnyEntry();
            temp.add(e);

            if (e.currency.equals(currency)) {
                if (e.type == EntryType.CREDIT) {
                    balance += e.amountCents;
                } else {
                    balance -= e.amountCents;
                }
            }
        }

        for (LedgerEntry e : temp) {
            this.entriesById.put(e.id, e);
        }

        return balance;
    }

    public boolean hasSufficientFunds(int debitCents, String currency) {
        validateCurrency(currency);
        validateAmountPositive(debitCents);
        return this.balanceCents(currency) >= debitCents;
    }

    public void deposit(int amountCents, String currency) {
        validateCurrency(currency);
        validateAmountPositive(amountCents);
        String id = this.nextTransactionId("dep");
        this.addEntry(id, amountCents, currency, EntryType.CREDIT);
    }

    public void withdraw(int amountCents, String currency) {
        validateCurrency(currency);
        validateAmountPositive(amountCents);

        assert this.hasSufficientFunds(amountCents,
                currency) : "Insufficient funds for " + amountCents + " "
                        + currency;

        String id = this.nextTransactionId("wd");
        this.addEntry(id, amountCents, currency, EntryType.DEBIT);
    }

    public int totalCreditsCents(String currency) {
        validateCurrency(currency);

        int total = 0;

        List<LedgerEntry> temp = new ArrayList<>();
        while (!this.entriesById.isEmpty()) {
            LedgerEntry e = this.removeAnyEntry();
            temp.add(e);

            if (e.currency.equals(currency) && e.type == EntryType.CREDIT) {
                total += e.amountCents;
            }
        }

        for (LedgerEntry e : temp) {
            this.entriesById.put(e.id, e);
        }

        return total;
    }

    public int totalDebitsCents(String currency) {
        validateCurrency(currency);

        int total = 0;

        List<LedgerEntry> temp = new ArrayList<>();
        while (!this.entriesById.isEmpty()) {
            LedgerEntry e = this.removeAnyEntry();
            temp.add(e);

            if (e.currency.equals(currency) && e.type == EntryType.DEBIT) {
                total += e.amountCents;
            }
        }

        for (LedgerEntry e : temp) {
            this.entriesById.put(e.id, e);
        }

        return total;
    }

    public LedgerEntry findById(String id) {
        validateId(id);
        return this.entriesById.get(id);
    }

    public List<String> historyLines() {
        List<String> lines = new ArrayList<>();
        for (LedgerEntry e : this.entriesById.values()) {
            lines.add(e.toString());
        }
        return lines;
    }

    /*
     * Validation helpers
     */

    private static void validateId(String id) {
        assert id != null && !id.trim().isEmpty() : "id must not be empty";
    }

    private static void validateCurrency(String currency) {
        assert currency != null && currency
                .length() == 3 : "currency must be a 3 letter code like USD";
        for (int i = 0; i < currency.length(); i++) {
            char c = currency.charAt(i);
            boolean upper = (c >= 'A' && c <= 'Z');
            assert upper : "currency must be uppercase letters like USD";
        }
    }

    private static void validateAmountPositive(int amountCents) {
        assert amountCents > 0 : "amountCents must be positive";
    }

    private String nextTransactionId(String prefix) {
        String id;
        do {
            id = prefix + "-" + this.nextTransactionNumber;
            this.nextTransactionNumber++;
        } while (this.entriesById.containsKey(id));
        return id;
    }

    /*
     * Main method demonstration
     *
     * Shows a variety of use cases and sells the component value - deposits and
     * withdrawals - sufficient funds check - multi currency support - history
     * inspection - totals by type
     */
    public static void main(String[] args) {
        WalletLedgerMVP ledger = new WalletLedgerMVP();

        System.out.println("Starting count: " + ledger.entryCount());

        ledger.deposit(50_000, "USD");
        ledger.deposit(20_000, "USD");
        ledger.deposit(10_000, "EUR");

        System.out.println("After deposits count: " + ledger.entryCount());
        System.out.println("USD balance cents: " + ledger.balanceCents("USD"));
        System.out.println("EUR balance cents: " + ledger.balanceCents("EUR"));

        System.out.println("Can withdraw 40_000 USD: "
                + ledger.hasSufficientFunds(40_000, "USD"));
        System.out.println("Can withdraw 80_000 USD: "
                + ledger.hasSufficientFunds(80_000, "USD"));

        ledger.withdraw(15_000, "USD");
        ledger.withdraw(5_000, "USD");

        System.out.println(
                "USD balance after withdrawals: " + ledger.balanceCents("USD"));
        System.out.println(
                "USD total credits: " + ledger.totalCreditsCents("USD"));
        System.out
                .println("USD total debits: " + ledger.totalDebitsCents("USD"));

        System.out.println("History");
        for (String line : ledger.historyLines()) {
            System.out.println(line);
        }

        System.out
                .println("Find a random existing entry by iterating first key");
        LedgerEntry any = ledger.removeAnyEntry();
        System.out.println("Removed any entry: " + any);
        System.out
                .println("Count after removeAnyEntry: " + ledger.entryCount());

        ledger.addEntry(any.id, any.amountCents, any.currency, any.type);
        System.out.println(
                "Restored removed entry. Count: " + ledger.entryCount());

        System.out.println("Done");
    }
}
