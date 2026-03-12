import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kernel implementation of {@link WalletLedger} backed by a map.
 */
public final class WalletLedger1L extends WalletLedgerSecondary {

    /**
     * Immutable ledger entry implementation.
     */
    private static final class LedgerEntryRecord implements LedgerEntry {
        private final String id;
        private final int amountCents;
        private final String currency;
        private final EntryType type;

        private LedgerEntryRecord(String id, int amountCents, String currency,
                EntryType type) {
            this.id = id;
            this.amountCents = amountCents;
            this.currency = currency;
            this.type = type;
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        public int amountCents() {
            return this.amountCents;
        }

        @Override
        public String currency() {
            return this.currency;
        }

        @Override
        public EntryType type() {
            return this.type;
        }
    }

    /*
     * Representation:
     *
     * $this.entries maps each entry id to exactly one immutable ledger entry
     * having the same id.
     *
     * Convention:
     *
     * - this.entries is not null
     * - every key in this.entries is non-null and not blank
     * - every mapped entry is non-null
     * - each mapped entry has positive amount, valid currency, and non-null type
     * - each map key equals the id stored in its mapped entry
     */
    private Map<String, LedgerEntry> entries;

    /**
     * Creates an empty ledger.
     */
    public WalletLedger1L() {
        this.createNewRep();
    }

    /**
     * Initializes the representation.
     */
    private void createNewRep() {
        this.entries = new LinkedHashMap<>();
    }

    /**
     * Checks whether id satisfies the kernel contract.
     *
     * @param id candidate id
     */
    private static void assertValidId(String id) {
        assert id != null && !id.isBlank() : "Violation of: id is not empty";
    }

    /**
     * Checks whether amount satisfies the kernel contract.
     *
     * @param amountCents amount in cents
     */
    private static void assertPositiveAmount(int amountCents) {
        assert amountCents > 0 : "Violation of: amountCents > 0";
    }

    @Override
    public void clear() {
        this.createNewRep();
    }

    @Override
    public WalletLedgerKernel newInstance() {
        return new WalletLedger1L();
    }

    @Override
    public void transferFrom(WalletLedgerKernel source) {
        assert source != null : "Violation of: source is not null";
        assert source != this : "Violation of: source is not this";
        assert source instanceof WalletLedger1L
                : "Violation of: source has dynamic type WalletLedger1L";

        WalletLedger1L localSource = (WalletLedger1L) source;
        this.entries = localSource.entries;
        localSource.createNewRep();
    }

    @Override
    public boolean hasEntry(String id) {
        assertValidId(id);
        return this.entries.containsKey(id);
    }

    @Override
    public boolean isValidCurrency(String currency) {
        if (currency == null || currency.length() != 3) {
            return false;
        }

        for (int i = 0; i < currency.length(); i++) {
            char current = currency.charAt(i);
            if (current < 'A' || current > 'Z') {
                return false;
            }
        }

        return true;
    }

    @Override
    public void addEntry(String id, int amountCents, String currency,
            EntryType type) {
        assertValidId(id);
        assertPositiveAmount(amountCents);
        assert this.isValidCurrency(currency)
                : "Violation of: isValidCurrency(currency)";
        assert type != null : "Violation of: type is not null";
        assert !this.hasEntry(id) : "Violation of: not hasEntry(id)";

        this.entries.put(id,
                new LedgerEntryRecord(id, amountCents, currency, type));
    }

    @Override
    public LedgerEntry removeEntry(String id) {
        assertValidId(id);
        assert this.hasEntry(id) : "Violation of: hasEntry(id)";

        return this.entries.remove(id);
    }

    @Override
    public LedgerEntry removeAnyEntry() {
        assert this.entryCount() > 0 : "Violation of: this is not empty";

        String id = this.entries.keySet().iterator().next();
        return this.entries.remove(id);
    }

    @Override
    public int entryCount() {
        return this.entries.size();
    }
}
