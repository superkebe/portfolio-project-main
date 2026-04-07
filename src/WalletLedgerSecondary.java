import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Secondary implementation of {@link WalletLedger}.
 *
 * Implements all enhanced-interface methods using only kernel and Standard
 * methods.
 */
public abstract class WalletLedgerSecondary implements WalletLedger {

    /**
     * Checks whether the id satisfies the client contract.
     *
     * @param id entry id
     */
    private static void assertValidId(String id) {
        assert id != null && !id.isBlank() : "Violation of: id is not empty";
    }

    /**
     * Checks whether the amount satisfies the client contract.
     *
     * @param amountCents amount in cents
     */
    private static void assertPositiveAmount(int amountCents) {
        assert amountCents > 0 : "Violation of: amountCents > 0";
    }

    /**
     * Restores all entries from source into destination.
     *
     * @param source source ledger
     * @param destination destination ledger
     * @updates source, destination
     * @requires source /= null
     * @requires destination /= null
     * @ensures source is empty
     */
    private static void restoreEntries(WalletLedgerKernel source,
            WalletLedgerKernel destination) {

        assert source != null : "Violation of: source /= null";
        assert destination != null : "Violation of: destination /= null";

        while (source.entryCount() > 0) {
            LedgerEntry entry = source.removeAnyEntry();
            destination.addEntry(entry.id(), entry.amountCents(),
                    entry.currency(), entry.type());
        }
    }

    /**
     * Reports whether two entries have the same observable state.
     *
     * @param first first entry
     * @param second second entry
     * @return true iff the entries match field-for-field
     */
    private static boolean sameEntry(LedgerEntry first, LedgerEntry second) {
        return first != null && second != null && first.id().equals(second.id())
                && first.amountCents() == second.amountCents()
                && first.currency().equals(second.currency())
                && first.type() == second.type();
    }

    /**
     * Returns a deterministic text form for one entry.
     *
     * @param entry ledger entry
     * @return text form of entry
     */
    private static String entryText(LedgerEntry entry) {
        return entry.id() + "|" + entry.type() + "|" + entry.currency() + "|"
                + entry.amountCents();
    }

    /**
     * Returns a fresh id not currently used in this ledger.
     *
     * @return unused entry id
     * @ensures result is not empty and not already in this
     */
    private String freshEntryId() {
        int suffix = this.entryCount() + 1;
        String id = "E" + suffix;

        while (this.hasEntry(id)) {
            suffix++;
            id = "E" + suffix;
        }

        return id;
    }

    @Override
    public final int balanceCents(String currency) {
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        return this.totalCreditsCents(currency)
                - this.totalDebitsCents(currency);
    }

    @Override
    public final boolean hasSufficientFunds(int debitCents,
            String currency) {

        assertPositiveAmount(debitCents);
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        return this.balanceCents(currency) >= debitCents;
    }

    @Override
    public final void deposit(int amountCents, String currency) {
        assertPositiveAmount(amountCents);
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        this.addEntry(this.freshEntryId(), amountCents, currency,
                EntryType.CREDIT);
    }

    @Override
    public final void withdraw(int amountCents, String currency) {
        assertPositiveAmount(amountCents);
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";
        assert this.hasSufficientFunds(amountCents, currency)
                : "Violation of: hasSufficientFunds(amountCents, currency)";

        this.addEntry(this.freshEntryId(), amountCents, currency,
                EntryType.DEBIT);
    }

    @Override
    public final int totalCreditsCents(String currency) {
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        int total = 0;
        WalletLedgerKernel temp = this.newInstance();

        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();

            if (entry.type() == EntryType.CREDIT
                    && entry.currency().equals(currency)) {
                total += entry.amountCents();
            }

            temp.addEntry(entry.id(), entry.amountCents(),
                    entry.currency(), entry.type());
        }

        restoreEntries(temp, this);
        return total;
    }

    @Override
    public final int totalDebitsCents(String currency) {
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        int total = 0;
        WalletLedgerKernel temp = this.newInstance();

        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();

            if (entry.type() == EntryType.DEBIT
                    && entry.currency().equals(currency)) {
                total += entry.amountCents();
            }

            temp.addEntry(entry.id(), entry.amountCents(),
                    entry.currency(), entry.type());
        }

        restoreEntries(temp, this);
        return total;
    }

    @Override
    public final LedgerEntry findById(String id) {
        assertValidId(id);

        LedgerEntry result = null;
        WalletLedgerKernel temp = this.newInstance();

        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();

            if (entry.id().equals(id)) {
                result = entry;
            }

            temp.addEntry(entry.id(), entry.amountCents(),
                    entry.currency(), entry.type());
        }

        restoreEntries(temp, this);
        return result;
    }

    @Override
    public final String toString() {
        List<String> entries = new ArrayList<>();
        WalletLedgerKernel temp = this.newInstance();

        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();
            entries.add(entryText(entry));

            temp.addEntry(entry.id(), entry.amountCents(),
                    entry.currency(), entry.type());
        }

        restoreEntries(temp, this);
        Collections.sort(entries);

        return "WalletLedger[" + String.join(", ", entries) + "]";
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof WalletLedger)) {
            return false;
        }

        WalletLedger other = (WalletLedger) obj;

        if (this.entryCount() != other.entryCount()) {
            return false;
        }

        boolean same = true;
        WalletLedgerKernel temp = this.newInstance();

        while (same && this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();

            temp.addEntry(entry.id(), entry.amountCents(),
                    entry.currency(), entry.type());

            same = sameEntry(entry, other.findById(entry.id()));
        }

        restoreEntries(temp, this);
        return same;
    }

    @Override
    public final int hashCode() {
        return this.toString().hashCode();
    }
}
