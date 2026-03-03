import components.standard.Standard;

/**
 * Kernel interface for the WalletLedger component.
 *
 * The kernel provides only the minimal operations required to model a wallet
 * ledger as a collection of credit and debit entries.
 *
 * The enhanced interface WalletLedger will layer secondary operations such as
 * balance computation and safety checks on top of this kernel.
 */
public interface WalletLedgerKernel extends Standard<WalletLedgerKernel> {

    /**
     * Entry type for the ledger.
     */
    enum EntryType {
        CREDIT,
        DEBIT
    }

    /**
     * A single immutable ledger entry returned to clients.
     *
     * This is a client side value type used by kernel operations that remove and
     * return entries.
     */
    interface LedgerEntry {

        /**
         * Returns the unique id for this entry.
         *
         * @return id for this entry
         * @ensures id is not empty
         */
        String id();

        /**
         * Returns the positive amount in cents for this entry.
         *
         * @return amount in cents
         * @ensures amountCents > 0
         */
        int amountCents();

        /**
         * Returns the 3 letter uppercase currency code for this entry.
         *
         * @return currency code
         * @ensures currency has length 3 and uses uppercase letters
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
     * Adds a new ledger entry.
     *
     * @param id
     *            unique entry id
     * @param amountCents
     *            positive amount in cents
     * @param currency
     *            3 letter uppercase currency code
     * @param type
     *            CREDIT or DEBIT
     * @updates this
     * @requires id is not empty
     * @requires amountCents > 0
     * @requires currency is a 3 letter uppercase code
     * @requires type is not null
     * @requires there is no existing entry in this with id
     * @ensures this contains an entry with the given fields
     */
    void addEntry(String id, int amountCents, String currency, EntryType type);

    /**
     * Removes the entry with the given id and returns it.
     *
     * @param id
     *            id of the entry to remove
     * @return the removed entry
     * @updates this
     * @requires id is not empty
     * @requires this contains an entry with id
     * @ensures this no longer contains an entry with id
     * @ensures removeEntry.id equals id
     */
    LedgerEntry removeEntry(String id);

    /**
     * Removes and returns any entry from this ledger.
     *
     * This method exists to support safe traversal patterns where the client
     * temporarily removes entries, processes them, and then restores them.
     *
     * @return one entry formerly in this
     * @updates this
     * @requires this is not empty
     * @ensures this has one fewer entry than before
     */
    LedgerEntry removeAnyEntry();

    /**
     * Returns the number of entries in this ledger.
     *
     * @return number of entries
     * @ensures entryCount is at least 0
     */
    int entryCount();
}
