import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Secondary methods for WalletLedger.
 *
 * Implements all enhanced-interface methods using only kernel and
 * Standard methods.
 *
 * @author Isiaka Kebe
 */
public abstract class WalletLedgerSecondary implements WalletLedger {

    /**
     * Returns a fresh id not currently used in this ledger.
     *
     * @return unused entry id
     * @ensures result is not empty and not already in this
     */
    private String freshEntryId() {
        int n = this.entryCount() + 1;
        String id = "E" + n;
        while (this.hasEntry(id)) {
            n++;
            id = "E" + n;
        }
        return id;
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
        while (source.entryCount() > 0) {
            LedgerEntry e = source.removeAnyEntry();
            destination.addEntry(e.id(), e.amountCents(), e.currency(), e.type());
        }
    }

    /**
     * Returns a deterministic text form for one entry.
     *
     * @param e ledger entry
     * @return text form of e
     */
    private static String entryText(LedgerEntry e) {
        return e.id() + "|" + e.type() + "|" + e.currency() + "|"
                + e.amountCents();
    }

    @Override
    public final int balanceCents(String currency) {
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        return this.totalCreditsCents(currency) - this.totalDebitsCents(currency);
    }

    @Override
    public final boolean hasSufficientFunds(int debitCents, String currency) {
        assert debitCents > 0 : "Violation of: debitCents > 0";
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        return this.balanceCents(currency) >= debitCents;
    }

    @Override
    public final void deposit(int amountCents, String currency) {
        assert amountCents > 0 : "Violation of: amountCents > 0";
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        this.addEntry(this.freshEntryId(), amountCents, currency, EntryType.CREDIT);
    }

    @Override
    public final void withdraw(int amountCents, String currency) {
        assert amountCents > 0 : "Violation of: amountCents > 0";
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";
        assert this.hasSufficientFunds(amountCents, currency)
                : "Violation of: hasSufficientFunds(amountCents, currency)";

        this.addEntry(this.freshEntryId(), amountCents, currency, EntryType.DEBIT);
    }

    @Override
    public final int totalCreditsCents(String currency) {
        assert currency != null : "Violation of: currency is not null";
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        int total = 0;
        WalletLedgerKernel temp = this.newInstance();

        while (this.entryCount() > 0) {
            LedgerEntry e = this.removeAnyEntry();
            if (e.type() == EntryType.CREDIT && e.currency().equals(currency)) {
                total += e.amountCents();
            }
            temp.addEntry(e.id(), e.amountCents(), e.currency(), e.type());
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
            LedgerEntry e = this.removeAnyEntry();
            if (e.type() == EntryType.DEBIT && e.currency().equals(currency)) {
                total += e.amountCents();
            }
            temp.addEntry(e.id(), e.amountCents(), e.currency(), e.type());
        }

        restoreEntries(temp, this);
        return total;
    }

    @Override
    public final LedgerEntry findById(String id) {
        assert id != null : "Violation of: id is not null";
        assert id.length() > 0 : "Violation of: id is not empty";

        LedgerEntry result = null;

        if (this.hasEntry(id)) {
            result = this.removeEntry(id);
            this.addEntry(result.id(), result.amountCents(), result.currency(),
                    result.type());
        }

        return result;
    }

    @Override
    public final String toString() {
        List<String> entries = new ArrayList<>();
        WalletLedgerKernel temp = this.newInstance();

        while (this.entryCount() > 0) {
            LedgerEntry e = this.removeAnyEntry();
            entries.add(entryText(e));
            temp.addEntry(e.id(), e.amountCents(), e.currency(), e.type());
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
        WalletLedgerKernel tempThis = this.newInstance();
        WalletLedgerKernel tempOther = other.newInstance();

        while (same && this.entryCount() > 0) {
            LedgerEntry e = this.removeAnyEntry();
            tempThis.addEntry(e.id(), e.amountCents(), e.currency(), e.type());

            if (!other.hasEntry(e.id())) {
                same = false;
            } else {
                LedgerEntry f = other.removeEntry(e.id());
                tempOther.addEntry(f.id(), f.amountCents(), f.currency(), f.type());

                same = e.amountCents() == f.amountCents()
                        && e.currency().equals(f.currency())
                        && e.type() == f.type();
            }
        }

        restoreEntries(tempThis, this);
        restoreEntries(tempOther, other);

        return same;
    }

    @Override
    public final int hashCode() {
        return this.toString().hashCode();
    }
}
