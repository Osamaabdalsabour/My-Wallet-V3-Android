package com.blockchain.home.presentation.dashboard.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import com.blockchain.chrome.navigation.AssetActionsNavigation
import com.blockchain.coincore.AssetAction
import com.blockchain.componentlib.lazylist.paddedItem
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.data.DataResource
import com.blockchain.home.presentation.activity.list.ActivityViewState
import com.blockchain.home.presentation.allassets.AssetsViewState
import com.blockchain.walletmode.WalletMode
// todo defi
fun LazyListScope.emptyCard(
    walletMode: WalletMode,
    assetsViewState: AssetsViewState,
    actiityViewState: ActivityViewState,
    assetActionsNavigation: AssetActionsNavigation
) {
    val state = dashboardState(assetsViewState, actiityViewState)

    if (state == DashboardState.EMPTY) {
        paddedItem(
            paddingValues = {
                PaddingValues(horizontal = AppTheme.dimensions.smallSpacing)
            }
        ) {
            NonCustodialEmptyStateCard {
                assetActionsNavigation.navigate(AssetAction.Receive)
            }
        }
    }
}

enum class DashboardState {
    EMPTY, NON_EMPTY, UNKNOWN
}

fun dashboardState(
    assetsViewState: AssetsViewState,
    activityViewState: ActivityViewState?
): DashboardState {
    activityViewState ?: return DashboardState.UNKNOWN
    val hasAnyActivity =
        (activityViewState.activity as? DataResource.Data)?.data?.any { act -> act.value.isNotEmpty() }
            ?: return DashboardState.UNKNOWN
    val hasAnyAssets =
        (assetsViewState.assets as? DataResource.Data)?.data?.isNotEmpty() ?: return DashboardState.UNKNOWN

    val shouldShowEmptyStateForAssets = assetsViewState.showNoResults

    return if (!hasAnyActivity && !hasAnyAssets && shouldShowEmptyStateForAssets) {
        DashboardState.EMPTY
    } else DashboardState.NON_EMPTY
}
