package com.blockchain.transactions.swap

import com.blockchain.coincore.AssetAction
import com.blockchain.coincore.Coincore
import com.blockchain.coincore.CryptoAccount
import com.blockchain.data.DataResource
import com.blockchain.nabu.datamanagers.repositories.swap.CustodialRepository
import com.blockchain.utils.toFlowDataResource
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CurrencyPair
import info.blockchain.balance.FiatCurrency
import io.reactivex.rxjava3.kotlin.zipWith
import kotlinx.coroutines.flow.Flow

internal class SwapRepository(
    private val coincore: Coincore,
    private val custodialRepository: CustodialRepository,
) : SwapService {

    override fun sourceAccounts(): Flow<DataResource<List<CryptoAccount>>> {
        return coincore.walletsWithAction(
            action = AssetAction.Swap
        ).zipWith(
            custodialRepository.getSwapAvailablePairs()
        ).map { (accounts, pairs) ->
            accounts.filter { account ->
                (account as? CryptoAccount)?.isAvailableToSwapFrom(pairs) ?: false
            }
        }.map {
            it.map { account -> account as CryptoAccount }
        }.toFlowDataResource()
    }

    private fun CryptoAccount.isAvailableToSwapFrom(pairs: List<CurrencyPair>): Boolean =
        pairs.any { it.source == this.currency }

    override suspend fun fiatToCrypto(
        fiatValue: String,
        fiatCurrency: FiatCurrency,
        cryptoCurrency: CryptoCurrency
    ): String {
        return (fiatValue.toDouble() / 2).toString()
    }

    override suspend fun cryptoToFiat(
        cryptoValue: String,
        fiatCurrency: FiatCurrency,
        cryptoCurrency: CryptoCurrency
    ): String {
        return (cryptoValue.toDouble() * 2).toString()
    }
}