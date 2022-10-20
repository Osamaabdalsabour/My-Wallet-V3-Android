package com.blockchain.home.presentation.activity.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.blockchain.componentlib.basic.Image
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.control.CancelableOutlinedSearch
import com.blockchain.componentlib.navigation.NavigationBar
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.data.DataResource
import com.blockchain.home.presentation.R
import com.blockchain.home.presentation.SectionSize
import com.blockchain.home.presentation.activity.ActivityIntent
import com.blockchain.home.presentation.activity.ActivityViewModel
import com.blockchain.home.presentation.activity.ActivityViewState
import com.blockchain.home.presentation.activity.TransactionGroup
import com.blockchain.home.presentation.activity.TransactionState
import com.blockchain.home.presentation.activity.TransactionStatus
import com.blockchain.home.presentation.allassets.composable.CryptoAssetsLoading
import com.blockchain.koin.payloadScope
import org.koin.androidx.compose.getViewModel

@Composable
fun Acitivity(
    viewModel: ActivityViewModel = getViewModel(scope = payloadScope)
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val stateFlowLifecycleAware = remember(viewModel.viewState, lifecycleOwner) {
        viewModel.viewState.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
    }
    val viewState: ActivityViewState? by stateFlowLifecycleAware.collectAsState(null)

    DisposableEffect(key1 = viewModel) {
        viewModel.onIntent(ActivityIntent.LoadActivity(SectionSize.All))
        onDispose { }
    }

    viewState?.let { state ->
        ActivityScreen(
            activity = state.activity
        )
    }
}

@Composable
fun ActivityScreen(
    activity: DataResource<Map<TransactionGroup, List<TransactionState>>>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0XFFF1F2F7))
    ) {
        NavigationBar(
            title = "//todo// Activity",
            onBackButtonClick = { },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimensions.smallSpacing)
        ) {
            when (activity) {
                is DataResource.Loading -> {
                    CryptoAssetsLoading()
                }
                is DataResource.Error -> {
                    // todo
                }
                is DataResource.Data -> {
                    ActivityData(
                        transactions = activity.data,
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityData(
    transactions: Map<TransactionGroup, List<TransactionState>>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CancelableOutlinedSearch(
            onValueChange = { },
            placeholder = stringResource(R.string.search)
        )

        Spacer(modifier = Modifier.size(AppTheme.dimensions.smallSpacing))

        ActivityGroups(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            transactions = transactions
        )
    }
}

@Composable
fun ActivityGroups(
    modifier: Modifier = Modifier,
    transactions: Map<TransactionGroup, List<TransactionState>>
) {
    transactions.keys.forEachIndexed { index, group ->
        val transactionsList = transactions[group]!!
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.name,
                style = AppTheme.typography.body2,
                color = AppTheme.colors.muted
            )

            if (group is TransactionGroup.Pending) {
                Spacer(modifier = Modifier.size(AppTheme.dimensions.smallestSpacing))

                Image(ImageResource.Local(R.drawable.ic_question))
            }
        }

        Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))

        ActivityList(transactions = transactionsList)

        if (index < transactionsList.toList().lastIndex) {
            Spacer(modifier = Modifier.size(AppTheme.dimensions.largeSpacing))
        }
    }
}

@Composable
fun ActivityList(
    modifier: Modifier = Modifier,
    transactions: List<TransactionState>
) {
    if (transactions.isNotEmpty()) {
        Card(
            backgroundColor = AppTheme.colors.background,
            shape = RoundedCornerShape(AppTheme.dimensions.mediumSpacing),
            elevation = 0.dp
        ) {
            Column(modifier = modifier) {
                transactions.forEachIndexed { index, transaction ->
                    TransactionState(
                        status = transaction.status,
                        iconUrl = transaction.transactionTypeIcon,
                        valueTopStart = transaction.valueTopStart,
                        valueTopEnd = transaction.valueTopEnd,
                        valueBottomStart = transaction.valueBottomStart,
                        valueBottomEnd = transaction.valueBottomEnd
                    )

                    if (index < transactions.lastIndex) {
                        Divider(color = Color(0XFFF1F2F7))
                    }
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xFF272727)
@Composable
fun PreviewActivityScreen() {
    ActivityScreen(
        activity = DataResource.Data(
            mapOf(
                TransactionGroup.Pending to listOf(
                    TransactionState(
                        transactionTypeIcon = "transactionTypeIcon",
                        transactionCoinIcon = "transactionCoinIcon",
                        TransactionStatus.Pending(),
                        valueTopStart = "Sent Bitcoin",
                        valueTopEnd = "-10.00",
                        valueBottomStart = "85% confirmed",
                        valueBottomEnd = "-0.00893208 ETH"
                    ),
                    TransactionState(
                        transactionTypeIcon = "Cashed Out USD",
                        transactionCoinIcon = "transactionCoinIcon",
                        TransactionStatus.Pending(isRbfTransaction = true),
                        valueTopStart = "Sent Bitcoin",
                        valueTopEnd = "-25.00",
                        valueBottomStart = "RBF transaction",
                        valueBottomEnd = "valueBottomEnd"
                    )
                ),
                TransactionGroup.Date("June") to listOf(
                    TransactionState(
                        transactionTypeIcon = "transactionTypeIcon",
                        transactionCoinIcon = "transactionCoinIcon",
                        TransactionStatus.Settled,
                        valueTopStart = "Sent Bitcoin",
                        valueTopEnd = "-10.00",
                        valueBottomStart = "June 14",
                        valueBottomEnd = "-0.00893208 ETH"
                    ),
                    TransactionState(
                        transactionTypeIcon = "Cashed Out USD",
                        transactionCoinIcon = "transactionCoinIcon",
                        TransactionStatus.Canceled,
                        valueTopStart = "Sent Bitcoin",
                        valueTopEnd = "-25.00",
                        valueBottomStart = "Canceled",
                        valueBottomEnd = "valueBottomEnd"
                    ),
                    TransactionState(
                        transactionTypeIcon = "transactionTypeIcon",
                        transactionCoinIcon = "transactionCoinIcon",
                        TransactionStatus.Canceled,
                        valueTopStart = "Sent Bitcoin",
                        valueTopEnd = "100.00",
                        valueBottomStart = "Canceled",
                        valueBottomEnd = "0.00025 BTC"
                    )
                ),
                TransactionGroup.Date("July") to listOf(
                    TransactionState(
                        transactionTypeIcon = "transactionTypeIcon",
                        transactionCoinIcon = "transactionCoinIcon",
                        TransactionStatus.Declined,
                        valueTopStart = "Added USD",
                        valueTopEnd = "-25.00",
                        valueBottomStart = "Declined",
                        valueBottomEnd = "valueBottomEnd"
                    ),
                    TransactionState(
                        transactionTypeIcon = "transactionTypeIcon",
                        transactionCoinIcon = "transactionCoinIcon",
                        TransactionStatus.Failed,
                        valueTopStart = "Added USD",
                        valueTopEnd = "-25.00",
                        valueBottomStart = "Failed",
                        valueBottomEnd = "valueBottomEnd"
                    )
                )
            )
        )
    )
}
