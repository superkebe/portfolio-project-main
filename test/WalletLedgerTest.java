import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * JUnit tests for WalletLedger.
 */
public final class WalletLedgerTest {

    /**
     * Returns a new test ledger.
     *
     * @return new empty ledger
     */
    private WalletLedger newLedger() {
        return new WalletLedger1L();
    }

    /**
     * Tests that a new ledger starts empty.
     */
    @Test
    public void testConstructorStartsEmpty() {
        WalletLedger ledger = this.newLedger();
        assertEquals(0, ledger.entryCount());
    }

    /**
     * Tests balances and totals on an empty ledger.
     */
    @Test
    public void testEmptyLedgerBalancesAreZero() {
        WalletLedger ledger = this.newLedger();

        assertEquals(0, ledger.balanceCents("USD"));
        assertEquals(0, ledger.totalCreditsCents("USD"));
        assertEquals(0, ledger.totalDebitsCents("USD"));
    }

    /**
     * Tests valid currency.
     */
    @Test
    public void testValidCurrency() {
        WalletLedger ledger = this.newLedger();
        assertTrue(ledger.isValidCurrency("USD"));
    }

    /**
     * Tests invalid lowercase currency.
     */
    @Test
    public void testInvalidLowercaseCurrency() {
        WalletLedger ledger = this.newLedger();
        assertFalse(ledger.isValidCurrency("usd"));
    }

    /**
     * Tests invalid short currency.
     */
    @Test
    public void testInvalidShortCurrency() {
        WalletLedger ledger = this.newLedger();
        assertFalse(ledger.isValidCurrency("US"));
    }

    /**
     * Tests invalid long currency.
     */
    @Test
    public void testInvalidLongCurrency() {
        WalletLedger ledger = this.newLedger();
        assertFalse(ledger.isValidCurrency("USDT"));
    }

    /**
     * Tests invalid mixed currency.
     */
    @Test
    public void testInvalidMixedCurrency() {
        WalletLedger ledger = this.newLedger();
        assertFalse(ledger.isValidCurrency("U1D"));
    }

    /**
     * Tests adding one valid entry.
     */
    @Test
    public void testAddEntryIncreasesCount() {
        WalletLedger ledger = this.newLedger();

        ledger.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);

