import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kernel implementation of {@link WalletLedger} backed by a map.
 *
 * @convention
 * [entries is not null] and
 * [every key in entries is non-null and not blank] and
 * [every mapped value is non-null] and
 * [for every key k in entries, entries.get(k).id().equals(k)] and
 * [every mapped entry has amountCents() > 0] and
 * [every mapped entry has a valid 3-letter uppercase currency] and
 * [every mapped entry has non-null type]
 *
 * @correspondence
 * this = the wallet ledger whose entries are exactly the values stored in
 * this.entries, where each map key is the unique id of its associated entry
 */
public final class WalletLedger1L extends WalletLedgerSecondary {

    /**
     * Immutable ledger entry implementation.
     */
    private static final class LedgerEntryRecord implements LedgerEntry {

        /**
         * Entry id.
         */
        private final String id;

        /**
         * Positive amount in cents.
         */
        private final int amountCents;

        /**
         * 3-letter uppercase currency code.
         */
        private final String currency;

        /**
         * Entry type.
         */
        private final EntryType type;

        /**
         * Constructs a ledger entry.
         *
         * @param id
         *            the entry id
         * @param amountCents
         *            the amount in cents
         * @param currency
         *            the currency code
         * @param type
         *            the entry type
         * @requires id is not empty and amountCents > 0 and
         *           currency is a valid currency and type is not null
         * @ensures this.id() = id and this.amountCents() = amountCents and
         *          this.currency() = currency and this.type() = type
         */
        private LedgerEntryRecord(String id, int amountCents, String currency,
                EntryType type) {
            assert id != null && !id.isBlank() : "Violation of: id is not empty";
            assert amountCents > 0 : "Violation of: amountCents > 0";
            assert isValidCurrencyCode(currency)
                    : "Violation of: currency is a valid currency";
            assert type != null : "Violation of: type is not null";

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

    /**
     * Private representation.
     */
    private Map<String, LedgerEntry> entries;

    /**
     * Creates a new empty representation.
     */
    private void createNewRep() {
        this.entries = new LinkedHashMap<>();
    }

    /**
     * Checks whether id satisfies the kernel contract.
     *
     * @param id
     *            candidate id
     */
    private static void assertValidId(String id) {
        assert id != null && !id.isBlank() : "Violation of: id is not empty";
    }

    /**
     * Checks whether amount satisfies the kernel contract.
     *
     * @param amountCents
     *            amount in cents
     */
    private static void assertPositiveAmount(int amountCents) {
        assert amountCents > 0 : "Violation of: amountCents > 0";
    }

    /**
     * Reports whether the given currency string is a valid 3-letter uppercase
     * code.
     *
     * @param currency
     *            candidate currency
     * @return true iff currency is valid
     * @ensures isValidCurrencyCode =
     *          (currency is a 3-letter uppercase code)
     */
    private static boolean isValidCurrencyCode(String currency) {
        if (currency == null || currency.length() != 3) {
            return false;
        }
        for (int i = 0; i < currency.length(); i++) {
            char c = currency.charAt(i);
            if (c < 'A' || c > 'Z') {
                return false;
            }
        }
        return true;
    }

    /**
     * No-argument constructor.
     *
     * @ensures this is empty
     */
    public WalletLedger1L() {
        this.createNewRep();
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
        return isValidCurrencyCode(currency);
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
