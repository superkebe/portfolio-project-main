import components.standard.Standard;

/**
 * Kernel interface for WalletLedger.
 *
 * The kernel provides the minimal operations required to model a
 * wallet ledger as a collection of credit and debit entries.
 */
public interface WalletLedgerKernel
        extends Standard<WalletLedgerKernel> {

    /**
     * Entry types for the ledger.
     */
    enum EntryType {
        CREDIT,
        DEBIT
    }

    /**
     * Client-visible immutable ledger entry.
     */
    interface LedgerEntry {

        /**
         * Returns the unique id of this entry.
         *
         * @return id of this entry
         * @ensures id is not empty
         */
        String id();

        /**
         * Returns the positive amount in cents.
         *
         * @return amount in cents
         * @ensures amountCents > 0
         */
        int amountCents();

        /**
         * Returns the 3-letter uppercase currency code.
         *
         * @return currency code
         * @ensures currency length is 3 and uppercase
         */
        String currency();

        /**
         * Returns whether this entry is a credit or debit.
         *
         * @return entry type
         * @ensures type is CREDIT or DEBIT
         */
        EntryType type();
    }

    /**
     * Reports whether the given id currently identifies an entry in this
     * ledger.
     *
     * @param id candidate entry id
     * @return true iff an entry with {@code id} exists in this
     *
     * @requires id is not empty
     * @ensures result = (an entry with id exists in this)
     * @ensures this is unchanged
     */
    boolean hasEntry(String id);

    /**
     * Reports whether the given currency code is valid for this component.
     *
     * @param currency candidate currency code
     * @return true iff currency is a 3-letter uppercase code
     *
     * @ensures result = (currency is a 3-letter uppercase code)
     * @ensures this is unchanged
     */
    boolean isValidCurrency(String currency);

    /**
     * Adds a new ledger entry.
     *
     * @param id unique entry id
     * @param amountCents positive amount in cents
     * @param currency 3-letter uppercase currency code
     * @param type credit or debit
     *
     * @updates this
     * @requires id is not empty
     * @requires amountCents > 0
     * @requires isValidCurrency(currency)
     * @requires type is not null
     * @requires not hasEntry(id)
     * @ensures this contains a new entry with given fields
     */
    void addEntry(String id, int amountCents,
                  String currency, EntryType type);

    /**
     * Removes and returns the entry with the given id.
     *
     * @param id id to remove
     * @return removed entry
     *
     * @updates this
     * @requires id is not empty
     * @requires hasEntry(id)
     * @ensures entry with id is removed from this
     */
    LedgerEntry removeEntry(String id);

    /**
     * Removes and returns any entry.
     *
     * @return some entry previously in this
     *
     * @updates this
     * @requires this is not empty
     * @ensures this has one fewer entry
     */
    LedgerEntry removeAnyEntry();

    /**
     * Returns number of entries.
     *
     * @return number of entries
     * @ensures result >= 0
     */
    int entryCount();
}
