package com.blockchain.home.presentation.dashboard.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import com.blockchain.analytics.Analytics
import com.blockchain.chrome.LocalNavControllerProvider
import com.blockchain.coincore.AssetAction
import com.blockchain.commonarch.presentation.mvi_v2.compose.NavArgument
import com.blockchain.commonarch.presentation.mvi_v2.compose.navigate
import com.blockchain.componentlib.alert.AlertType
import com.blockchain.componentlib.alert.CardAlert
import com.blockchain.componentlib.card.CardButton
import com.blockchain.componentlib.chrome.MenuOptionsScreen
import com.blockchain.componentlib.lazylist.paddedItem
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.utils.collectAsStateLifecycleAware
import com.blockchain.componentlib.utils.openUrl
import com.blockchain.data.DataResource
import com.blockchain.data.dataOrElse
import com.blockchain.data.doOnData
import com.blockchain.data.map
import com.blockchain.data.toImmutableList
import com.blockchain.domain.paymentmethods.model.FundsLocks
import com.blockchain.domain.referral.model.ReferralInfo
import com.blockchain.home.handhold.HandholdTask
import com.blockchain.stringResources.R
import com.blockchain.home.presentation.SectionSize
import com.blockchain.home.presentation.accouncement.AnnouncementsIntent
import com.blockchain.home.presentation.accouncement.AnnouncementsViewModel
import com.blockchain.home.presentation.accouncement.AnnouncementsViewState
import com.blockchain.home.presentation.accouncement.LocalAnnouncementType
import com.blockchain.home.presentation.accouncement.composable.LocalAnnouncements
import com.blockchain.home.presentation.accouncement.composable.StackedAnnouncements
import com.blockchain.home.presentation.activity.list.ActivityIntent
import com.blockchain.home.presentation.activity.list.ActivityViewState
import com.blockchain.home.presentation.activity.list.custodial.CustodialActivityViewModel
import com.blockchain.home.presentation.activity.list.privatekey.PrivateKeyActivityViewModel
import com.blockchain.home.presentation.allassets.AssetsIntent
import com.blockchain.home.presentation.allassets.AssetsViewModel
import com.blockchain.home.presentation.allassets.AssetsViewState
import com.blockchain.home.presentation.dapps.HomeDappsIntent
import com.blockchain.home.presentation.dapps.HomeDappsViewModel
import com.blockchain.home.presentation.dapps.HomeDappsViewState
import com.blockchain.home.presentation.dashboard.DashboardAnalyticsEvents
import com.blockchain.home.presentation.handhold.HandholdIntent
import com.blockchain.home.presentation.handhold.HandholdViewModel
import com.blockchain.home.presentation.handhold.HandholdViewState
import com.blockchain.home.presentation.navigation.ARG_ACTIVITY_TX_ID
import com.blockchain.home.presentation.navigation.ARG_WALLET_MODE
import com.blockchain.chrome.navigation.AssetActionsNavigation
import com.blockchain.chrome.navigation.LocalAssetActionsNavigationProvider
import com.blockchain.coincore.NullCryptoAddress.asset
import com.blockchain.home.presentation.navigation.HomeDestination
import com.blockchain.home.presentation.navigation.RecurringBuyNavigation
import com.blockchain.home.presentation.navigation.SupportNavigation
import com.blockchain.home.presentation.news.NewsIntent
import com.blockchain.home.presentation.news.NewsViewModel
import com.blockchain.home.presentation.news.NewsViewState
import com.blockchain.home.presentation.quickactions.QuickActions
import com.blockchain.home.presentation.quickactions.QuickActionsIntent
import com.blockchain.home.presentation.quickactions.QuickActionsViewModel
import com.blockchain.home.presentation.quickactions.QuickActionsViewState
import com.blockchain.home.presentation.quickactions.maxQuickActionsOnScreen
import com.blockchain.home.presentation.recurringbuy.list.RecurringBuyEligibleState
import com.blockchain.home.presentation.recurringbuy.list.RecurringBuysIntent
import com.blockchain.home.presentation.recurringbuy.list.RecurringBuysViewModel
import com.blockchain.home.presentation.recurringbuy.list.RecurringBuysViewState
import com.blockchain.home.presentation.referral.ReferralIntent
import com.blockchain.home.presentation.referral.ReferralViewModel
import com.blockchain.home.presentation.referral.ReferralViewState
import com.blockchain.koin.payloadScope
import com.blockchain.prices.prices.PricesIntents
import com.blockchain.prices.prices.PricesLoadStrategy
import com.blockchain.prices.prices.PricesViewModel
import com.blockchain.prices.prices.PricesViewState
import com.blockchain.prices.prices.percentAndPositionOf
import com.blockchain.walletconnect.ui.composable.common.DappSessionUiElement
import com.blockchain.walletmode.WalletMode
import com.blockchain.walletmode.WalletModeService
import info.blockchain.balance.AssetInfo
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    analytics: Analytics = get(),
    listState: LazyListState,
    isSwipingToRefresh: Boolean,
    recurringBuyNavigation: RecurringBuyNavigation,
    supportNavigation: SupportNavigation,
    openSettings: () -> Unit,
    launchQrScanner: () -> Unit,
    openRecurringBuys: () -> Unit,
    openRecurringBuyDetail: (String) -> Unit,
    openSwapDexOption: () -> Unit,
    openFiatActionDetail: (String) -> Unit,
    openMoreQuickActions: () -> Unit,
    startPhraseRecovery: () -> Unit,
    processAnnouncementUrl: (String) -> Unit,
    onWalletConnectSessionClicked: (DappSessionUiElement) -> Unit,
    onWalletConnectSeeAllSessionsClicked: () -> Unit
) {
    var menuOptionsHeight: Int by remember { mutableStateOf(0) }
    var balanceOffsetToMenuOption: Float by remember { mutableStateOf(0F) }
    val balanceToMenuPaddingPx: Int = LocalDensity.current.run { 24.dp.toPx() }.toInt()
    var balanceScrollRange: Float by remember { mutableStateOf(0F) }

    val navController = LocalNavControllerProvider.current
    val assetActionsNavigation = LocalAssetActionsNavigationProvider.current


    // navigation
    fun NavHostController.openAssetsList(analytics: Analytics, assetsCount: Int) {
        navigate(HomeDestination.CryptoAssets)
        analytics.logEvent(
            DashboardAnalyticsEvents.AssetsSeeAllClicked(assetsCount = assetsCount)
        )
    }

    fun openCoinview(analytics: Analytics, asset: AssetInfo) {
        assetActionsNavigation.coinview(asset)
        analytics.logEvent(
            DashboardAnalyticsEvents.CryptoAssetClicked(ticker = asset.displayTicker)
        )
    }

    fun NavHostController.openActivityList(analytics: Analytics) {
        navigate(HomeDestination.Activity)
        analytics.logEvent(DashboardAnalyticsEvents.ActivitySeeAllClicked)
    }

    fun NavHostController.openActivityDetail(txId: String, walletMode: WalletMode) {
        navigate(
            destination = HomeDestination.ActivityDetail,
            args = listOf(
                NavArgument(key = ARG_ACTIVITY_TX_ID, value = txId),
                NavArgument(key = ARG_WALLET_MODE, value = walletMode)
            )
        )
    }

    fun NavHostController.openRefferal() {
        navigate(HomeDestination.Referral)
    }
    //

    val walletMode by
    get<WalletModeService>(scope = payloadScope).walletMode.collectAsStateLifecycleAware(initial = null)

    DisposableEffect(walletMode) {
        walletMode?.let {
            analytics.logEvent(DashboardAnalyticsEvents.ModeViewed(walletMode = it))
        }
        onDispose { }
    }

    walletMode?.let {
        when (it) {
            WalletMode.CUSTODIAL -> {
                CustodialHomeDashboard(
                    analytics = analytics,

                    isSwipingToRefresh = isSwipingToRefresh,

                    listState = listState,
                    openSettings = openSettings,
                    launchQrScanner = launchQrScanner,

                    processAnnouncementUrl = processAnnouncementUrl,
                    startPhraseRecovery = startPhraseRecovery,

                    assetActionsNavigation = assetActionsNavigation,
                    openDexSwapOptions = openSwapDexOption,
                    openMoreQuickActions = openMoreQuickActions,

                    openCryptoAssets = {
                        navController.openAssetsList(analytics = analytics, assetsCount = it)
                    },
                    assetOnClick = { asset ->
                        openCoinview(analytics = analytics, asset = asset)
                    },
                    fundsLocksOnClick = { fundsLocks ->
                        assetActionsNavigation.fundsLocksDetail(fundsLocks)
                    },
                    openFiatActionDetail = { ticker ->
                        openFiatActionDetail(ticker)
                        analytics.logEvent(DashboardAnalyticsEvents.FiatAssetClicked(ticker = ticker))
                    },

                    manageOnclick = openRecurringBuys,
                    upsellOnClick = recurringBuyNavigation::openOnboarding,
                    recurringBuyOnClick = openRecurringBuyDetail,

                    openActivity = {
                        navController.openActivityList(analytics)
                    },
                    openActivityDetail = navController::openActivityDetail,

                    openReferral = navController::openRefferal,

                    supportNavigation = supportNavigation,

                    showBackground = balanceOffsetToMenuOption <= 0F && menuOptionsHeight > 0F,
                    showBalance = balanceScrollRange <= 0.5 && menuOptionsHeight > 0F,
                    balanceAlphaProvider = { balanceScrollRange },
                    hideBalance = balanceScrollRange <= 0.5 && menuOptionsHeight > 0F,
                    menuOptionsHeightLoaded = {
                        if (menuOptionsHeight == 0) menuOptionsHeight = it
                    },
                    balanceYPositionLoaded = { balanceYPosition ->
                        (balanceYPosition - menuOptionsHeight + balanceToMenuPaddingPx)
                            .coerceAtLeast(0F).let {
                                if (balanceOffsetToMenuOption != it) balanceOffsetToMenuOption = it
                            }

                        ((balanceYPosition / menuOptionsHeight.toFloat()) * 2).coerceIn(0F, 1F).let {
                            if (balanceScrollRange != it) balanceScrollRange = it
                        }
                    }
                )
            }

            WalletMode.NON_CUSTODIAL -> {
                DefiHomeDashboard(
                    analytics = analytics,

                    isSwipingToRefresh = isSwipingToRefresh,

                    listState = listState,
                    openSettings = openSettings,
                    launchQrScanner = launchQrScanner,

                    processAnnouncementUrl = processAnnouncementUrl,
                    startPhraseRecovery = startPhraseRecovery,

                    assetActionsNavigation = assetActionsNavigation,
                    openDexSwapOptions = openSwapDexOption,
                    openMoreQuickActions = openMoreQuickActions,

                    openCryptoAssets = {
                        navController.openAssetsList(analytics = analytics, assetsCount = it)
                    },
                    assetOnClick = { asset ->
                        openCoinview(analytics = analytics, asset = asset)
                    },
                    fundsLocksOnClick = { fundsLocks ->
                        assetActionsNavigation.fundsLocksDetail(fundsLocks)
                    },
                    openFiatActionDetail = { ticker ->
                        openFiatActionDetail(ticker)
                        analytics.logEvent(DashboardAnalyticsEvents.FiatAssetClicked(ticker = ticker))
                    },

                    onDappSessionClicked = onWalletConnectSessionClicked,
                    onWalletConnectSeeAllSessionsClicked = onWalletConnectSeeAllSessionsClicked,

                    openActivity = {
                        navController.openActivityList(analytics)
                    },
                    openActivityDetail = navController::openActivityDetail,
                    openReferral = navController::openRefferal,

                    supportNavigation = supportNavigation,

                    showBackground = balanceOffsetToMenuOption <= 0F && menuOptionsHeight > 0F,
                    showBalance = balanceScrollRange <= 0.5 && menuOptionsHeight > 0F,
                    balanceAlphaProvider = { balanceScrollRange },
                    hideBalance = balanceScrollRange <= 0.5 && menuOptionsHeight > 0F,
                    menuOptionsHeightLoaded = {
                        if (menuOptionsHeight == 0) menuOptionsHeight = it
                    },
                    balanceYPositionLoaded = { balanceYPosition ->
                        (balanceYPosition - menuOptionsHeight + balanceToMenuPaddingPx)
                            .coerceAtLeast(0F).let {
                                if (balanceOffsetToMenuOption != it) balanceOffsetToMenuOption = it
                            }

                        ((balanceYPosition / menuOptionsHeight.toFloat()) * 2).coerceIn(0F, 1F).let {
                            if (balanceScrollRange != it) balanceScrollRange = it
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustodialHomeDashboard(
    analytics: Analytics,

    isSwipingToRefresh: Boolean,

    listState: LazyListState,
    openSettings: () -> Unit,
    launchQrScanner: () -> Unit,

    processAnnouncementUrl: (String) -> Unit,
    startPhraseRecovery: () -> Unit,

    assetActionsNavigation: AssetActionsNavigation,
    openDexSwapOptions: () -> Unit,
    openMoreQuickActions: () -> Unit,

    assetOnClick: (AssetInfo) -> Unit,
    openCryptoAssets: (count: Int) -> Unit,
    fundsLocksOnClick: (FundsLocks) -> Unit,
    openFiatActionDetail: (String) -> Unit,

    manageOnclick: () -> Unit,
    upsellOnClick: () -> Unit,
    recurringBuyOnClick: (String) -> Unit,

    openActivity: () -> Unit,
    openActivityDetail: (String, WalletMode) -> Unit,

    openReferral: () -> Unit,

    supportNavigation: SupportNavigation,

    showBackground: Boolean = false,
    showBalance: Boolean = false,
    balanceAlphaProvider: () -> Float,
    hideBalance: Boolean,
    menuOptionsHeightLoaded: (Int) -> Unit,
    balanceYPositionLoaded: (Float) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = LocalNavControllerProvider.current
    val context = LocalContext.current

    val handholdViewModel: HandholdViewModel = getViewModel(scope = payloadScope)
    val handholdViewState: HandholdViewState by handholdViewModel.viewState.collectAsStateLifecycleAware()
    val quickActionsViewModel: QuickActionsViewModel = getViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner,
        scope = payloadScope
    )
    val quickActionsState: QuickActionsViewState by quickActionsViewModel.viewState.collectAsStateLifecycleAware()
    val maxQuickActions = maxQuickActionsOnScreen
    val announcementsViewModel: AnnouncementsViewModel = getViewModel(scope = payloadScope)
    val announcementsState: AnnouncementsViewState by announcementsViewModel.viewState.collectAsStateLifecycleAware()
    val homeAssetsViewModel: AssetsViewModel = getViewModel(scope = payloadScope)
    val assetsViewState: AssetsViewState by homeAssetsViewModel.viewState.collectAsStateLifecycleAware()
    val rbViewModel: RecurringBuysViewModel = getViewModel(scope = payloadScope)
    val rbViewState: RecurringBuysViewState by rbViewModel.viewState.collectAsStateLifecycleAware()
    val pricesViewModel: PricesViewModel = getViewModel(scope = payloadScope)
    val pricesViewState: PricesViewState by pricesViewModel.viewState.collectAsStateLifecycleAware()
    val activityViewModel: CustodialActivityViewModel = getViewModel(scope = payloadScope)
    val activityViewState: ActivityViewState by activityViewModel.viewState.collectAsStateLifecycleAware()
    val referralViewModel: ReferralViewModel = getViewModel(scope = payloadScope)
    val referralState: ReferralViewState by referralViewModel.viewState.collectAsStateLifecycleAware()
    val newsViewModel: NewsViewModel = getViewModel(scope = payloadScope)
    val newsViewState: NewsViewState by newsViewModel.viewState.collectAsStateLifecycleAware()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // onboarding handhold
                handholdViewModel.onIntent(HandholdIntent.LoadData)
                // quick action
                quickActionsViewModel.onIntent(
                    QuickActionsIntent.LoadActions(
                        walletMode = WalletMode.CUSTODIAL,
                        maxQuickActionsOnScreen = maxQuickActions
                    )
                )
                // announcements
                announcementsViewModel.onIntent(
                    AnnouncementsIntent.LoadAnnouncements(
                        walletMode = WalletMode.CUSTODIAL,
                    )
                )
                // accounts
                homeAssetsViewModel.onIntent(AssetsIntent.LoadFilters)
                homeAssetsViewModel.onIntent(
                    AssetsIntent.LoadAccounts(
                        walletMode = WalletMode.CUSTODIAL,
                        sectionSize = SectionSize.Limited(MAX_ASSET_COUNT)
                    )
                )
                homeAssetsViewModel.onIntent(AssetsIntent.LoadFundLocks)
                // rb
                rbViewModel.onIntent(RecurringBuysIntent.LoadRecurringBuys(SectionSize.Limited(MAX_RB_COUNT)))
                // top movers
                pricesViewModel.onIntent(PricesIntents.LoadData(PricesLoadStrategy.TradableOnly))
                // activity
                activityViewModel.onIntent(ActivityIntent.LoadActivity(SectionSize.Limited(MAX_ACTIVITY_COUNT)))
                // referral
                referralViewModel.onIntent(ReferralIntent.LoadData())
                // news
                newsViewModel.onIntent(NewsIntent.LoadData)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = isSwipingToRefresh) {
        if (isSwipingToRefresh) {
            announcementsViewModel.onIntent(AnnouncementsIntent.Refresh)
            homeAssetsViewModel.onIntent(AssetsIntent.Refresh)
            pricesViewModel.onIntent(PricesIntents.Refresh)
            quickActionsViewModel.onIntent(QuickActionsIntent.Refresh)
            activityViewModel.onIntent(ActivityIntent.Refresh())
            newsViewModel.onIntent(NewsIntent.Refresh)
        }
    }

    val balance = (assetsViewState.balance.balance as? DataResource.Data)?.data
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(
                AppTheme.colors.background
            )
    ) {
        stickyHeader {
            MenuOptionsScreen(
                modifier = Modifier.onGloballyPositioned {
                    menuOptionsHeightLoaded(it.size.height)
                },
                walletBalanceCurrency = balance?.symbol.orEmpty(),
                walletBalance = balance?.toStringWithoutSymbol().orEmpty(),
                openSettings = openSettings,
                launchQrScanner = launchQrScanner,
                showBackground = showBackground,
                showBalance = showBalance
            )
        }

        item {
            BalanceScreen(
                modifier = Modifier.onGloballyPositioned {
                    balanceYPositionLoaded(it.positionInParent().y)
                },
                balanceAlphaProvider = balanceAlphaProvider,
                hideBalance = hideBalance,
                walletBalance = assetsViewState.balance,
            )
        }

        // quick actions
        // should not be shown if kyc is rejected
        if (!handholdViewState.showKycRejected) {
            paddedItem(
                paddingValues = {
                    PaddingValues(AppTheme.dimensions.smallSpacing)
                }
            ) {
                QuickActions(
                    quickActionItems = quickActionsState.actions,
                    assetActionsNavigation = assetActionsNavigation,
                    quickActionsViewModel = quickActionsViewModel,
                    openDexSwapOptions = openDexSwapOptions,
                    dashboardState = dashboardState(assetsViewState, activityViewState),
                    openMoreQuickActions = openMoreQuickActions
                )
            }
        }

        // announcements, assets, dapps, rb, top movers, activity, referrals, news
        // should wait for handhold status
        handholdViewState.showHandhold.doOnData { showHandhold ->
            when (showHandhold) {
                true -> {
                    // at this point the dashboard will only be handhold + help
                    val handholdTasks = (handholdViewState.tasksStatus as DataResource.Data).data.toImmutableList()
                    handhold(
                        data = handholdTasks,
                        onClick = {
                            when (it) {
                                HandholdTask.VerifyEmail -> {
                                    navController.navigate(HomeDestination.EmailVerification)
                                }

                                HandholdTask.Kyc -> {
                                    assetActionsNavigation.startKyc()
                                }

                                HandholdTask.BuyCrypto -> {
                                    assetActionsNavigation.navigate(AssetAction.Buy)
                                }
                            }
                        }
                    )

                    homeHelp(
                        openSupportCenter = { supportNavigation.launchSupportCenter() }
                    )
                }

                false -> {
                    // if user has no balance and kyc is rejected -> block custodial wallet
                    // should be shown if kyc rejected && balance == 0
                    val showBlockingKycCard = handholdViewState.showKycRejected
                        && (balance?.isZero ?: false)

                    when (showBlockingKycCard) {
                        true -> {
                            // blocked ⛔️
                            kycRejected(
                                onClick = {

                                }
                            )
                        }

                        false -> {
                            // unlocked/full dashboard here ✅

                            // kyc rejected warning, when user has balance > 0
                            // should be shown if kyc is rejected && balance > 0
                            val showKycRejectedWithBalance = handholdViewState.showKycRejected
                                && (balance?.isPositive ?: false)

                            if (showKycRejectedWithBalance) {
                                paddedItem(
                                    paddingValues = {
                                        PaddingValues(AppTheme.dimensions.smallSpacing)
                                    }
                                ) {
                                    CardAlert(
                                        title = stringResource(R.string.dashboard_kyc_rejected_with_balance_title),
                                        subtitle = stringResource(
                                            R.string.dashboard_kyc_rejected_with_balance_description
                                        ),
                                        isDismissable = false,
                                        alertType = AlertType.Warning,
                                        primaryCta = CardButton(
                                            text = stringResource(R.string.dashboard_kyc_rejected_with_balance_support),
                                            onClick = {
                                                context.openUrl(
                                                    "https://support.blockchain.com/hc/en-us/requests/new?ticket_form_id=4705355075996"
                                                ) // todo
                                            }
                                        )
                                    )
                                }
                            }

                            // anouncements
                            item {
                                (announcementsState.remoteAnnouncements as? DataResource.Data)?.data?.let { announcements ->
                                    StackedAnnouncements(
                                        announcements = announcements,
                                        hideConfirmation = announcementsState.hideAnnouncementsConfirmation,
                                        animateHideConfirmation = announcementsState.animateHideAnnouncementsConfirmation,
                                        announcementOnSwiped = { announcement ->
                                            announcementsViewModel.onIntent(
                                                AnnouncementsIntent.DeleteAnnouncement(announcement)
                                            )
                                        },
                                        announcementOnClick = { announcement ->
                                            processAnnouncementUrl(announcement.actionUrl)
                                            announcementsViewModel.onIntent(
                                                AnnouncementsIntent.AnnouncementClicked(announcement)
                                            )
                                        }
                                    )
                                }
                            }

                            announcementsState.localAnnouncements.takeIf { it.isNotEmpty() }
                                ?.let { localAnnouncements ->
                                    paddedItem(
                                        paddingValues = { PaddingValues(AppTheme.dimensions.smallSpacing) }
                                    ) {
                                        LocalAnnouncements(
                                            announcements = localAnnouncements,
                                            onClick = { announcement ->
                                                when (announcement.type) {
                                                    LocalAnnouncementType.PHRASE_RECOVERY -> startPhraseRecovery()
                                                }
                                            }
                                        )
                                    }
                                }

                            // assets
                            val assets = (assetsViewState.assets as? DataResource.Data)?.data
                            val locks = (assetsViewState.fundsLocks as? DataResource.Data)?.data

                            assets?.takeIf { it.isNotEmpty() }?.let { data ->
                                homeAssets(
                                    locks = locks,
                                    data = assets,
                                    openCryptoAssets = { openCryptoAssets(data.size) },
                                    assetOnClick = assetOnClick,
                                    fundsLocksOnClick = fundsLocksOnClick,
                                    openFiatActionDetail = openFiatActionDetail
                                )
                            }

                            // rb
                            rbViewState.recurringBuys
                                .map { state ->
                                    (state as? RecurringBuyEligibleState.Eligible)?.recurringBuys
                                }
                                .dataOrElse(null)
                                ?.let { recurringBuys ->
                                    homeRecurringBuys(
                                        analytics = analytics,
                                        recurringBuys = recurringBuys,
                                        manageOnclick = manageOnclick,
                                        upsellOnClick = upsellOnClick,
                                        recurringBuyOnClick = recurringBuyOnClick
                                    )
                                }

                            // top movers
                            homeTopMovers(
                                data = pricesViewState.topMovers.toImmutableList(),
                                assetOnClick = { asset ->
                                    assetActionsNavigation.coinview(asset)

                                    pricesViewState.topMovers.percentAndPositionOf(asset)
                                        ?.let { (percentageMove, position) ->
                                            analytics.logEvent(
                                                DashboardAnalyticsEvents.TopMoverAssetClicked(
                                                    ticker = asset.networkTicker,
                                                    percentageMove = percentageMove,
                                                    position = position
                                                )
                                            )
                                        }
                                }
                            )

                            // activity
                            homeActivityScreen(
                                activityState = activityViewState,
                                openActivity = openActivity,
                                openActivityDetail = openActivityDetail,
                                wMode = WalletMode.CUSTODIAL
                            )

                            // referral
                            (referralState.referralInfo as? DataResource.Data)?.data?.let {
                                (it as? ReferralInfo.Data)?.let {
                                    homeReferral(
                                        referralData = it,
                                        openReferral = openReferral
                                    )
                                }
                            }

                            // news
                            homeNews(
                                data = newsViewState.newsArticles?.toImmutableList(),
                                seeAllOnClick = {
                                    navController.navigate(HomeDestination.News)
                                }
                            )

                            // help
                            homeHelp(
                                openSupportCenter = { supportNavigation.launchSupportCenter() }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.size(AppTheme.dimensions.borderRadiiLarge))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DefiHomeDashboard(
    analytics: Analytics,

    isSwipingToRefresh: Boolean,

    listState: LazyListState,
    openSettings: () -> Unit,
    launchQrScanner: () -> Unit,

    processAnnouncementUrl: (String) -> Unit,
    startPhraseRecovery: () -> Unit,

    assetActionsNavigation: AssetActionsNavigation,
    openDexSwapOptions: () -> Unit,
    openMoreQuickActions: () -> Unit,

    assetOnClick: (AssetInfo) -> Unit,
    openCryptoAssets: (count: Int) -> Unit,
    fundsLocksOnClick: (FundsLocks) -> Unit,
    openFiatActionDetail: (String) -> Unit,

    onDappSessionClicked: (DappSessionUiElement) -> Unit,
    onWalletConnectSeeAllSessionsClicked: () -> Unit,

    openActivity: () -> Unit,
    openActivityDetail: (String, WalletMode) -> Unit,

    openReferral: () -> Unit,

    supportNavigation: SupportNavigation,

    showBackground: Boolean = false,
    showBalance: Boolean = false,
    balanceAlphaProvider: () -> Float,
    hideBalance: Boolean,
    menuOptionsHeightLoaded: (Int) -> Unit,
    balanceYPositionLoaded: (Float) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = LocalNavControllerProvider.current

    val quickActionsViewModel: QuickActionsViewModel = getViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner,
        scope = payloadScope
    )
    val quickActionsState: QuickActionsViewState by quickActionsViewModel.viewState.collectAsStateLifecycleAware()
    val maxQuickActions = maxQuickActionsOnScreen
    val announcementsViewModel: AnnouncementsViewModel = getViewModel(scope = payloadScope)
    val announcementsState: AnnouncementsViewState by announcementsViewModel.viewState.collectAsStateLifecycleAware()
    val homeAssetsViewModel: AssetsViewModel = getViewModel(scope = payloadScope)
    val assetsViewState: AssetsViewState by homeAssetsViewModel.viewState.collectAsStateLifecycleAware()
    val homeDappsViewModel: HomeDappsViewModel = getViewModel(scope = payloadScope)
    val homeDappsState: HomeDappsViewState by homeDappsViewModel.viewState.collectAsStateLifecycleAware()
    val activityViewModel: PrivateKeyActivityViewModel = getViewModel(scope = payloadScope)
    val activityViewState: ActivityViewState by activityViewModel.viewState.collectAsStateLifecycleAware()
    val referralViewModel: ReferralViewModel = getViewModel(scope = payloadScope)
    val referralState: ReferralViewState by referralViewModel.viewState.collectAsStateLifecycleAware()
    val newsViewModel: NewsViewModel = getViewModel(scope = payloadScope)
    val newsViewState: NewsViewState by newsViewModel.viewState.collectAsStateLifecycleAware()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // quick action
                quickActionsViewModel.onIntent(
                    QuickActionsIntent.LoadActions(
                        walletMode = WalletMode.NON_CUSTODIAL,
                        maxQuickActionsOnScreen = maxQuickActions
                    )
                )
                // announcements
                announcementsViewModel.onIntent(
                    AnnouncementsIntent.LoadAnnouncements(
                        walletMode = WalletMode.CUSTODIAL,
                    )
                )
                // accounts
                homeAssetsViewModel.onIntent(AssetsIntent.LoadFilters)
                homeAssetsViewModel.onIntent(
                    AssetsIntent.LoadAccounts(
                        walletMode = WalletMode.NON_CUSTODIAL,
                        sectionSize = SectionSize.Limited(MAX_ASSET_COUNT)
                    )
                )
                // dapps
                homeDappsViewModel.onIntent(HomeDappsIntent.LoadData)
                // activity
                activityViewModel.onIntent(ActivityIntent.LoadActivity(SectionSize.Limited(MAX_ACTIVITY_COUNT)))
                // referral
                referralViewModel.onIntent(ReferralIntent.LoadData())
                // news
                newsViewModel.onIntent(NewsIntent.LoadData)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = isSwipingToRefresh) {
        if (isSwipingToRefresh) {
            announcementsViewModel.onIntent(AnnouncementsIntent.Refresh)
            homeAssetsViewModel.onIntent(AssetsIntent.Refresh)
            quickActionsViewModel.onIntent(QuickActionsIntent.Refresh)
            activityViewModel.onIntent(ActivityIntent.Refresh())
            newsViewModel.onIntent(NewsIntent.Refresh)
        }
    }

    val balance = (assetsViewState.balance.balance as? DataResource.Data)?.data
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(
                AppTheme.colors.background
            )
    ) {
        stickyHeader {
            MenuOptionsScreen(
                modifier = Modifier.onGloballyPositioned {
                    menuOptionsHeightLoaded(it.size.height)
                },
                walletBalanceCurrency = balance?.symbol.orEmpty(),
                walletBalance = balance?.toStringWithoutSymbol().orEmpty(),
                openSettings = openSettings,
                launchQrScanner = launchQrScanner,
                showBackground = showBackground,
                showBalance = showBalance
            )
        }

        item {
            BalanceScreen(
                modifier = Modifier.onGloballyPositioned {
                    balanceYPositionLoaded(it.positionInParent().y)
                },
                balanceAlphaProvider = balanceAlphaProvider,
                hideBalance = hideBalance,
                walletBalance = assetsViewState.balance,
            )
        }

        paddedItem(
            paddingValues = {
                PaddingValues(AppTheme.dimensions.smallSpacing)
            }
        ) {
            QuickActions(
                quickActionItems = quickActionsState.actions,
                assetActionsNavigation = assetActionsNavigation,
                quickActionsViewModel = quickActionsViewModel, // lambdas
                openDexSwapOptions = openDexSwapOptions,
                dashboardState = dashboardState(assetsViewState, activityViewState),
                openMoreQuickActions = openMoreQuickActions
            )
        }

        // anouncements
        item {
            (announcementsState.remoteAnnouncements as? DataResource.Data)?.data?.let { announcements ->
                StackedAnnouncements(
                    announcements = announcements,
                    hideConfirmation = announcementsState.hideAnnouncementsConfirmation,
                    animateHideConfirmation = announcementsState.animateHideAnnouncementsConfirmation,
                    announcementOnSwiped = { announcement ->
                        announcementsViewModel.onIntent(
                            AnnouncementsIntent.DeleteAnnouncement(announcement)
                        )
                    },
                    announcementOnClick = { announcement ->
                        processAnnouncementUrl(announcement.actionUrl)
                        announcementsViewModel.onIntent(
                            AnnouncementsIntent.AnnouncementClicked(announcement)
                        )
                    }
                )
            }
        }

        announcementsState.localAnnouncements.takeIf { it.isNotEmpty() }
            ?.let { localAnnouncements ->
                paddedItem(
                    paddingValues = { PaddingValues(AppTheme.dimensions.smallSpacing) }
                ) {
                    LocalAnnouncements(
                        announcements = localAnnouncements,
                        onClick = { announcement ->
                            when (announcement.type) {
                                LocalAnnouncementType.PHRASE_RECOVERY -> startPhraseRecovery()
                            }
                        }
                    )
                }
            }

        // assets
        val assets = (assetsViewState.assets as? DataResource.Data)?.data
        assets?.takeIf { it.isNotEmpty() }?.let { data ->
            homeAssets(
                locks = null,
                data = assets,
                openCryptoAssets = { openCryptoAssets(data.size) },
                assetOnClick = assetOnClick,
                fundsLocksOnClick = fundsLocksOnClick,
                openFiatActionDetail = openFiatActionDetail
            )
        }

        // dapps
        homeDapps(
            homeDappsState = homeDappsState,
            openQrCodeScanner = launchQrScanner,
            onDappSessionClicked = onDappSessionClicked,
            onWalletConnectSeeAllSessionsClicked = onWalletConnectSeeAllSessionsClicked,
        )

        // activity
        homeActivityScreen(
            activityState = activityViewState,
            openActivity = openActivity,
            openActivityDetail = openActivityDetail,
            wMode = WalletMode.CUSTODIAL
        )

        // referral
        (referralState.referralInfo as? DataResource.Data)?.data?.let {
            (it as? ReferralInfo.Data)?.let {
                homeReferral(
                    referralData = it,
                    openReferral = openReferral
                )
            }
        }

        // news
        homeNews(
            data = newsViewState.newsArticles?.toImmutableList(),
            seeAllOnClick = {
                navController.navigate(HomeDestination.News)
            }
        )

        // help
        homeHelp(
            openSupportCenter = { supportNavigation.launchSupportCenter() }
        )

        item {
            Spacer(modifier = Modifier.size(AppTheme.dimensions.borderRadiiLarge))
        }
    }
}

private const val MAX_ASSET_COUNT = 7
private const val MAX_ACTIVITY_COUNT = 5
private const val MAX_RB_COUNT = 5

