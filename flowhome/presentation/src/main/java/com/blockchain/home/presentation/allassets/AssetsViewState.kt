package com.blockchain.home.presentation.allassets

import androidx.compose.ui.graphics.Color
import com.blockchain.commonarch.presentation.mvi_v2.ViewState
import com.blockchain.componentlib.tablerow.ValueChange
import com.blockchain.data.DataResource
import com.blockchain.data.combineDataResources
import com.blockchain.data.dataOrElse
import com.blockchain.home.domain.AssetFilter
import info.blockchain.balance.Money
import info.blockchain.balance.percentageDelta

data class AssetsViewState(
    val balance: WalletBalance,
    val cryptoAssets: DataResource<List<CryptoAssetState>>,
    val fiatAssets: DataResource<List<FiatAssetState>>,
    val filters: List<AssetFilter>
) : ViewState

sealed interface HomeAsset {
    val icon: List<String>
    val name: String
    val balance: DataResource<Money>
}

data class CryptoAssetState(
    override val icon: List<String>,
    override val name: String,
    override val balance: DataResource<Money>,
    val fiatBalance: DataResource<Money>,
    val change: DataResource<ValueChange>
) : HomeAsset

data class FiatAssetState(
    override val icon: List<String>,
    override val name: String,
    override val balance: DataResource<Money>
) : HomeAsset

data class WalletBalance(
    val balance: DataResource<Money>,
    private val cryptoBalanceDifference24h: DataResource<Money>,
) {

    val balanceDifference: BalanceDifferenceConfig
        get() = combineDataResources(balance, cryptoBalanceDifference24h) { now, difference ->
            if (now.isZero && difference.isZero)
                BalanceDifferenceConfig()
            else
                ValueChange.fromValue(now.percentageDelta(difference)).takeIf { !it.value.isNaN() }
                    ?.let { valueChange ->
                        BalanceDifferenceConfig(
                            "${valueChange.indicator} " +
                                difference.toStringWithSymbol() +
                                " (${valueChange.value}%)",
                            valueChange.color
                        )
                    } ?: BalanceDifferenceConfig()
        }.dataOrElse(BalanceDifferenceConfig())
}

data class BalanceDifferenceConfig(val text: String = "", val color: Color = Color.Transparent)
