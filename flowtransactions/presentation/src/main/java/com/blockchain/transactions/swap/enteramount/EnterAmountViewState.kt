package com.blockchain.transactions.swap.enteramount

import com.blockchain.commonarch.presentation.mvi_v2.ViewState
import com.blockchain.componentlib.control.CurrencyValue
import com.blockchain.componentlib.control.InputCurrency

data class EnterAmountViewState(
    val selectedInput: InputCurrency,
    val assets: EnterAmountAssets?,
    val maxAmount: String?,
    val fiatAmount: CurrencyValue?,
    val cryptoAmount: CurrencyValue?,
    val snackbarError: Exception?,
    val inputError: SwapEnterAmountInputError?,
    val fatalError: SwapEnterAmountFatalError?
) : ViewState

data class EnterAmountAssets(
    val from: EnterAmountAssetState,
    val to: EnterAmountAssetState?
)

data class EnterAmountAssetState(
    val iconUrl: String,
    val nativeAssetIconUrl: String?,
    val ticker: String
)
