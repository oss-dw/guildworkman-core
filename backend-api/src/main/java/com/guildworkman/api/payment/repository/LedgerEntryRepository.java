package com.guildworkman.api.payment.repository;

import com.guildworkman.api.payment.model.LedgerDirection;
import com.guildworkman.api.payment.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Read-side of the journal. There is deliberately no update or delete query
 * here — see {@link com.guildworkman.api.payment.model.LedgerTransaction} on
 * why the ledger is append-only.
 */
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    /**
     * Total for one side of the whole book, in one currency. The trial-balance
     * invariant is that this returns the same figure for {@code DEBIT} and
     * {@code CREDIT}; {@code LedgerService.trialBalance} asserts exactly that,
     * and an integration test asserts it after every scenario the suite runs.
     *
     * <p>{@code coalesce} because {@code sum} over no rows is null in SQL, and
     * an empty book should read as zero rather than blow up on unboxing.
     */
    @Query("select coalesce(sum(e.amountMinor), 0) from LedgerEntry e "
            + "where e.direction = :direction and e.currency = :currency")
    long totalByDirection(@Param("direction") LedgerDirection direction, @Param("currency") String currency);

    @Query("select coalesce(sum(e.amountMinor), 0) from LedgerEntry e "
            + "where e.account.code = :accountCode and e.direction = :direction and e.currency = :currency")
    long totalByAccountAndDirection(@Param("accountCode") String accountCode,
                                    @Param("direction") LedgerDirection direction,
                                    @Param("currency") String currency);

    @Query("select distinct e.currency from LedgerEntry e")
    List<String> findDistinctCurrencies();

    List<LedgerEntry> findByLedgerTransactionIdOrderByIdAsc(Long ledgerTransactionId);
}
