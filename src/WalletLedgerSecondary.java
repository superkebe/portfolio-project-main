/**
 * Secondary implementation of {@link WalletLedger}.
 *
 * Implements all enhanced methods using only kernel and Standard methods.
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
     * Generates an unused entry id for a new transaction.
     *
     * @param prefix transaction prefix
     * @return an id not currently in this ledger
     */
    private String nextGeneratedId(String prefix) {
        int suffix = this.entryCount() + 1;
        String candidate = prefix + "-" + suffix;
        while (this.hasEntry(candidate)) {
            suffix++;
            candidate = prefix + "-" + suffix;
        }
        return candidate;
    }

    @Override
    public final int balanceCents(String currency) {
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        int balance = 0;
        WalletLedgerKernel temp = this.newInstance();
        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();
            temp.addEntry(entry.id(), entry.amountCents(), entry.currency(),
                    entry.type());
            if (entry.currency().equals(currency)) {
                if (entry.type() == EntryType.CREDIT) {
                    balance += entry.amountCents();
                } else {
                    balance -= entry.amountCents();
                }
            }
        }
        this.transferFrom(temp);
        return balance;
    }

    @Override
    public final boolean hasSufficientFunds(int debitCents, String currency) {
        assertPositiveAmount(debitCents);
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        return this.balanceCents(currency) >= debitCents;
    }

    @Override
    public final void deposit(int amountCents, String currency) {
        assertPositiveAmount(amountCents);
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        this.addEntry(this.nextGeneratedId("dep"), amountCents, currency,
                EntryType.CREDIT);
    }

    @Override
    public final void withdraw(int amountCents, String currency) {
        assertPositiveAmount(amountCents);
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";
        assert this.hasSufficientFunds(amountCents, currency)
                : "Violation of: hasSufficientFunds(amountCents, currency)";

        this.addEntry(this.nextGeneratedId("wd"), amountCents, currency,
                EntryType.DEBIT);
    }

    @Override
    public final int totalCreditsCents(String currency) {
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        int total = 0;
        WalletLedgerKernel temp = this.newInstance();
        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();
            temp.addEntry(entry.id(), entry.amountCents(), entry.currency(),
                    entry.type());
            if (entry.currency().equals(currency)
                    && entry.type() == EntryType.CREDIT) {
                total += entry.amountCents();
            }
        }
        this.transferFrom(temp);
        return total;
    }

    @Override
    public final int totalDebitsCents(String currency) {
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";

        int total = 0;
        WalletLedgerKernel temp = this.newInstance();
        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();
            temp.addEntry(entry.id(), entry.amountCents(), entry.currency(),
                    entry.type());
            if (entry.currency().equals(currency)
                    && entry.type() == EntryType.DEBIT) {
                total += entry.amountCents();
            }
        }
        this.transferFrom(temp);
        return total;
    }

    @Override
    public final LedgerEntry findById(String id) {
        assertValidId(id);

        LedgerEntry found = null;
        WalletLedgerKernel temp = this.newInstance();
        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();
            temp.addEntry(entry.id(), entry.amountCents(), entry.currency(),
                    entry.type());
            if (found == null && entry.id().equals(id)) {
                found = entry;
            }
        }
        this.transferFrom(temp);
        return found;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("WalletLedger[");
        boolean first = true;
        WalletLedgerKernel temp = this.newInstance();
        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();
            temp.addEntry(entry.id(), entry.amountCents(), entry.currency(),
                    entry.type());
            if (!first) {
                result.append(", ");
            }
            result.append(entry.id()).append(": ").append(entry.type())
                    .append(" ").append(entry.amountCents()).append(" ")
                    .append(entry.currency());
            first = false;
        }
        this.transferFrom(temp);
        result.append("]");
        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
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
            temp.addEntry(entry.id(), entry.amountCents(), entry.currency(),
                    entry.type());
            same = sameEntry(entry, other.findById(entry.id()));
        }
        this.transferFrom(temp);
        return same;
    }

    @Override
    public int hashCode() {
        int result = 0;
        WalletLedgerKernel temp = this.newInstance();
        while (this.entryCount() > 0) {
            LedgerEntry entry = this.removeAnyEntry();
            temp.addEntry(entry.id(), entry.amountCents(), entry.currency(),
                    entry.type());
            result += entry.id().hashCode();
            result += Integer.hashCode(entry.amountCents());
            result += entry.currency().hashCode();
            result += entry.type().hashCode();
        }
        this.transferFrom(temp);
        return result;
    }
}
