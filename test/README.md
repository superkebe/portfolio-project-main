README.md

This folder contains JUnit test files for the WalletLedger component.

The purpose of these tests is to verify that the component behaves correctly according to its contracts and maintains its representation invariant.

The tests cover

Kernel methods such as addEntry, removeEntry, removeAnyEntry, and entryCount
Enhanced methods such as deposit, withdraw, balanceCents, totalCreditsCents, and totalDebitsCents
Standard methods such as clear, newInstance, and transferFrom
Edge cases such as empty ledger behavior and multi-currency handling

The test structure follows the same logical organization as the source code to keep the project consistent and easy to navigate.
