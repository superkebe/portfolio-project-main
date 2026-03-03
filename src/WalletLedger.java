/**
 * Enhanced interface for the WalletLedger component.
 *
 * This interface adds secondary methods that can be implemented using only the
 * kernel and Standard methods.
 */
public interface WalletLedger extends WalletLedgerKernel {

    /**
     * Returns the current balance for the given currency.
     *
     * Balance is computed as total credits minus total debits for that currency.
     *
     * @param currency
     *            3 letter uppercase currency code
     * @return balance in cents for the given currency
     * @requires currency is a 3 letter uppercase code
     * @ensures balanceCents equals credits minus debits for currency
     * @ensures this is unchanged
     */
    int balanceCents(String currency);

    /**
     * Reports whether this ledger has sufficient funds to cover a debit of the
     * given amount in the given currency.
     *
     * @param debitCents
     *            positive debit amount in cents
     * @param currency
     *            3 letter uppercase currency code
     * @return true if balanceCents(currency) >= debitCents
     * @requires debitCents > 0
     * @requires currency is a 3 letter uppercase code
     * @ensures hasSufficientFunds equals (balanceCents(currency) >= debitCents)
     * @ensures this is unchanged
     */
    boolean hasSufficientFunds(int debitCents, String currency);

    /**
     * Convenience method that deposits money by adding a CREDIT entry.
     *
     * @param amountCents
     *            positive amount in cents
     * @param currency
     *            3 letter uppercase currency code
     * @updates this
     * @requires amountCents > 0
     * @requires currency is a 3 letter uppercase code
     * @ensures this contains one additional CREDIT entry for the given amount and currency
     */
    void deposit(int amountCents, String currency);

    /**
     * Convenience method that withdraws money by adding a DEBIT entry.
     *
     * @param amountCents
     *            positive amount in cents
     * @param currency
     *            3 letter uppercase currency code
     * @updates this
     * @requires amountCents > 0
     * @requires currency is a 3 letter uppercase code
     * @requires hasSufficientFunds(amountCents, currency)
     * @ensures this contains one additional DEBIT entry for the given amount and currency
     */
    void withdraw(int amountCents, String currency);

    /**
     * Returns the total of all CREDIT entries for the given currency.
     *
     * @param currency
     *            3 letter uppercase currency code
     * @return total credit amount in cents
     * @requires currency is a 3 letter uppercase code
     * @ensures totalCreditsCents is the sum of CREDIT entry amounts for currency
     * @ensures this is unchanged
     */
    int totalCreditsCents(String currency);

    /**
     * Returns the total of all DEBIT entries for the given currency.
     *
     * @param currency
     *            3 letter uppercase currency code
     * @return total debit amount in cents
     * @requires currency is a 3 letter uppercase code
     * @ensures totalDebitsCents is the sum of DEBIT entry amounts for currency
     * @ensures this is unchanged
     */
    int totalDebitsCents(String currency);

    /**
     * Finds and returns the entry with the given id, or returns null if it does
     * not exist.
     *
     * This method is useful for client queries without forcing the client to
     * remove entries manually.
     *
     * @param id
     *            entry id to search for
     * @return the entry with id, or null if none exists
     * @requires id is not empty
     * @ensures if an entry with id exists then findById.id equals id
     * @ensures this is unchanged
     */
    LedgerEntry findById(String id);
}
