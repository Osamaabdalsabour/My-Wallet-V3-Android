package com.blockchain.walletconnect.ui.navigation

import com.blockchain.commonarch.presentation.mvi_v2.compose.ComposeNavigationDestination
import com.blockchain.commonarch.presentation.mvi_v2.compose.wrappedArg

sealed class WalletConnectDestination(
    override val route: String
) : ComposeNavigationDestination {
    object WalletConnectDappList : WalletConnectDestination("WalletConnectDappList")
    object WalletConnectManageSession : WalletConnectDestination(
        "WalletConnectManageSession/${ARG_SESSION_ID.wrappedArg()}/${ARG_IS_V2_SESSION.wrappedArg()}"
    )

    companion object {
        const val ARG_SESSION_ID = "sessionId"
        const val ARG_IS_V2_SESSION = "isV2"
    }
}
