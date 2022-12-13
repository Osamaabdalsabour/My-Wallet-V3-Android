package com.blockchain.home.presentation.fiat.actions.sheetinterface

import com.blockchain.commonarch.presentation.base.SlidingModalBottomDialog
import com.blockchain.home.presentation.fiat.actions.models.LinkablePaymentMethodsForAction
import info.blockchain.balance.FiatCurrency

interface BankLinkingHost : SlidingModalBottomDialog.Host {
    fun onBankWireTransferSelected(currency: FiatCurrency)
    fun onLinkBankSelected(paymentMethodForAction: LinkablePaymentMethodsForAction)
}
