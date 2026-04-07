# SRC Folder

This folder contains the source code for the WalletLedger component.

The component is implemented using the OSU CSE component design discipline, which separates functionality into kernel, enhanced interface, secondary implementation, and kernel implementation layers.

Component Structure

The WalletLedger component consists of the following files

WalletLedgerKernel
Defines the minimal set of core operations required to represent a ledger
WalletLedger
Defines enhanced operations such as balance, deposit, withdraw, and totals
WalletLedgerSecondary
Implements all enhanced methods using only kernel and Standard methods
WalletLedger1L
Provides the concrete kernel implementation using a LinkedHashMap representation
Design Overview

The component models a financial ledger that tracks credit and debit transactions across multiple currencies.

Each transaction includes

a unique id
a positive amount in cents
a 3 letter uppercase currency code
a type indicating CREDIT or DEBIT

Key design decisions include

using a map-based representation for efficient lookup and updates
storing amounts in integer cents to avoid floating point precision errors
maintaining immutability of ledger entries
enforcing correctness through assertions and method contracts
Representation

The ledger is internally represented using a LinkedHashMap that maps entry ids to immutable ledger entries.

This representation provides

efficient insertion and removal
fast lookup by id
preservation of insertion order for transaction history
Organization

All source files in this folder follow a clear separation of responsibilities based on component design principles.

The structure can be extended into a package hierarchy such as

components.walletledger

to support modularity and reuse in larger systems.

Goal

The goal of this design is to ensure

correctness through well-defined contracts and invariants
maintainability through clear abstraction layers
extensibility for future financial and wallet-based applications
