/**
 * Enhanced interface for WalletLedger.
 *
 * Adds secondary operations that can be implemented
 * using only kernel and Standard methods.
 */
public interface WalletLedger
        extends WalletLedgerKernel {

    /**
     * Returns current balance for the given currency.
     *
     * @param currency 3-letter uppercase currency code
     * @return balance in cents
     *
     * @requires isValidCurrency(currency)
     * @ensures result equals total credits minus total debits
     * @ensures this is unchanged
     */
    int balanceCents(String currency);

    /**
     * Reports whether a debit can be covered.
     *
     * @param debitCents positive amount
     * @param currency currency code
     * @return true if balanceCents >= debitCents
     *
     * @requires debitCents > 0
     * @requires isValidCurrency(currency)
     * @ensures result equals (balanceCents >= debitCents)
     * @ensures this is unchanged
     */
    boolean hasSufficientFunds(int debitCents,
                               String currency);

    /**
     * Adds a credit entry.
     *
     * @param amountCents positive amount
     * @param currency currency code
     *
     * @updates this
     * @requires amountCents > 0
     * @requires isValidCurrency(currency)
     * @ensures this contains one additional CREDIT entry
     */
    void deposit(int amountCents, String currency);

    /**
     * Adds a debit entry.
     *
     * @param amountCents positive amount
     * @param currency currency code
     *
     * @updates this
     * @requires amountCents > 0
     * @requires isValidCurrency(currency)
     * @requires hasSufficientFunds(amountCents, currency)
     * @ensures this contains one additional DEBIT entry
     */
    void withdraw(int amountCents, String currency);

    /**
     * Returns total credits for the given currency.
     *
     * @param currency currency code
     * @return total credits in cents
     *
     * @requires isValidCurrency(currency)
     * @ensures result equals sum of CREDIT entries
     * @ensures this is unchanged
     */
    int totalCreditsCents(String currency);

    /**
     * Returns total debits for the given currency.
     *
     * @param currency currency code
     * @return total debits in cents
     *
     * @requires isValidCurrency(currency)
     * @ensures result equals sum of DEBIT entries
     * @ensures this is unchanged
     */
    int totalDebitsCents(String currency);

    /**
     * Finds an entry by id.
     *
     * @param id entry id
     * @return matching entry or null if none
     *
     * @requires id is not empty
     * @ensures hasEntry(id) implies result.id() = id
     * @ensures not hasEntry(id) implies result is null
     * @ensures this is unchanged
     */
    LedgerEntry findById(String id);
}
