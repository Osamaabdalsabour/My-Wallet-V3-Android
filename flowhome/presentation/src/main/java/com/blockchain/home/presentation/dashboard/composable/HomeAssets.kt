package com.blockchain.home.presentation.dashboard.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.blockchain.componentlib.R
import com.blockchain.componentlib.basic.Image
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.icons.Icons
import com.blockchain.componentlib.icons.Question
import com.blockchain.componentlib.lazylist.paddedItem
import com.blockchain.componentlib.lazylist.paddedRoundedCornersItems
import com.blockchain.componentlib.tablerow.BalanceChangeTableRow
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.theme.Grey400
import com.blockchain.componentlib.theme.Grey700
import com.blockchain.componentlib.utils.clickableNoEffect
import com.blockchain.data.map
import com.blockchain.domain.paymentmethods.model.FundsLocks
import com.blockchain.home.presentation.allassets.CustodialAssetState
import com.blockchain.home.presentation.allassets.FiatAssetState
import com.blockchain.home.presentation.allassets.HomeAsset
import com.blockchain.home.presentation.allassets.NonCustodialAssetState
import com.blockchain.home.presentation.allassets.composable.BalanceWithFiatAndCryptoBalance
import com.blockchain.home.presentation.allassets.composable.BalanceWithPriceChange
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.Money

@Composable
fun HomeAssetsHeader(openCryptoAssets: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.ma_home_assets_title),
            style = AppTheme.typography.body2,
            color = Grey700
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.clickableNoEffect(openCryptoAssets),
            text = stringResource(R.string.see_all),
            style = AppTheme.typography.paragraph2,
            color = AppTheme.colors.primary,
        )
    }
}

@Composable
fun FundLocksData(
    total: Money,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick),
        backgroundColor = AppTheme.colors.background,
        shape = RoundedCornerShape(AppTheme.dimensions.mediumSpacing),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(AppTheme.dimensions.smallSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.funds_locked_warning_title),
                style = AppTheme.typography.paragraph2,
                color = AppTheme.colors.muted
            )

            Spacer(modifier = Modifier.size(AppTheme.dimensions.smallestSpacing))

            Image(Icons.Question.withTint(Grey400).withSize(14.dp))

            Spacer(modifier = Modifier.weight(1F))

            Text(
                text = total.toStringWithSymbol(),
                style = AppTheme.typography.paragraph2,
                color = AppTheme.colors.muted
            )
        }
    }
}

internal fun LazyListScope.homeAssets(
    locks: FundsLocks?,
    data: List<HomeAsset>,
    assetOnClick: (AssetInfo) -> Unit,
    openCryptoAssets: () -> Unit,
    fundsLocksOnClick: (FundsLocks) -> Unit,
    openFiatActionDetail: (String) -> Unit
) {
    paddedItem(
        paddingValues = PaddingValues(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.large_spacing)))
        HomeAssetsHeader(openCryptoAssets)
        Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))
    }

    locks?.let {
        paddedItem(
            paddingValues = PaddingValues(horizontal = 16.dp)
        ) {
            FundLocksData(
                total = locks.onHoldTotalAmount,
                onClick = { fundsLocksOnClick(it) }
            )
            Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))
        }
    }

    paddedRoundedCornersItems(
        items = data.filterIsInstance<CustodialAssetState>(),
        key = { state -> state.asset.networkTicker },
        paddingValues = PaddingValues(horizontal = 16.dp)
    ) {
        BalanceWithPriceChange(
            cryptoAsset = it,
            onAssetClick = assetOnClick
        )
    }
    paddedRoundedCornersItems(
        items = data.filterIsInstance<NonCustodialAssetState>(),
        key = { state -> state.asset.networkTicker },
        paddingValues = PaddingValues(horizontal = 16.dp)
    ) {
        BalanceWithFiatAndCryptoBalance(
            cryptoAsset = it,
            onAssetClick = assetOnClick
        )
    }

    item {
        val fiatSpacer = if (data.filterIsInstance<CustodialAssetState>().isNotEmpty() &&
            data.filterIsInstance<FiatAssetState>().isNotEmpty()
        ) {
            AppTheme.dimensions.smallSpacing
        } else
            0.dp
        Spacer(modifier = Modifier.size(fiatSpacer))
    }

    paddedRoundedCornersItems(
        items = data.filterIsInstance<FiatAssetState>(),
        key = { state -> state.account.currency.networkTicker },
        paddingValues = PaddingValues(horizontal = 16.dp)
    ) {
        BalanceChangeTableRow(
            name = it.name,
            value = it.balance.map { money ->
                money.toStringWithSymbol()
            },
            contentStart = {
                Image(
                    imageResource = ImageResource.Remote(it.icon[0]),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(dimensionResource(R.dimen.standard_spacing)),
                    defaultShape = CircleShape
                )
            },
            onClick = {
                openFiatActionDetail(it.account.currency.networkTicker)
            }
        )
    }
}