        assertEquals(1, ledger.entryCount());
        assertTrue(ledger.hasEntry("E1"));
    }

    /**
     * Tests adding multiple valid entries.
     */
    @Test
    public void testAddMultipleEntries() {
        WalletLedger ledger = this.newLedger();

        ledger.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        ledger.addEntry("E2", 3000, "USD", WalletLedgerKernel.EntryType.DEBIT);
        ledger.addEntry("E3", 7000, "EUR", WalletLedgerKernel.EntryType.CREDIT);

        assertEquals(3, ledger.entryCount());
        assertTrue(ledger.hasEntry("E1"));
        assertTrue(ledger.hasEntry("E2"));
        assertTrue(ledger.hasEntry("E3"));
    }

    /**
     * Tests removing one entry by id.
     */
    @Test
    public void testRemoveEntryRemovesCorrectly() {
        WalletLedger ledger = this.newLedger();
        ledger.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);

        WalletLedgerKernel.LedgerEntry removed = ledger.removeEntry("E1");

        assertEquals("E1", removed.id());
        assertEquals(5000, removed.amountCents());
        assertEquals("USD", removed.currency());
        assertEquals(WalletLedgerKernel.EntryType.CREDIT, removed.type());
        assertEquals(0, ledger.entryCount());
        assertFalse(ledger.hasEntry("E1"));
    }

    /**
     * Tests removeAnyEntry on non-empty ledger.
     */
    @Test
    public void testRemoveAnyEntryRemovesOneEntry() {
        WalletLedger ledger = this.newLedger();
        ledger.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        ledger.addEntry("E2", 3000, "USD", WalletLedgerKernel.EntryType.DEBIT);

        WalletLedgerKernel.LedgerEntry removed = ledger.removeAnyEntry();

        assertNotNull(removed);
        assertEquals(1, ledger.entryCount());
    }

    /**
     * Tests total credits in one currency.
     */
    @Test
    public void testTotalCreditsCents() {
        WalletLedger ledger = this.newLedger();

        ledger.addEntry("E1", 4000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        ledger.addEntry("E2", 6000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        ledger.addEntry("E3", 2000, "USD", WalletLedgerKernel.EntryType.DEBIT);

        assertEquals(10000, ledger.totalCreditsCents("USD"));
    }

    /**
     * Tests total debits in one currency.
     */
    @Test
    public void testTotalDebitsCents() {
        WalletLedger ledger = this.newLedger();

        ledger.addEntry("E1", 4000, "USD", WalletLedgerKernel.EntryType.DEBIT);
        ledger.addEntry("E2", 6000, "USD", WalletLedgerKernel.EntryType.DEBIT);
        ledger.addEntry("E3", 2000, "USD", WalletLedgerKernel.EntryType.CREDIT);

        assertEquals(10000, ledger.totalDebitsCents("USD"));
    }

    /**
     * Tests balance calculation in one currency.
     */
    @Test
    public void testBalanceCents() {
        WalletLedger ledger = this.newLedger();

        ledger.addEntry("E1", 10000, "USD",
                WalletLedgerKernel.EntryType.CREDIT);
        ledger.addEntry("E2", 3000, "USD", WalletLedgerKernel.EntryType.DEBIT);

        assertEquals(7000, ledger.balanceCents("USD"));
    }

    /**
     * Tests balance separation across currencies.
     */
    @Test
    public void testMultiCurrencySeparation() {
        WalletLedger ledger = this.newLedger();

        ledger.addEntry("E1", 10000, "USD",
                WalletLedgerKernel.EntryType.CREDIT);
        ledger.addEntry("E2", 5000, "EUR", WalletLedgerKernel.EntryType.CREDIT);
        ledger.addEntry("E3", 2000, "USD", WalletLedgerKernel.EntryType.DEBIT);

        assertEquals(8000, ledger.balanceCents("USD"));
        assertEquals(5000, ledger.balanceCents("EUR"));
    }

    /**
     * Tests that secondary methods do not change contents.
     */
    @Test
    public void testSecondaryMethodsDoNotChangeLedgerContents() {
        WalletLedger ledger = this.newLedger();

        ledger.addEntry("E1", 10000, "USD",
                WalletLedgerKernel.EntryType.CREDIT);
        ledger.addEntry("E2", 3000, "USD", WalletLedgerKernel.EntryType.DEBIT);
        ledger.addEntry("E3", 5000, "EUR", WalletLedgerKernel.EntryType.CREDIT);

        int beforeCount = ledger.entryCount();

        ledger.balanceCents("USD");
        ledger.totalCreditsCents("USD");
        ledger.totalDebitsCents("USD");

        assertEquals(beforeCount, ledger.entryCount());
        assertTrue(ledger.hasEntry("E1"));
        assertTrue(ledger.hasEntry("E2"));
        assertTrue(ledger.hasEntry("E3"));
    }

    /**
     * Tests deposit.
     */
    @Test
    public void testDepositAddsCredit() {
        WalletLedger ledger = this.newLedger();

        ledger.deposit(5000, "USD");

        assertEquals(1, ledger.entryCount());
        assertEquals(5000, ledger.totalCreditsCents("USD"));
        assertEquals(0, ledger.totalDebitsCents("USD"));
        assertEquals(5000, ledger.balanceCents("USD"));
    }

    /**
     * Tests withdraw after sufficient funds exist.
     */
    @Test
    public void testWithdrawAddsDebit() {
        WalletLedger ledger = this.newLedger();

        ledger.deposit(10000, "USD");
        ledger.withdraw(4000, "USD");

        assertEquals(2, ledger.entryCount());
        assertEquals(10000, ledger.totalCreditsCents("USD"));
        assertEquals(4000, ledger.totalDebitsCents("USD"));
        assertEquals(6000, ledger.balanceCents("USD"));
    }

    /**
     * Tests hasSufficientFunds.
     */
    @Test
    public void testHasSufficientFunds() {
        WalletLedger ledger = this.newLedger();

        ledger.deposit(7000, "USD");

        assertTrue(ledger.hasSufficientFunds(5000, "USD"));
        assertFalse(ledger.hasSufficientFunds(9000, "USD"));
    }

    /**
     * Tests findById when entry exists.
     */
    @Test
    public void testFindByIdExisting() {
        WalletLedger ledger = this.newLedger();
        ledger.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);

        WalletLedgerKernel.LedgerEntry found = ledger.findById("E1");

        assertNotNull(found);
        assertEquals("E1", found.id());
        assertEquals(5000, found.amountCents());
        assertEquals("USD", found.currency());
        assertEquals(WalletLedgerKernel.EntryType.CREDIT, found.type());
        assertEquals(1, ledger.entryCount());
        assertTrue(ledger.hasEntry("E1"));
    }

    /**
     * Tests findById when entry does not exist.
     */
    @Test
    public void testFindByIdMissing() {
        WalletLedger ledger = this.newLedger();
        ledger.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);

        WalletLedgerKernel.LedgerEntry found = ledger.findById("E9");

        assertNull(found);
        assertEquals(1, ledger.entryCount());
        assertTrue(ledger.hasEntry("E1"));
    }

    /**
     * Tests clear.
     */
    @Test
    public void testClearResetsLedger() {
        WalletLedger ledger = this.newLedger();

        ledger.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        ledger.addEntry("E2", 3000, "EUR", WalletLedgerKernel.EntryType.DEBIT);

        ledger.clear();

        assertEquals(0, ledger.entryCount());
    }

    /**
     * Tests newInstance.
     */
    @Test
    public void testNewInstanceCreatesEmptyLedger() {
        WalletLedger original = this.newLedger();
        original.addEntry("E1", 5000, "USD",
                WalletLedgerKernel.EntryType.CREDIT);

        WalletLedgerKernel fresh = original.newInstance();

        assertNotNull(fresh);
        assertEquals(0, fresh.entryCount());
        assertEquals(1, original.entryCount());
    }

    /**
     * Tests transferFrom.
     */
    @Test
    public void testTransferFromMovesContents() {
        WalletLedgerKernel source = new WalletLedger1L();
        WalletLedgerKernel destination = new WalletLedger1L();

        source.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        source.addEntry("E2", 3000, "USD", WalletLedgerKernel.EntryType.DEBIT);

        destination.transferFrom(source);

        assertEquals(0, source.entryCount());
        assertEquals(2, destination.entryCount());
        assertTrue(destination.hasEntry("E1"));
        assertTrue(destination.hasEntry("E2"));
    }

    /**
     * Tests equals on same content.
     */
    @Test
    public void testEqualsForSameContent() {
        WalletLedger first = this.newLedger();
        WalletLedger second = this.newLedger();

        first.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        first.addEntry("E2", 3000, "EUR", WalletLedgerKernel.EntryType.DEBIT);

        second.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        second.addEntry("E2", 3000, "EUR", WalletLedgerKernel.EntryType.DEBIT);

        assertTrue(first.equals(second));
        assertTrue(second.equals(first));
    }

    /**
     * Tests equals on different content.
     */
    @Test
    public void testEqualsForDifferentContent() {
        WalletLedger first = this.newLedger();
        WalletLedger second = this.newLedger();

        first.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        second.addEntry("E1", 7000, "USD", WalletLedgerKernel.EntryType.CREDIT);

        assertFalse(first.equals(second));
    }

    /**
     * Tests hashCode consistency with equals.
     */
    @Test
    public void testHashCodeForEqualLedgers() {
        WalletLedger first = this.newLedger();
        WalletLedger second = this.newLedger();

        first.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);
        second.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);

        assertEquals(first.hashCode(), second.hashCode());
    }

    /**
     * Tests toString stability.
     */
    @Test
    public void testToStringIsDeterministic() {
        WalletLedger ledger = this.newLedger();

        ledger.addEntry("E2", 3000, "USD", WalletLedgerKernel.EntryType.DEBIT);
        ledger.addEntry("E1", 5000, "USD", WalletLedgerKernel.EntryType.CREDIT);

        String first = ledger.toString();
        String second = ledger.toString();

        assertEquals(first, second);
    }
}