package com.blockchain.home.presentation.accouncement

import androidx.lifecycle.viewModelScope
import com.blockchain.commonarch.presentation.mvi_v2.ModelConfigArgs
import com.blockchain.commonarch.presentation.mvi_v2.MviViewModel
import com.blockchain.componentlib.icons.Icons
import com.blockchain.componentlib.icons.Unlock
import com.blockchain.componentlib.theme.Pink600
import com.blockchain.componentlib.utils.ImageValue
import com.blockchain.componentlib.utils.TextValue
import com.blockchain.data.RefreshStrategy
import com.blockchain.data.updateDataWith
import com.blockchain.defiwalletbackup.domain.service.BackupPhraseService
import com.blockchain.home.announcements.AnnouncementsService
import com.blockchain.home.presentation.R
import com.blockchain.home.presentation.dashboard.HomeNavEvent
import com.blockchain.presentation.pulltorefresh.PullToRefresh
import com.blockchain.walletmode.WalletModeService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AnnouncementsViewModel(
    private val walletModeService: WalletModeService,
    private val backupPhraseService: BackupPhraseService,
    private val announcementsService: AnnouncementsService,
) : MviViewModel<
    AnnouncementsIntent,
    AnnouncementsViewState,
    AnnouncementModelState,
    HomeNavEvent,
    ModelConfigArgs.NoArgs>(
    AnnouncementModelState()
) {
    private var remoteAnnouncementsJob: Job? = null
    private var customAnnouncementsJob: Job? = null

    override fun viewCreated(args: ModelConfigArgs.NoArgs) {}

    override fun reduce(state: AnnouncementModelState): AnnouncementsViewState = state.run {
        AnnouncementsViewState(
            stackedAnnouncements = stackedAnnouncements,
            customAnnouncements = customAnnouncements
        )
    }

    override suspend fun handleIntent(modelState: AnnouncementModelState, intent: AnnouncementsIntent) {
        when (intent) {
            AnnouncementsIntent.LoadAnnouncements -> {
                loadRemoteAnnouncements(forceRefresh = false)
                loadCustomAnnouncements()
            }

            AnnouncementsIntent.Refresh -> {
                loadRemoteAnnouncements(forceRefresh = true)
            }
        }
    }

    private fun loadRemoteAnnouncements(forceRefresh: Boolean) {
        remoteAnnouncementsJob?.cancel()
        remoteAnnouncementsJob = viewModelScope.launch {
            announcementsService.announcements(
                PullToRefresh.freshnessStrategy(
                    shouldGetFresh = forceRefresh,
                    RefreshStrategy.RefreshIfOlderThan(amount = 15, unit = TimeUnit.MINUTES)
                )
            ).collectLatest { dataResource ->
                updateState {
                    it.copy(stackedAnnouncements = it.stackedAnnouncements.updateDataWith(dataResource))
                }
            }
        }
    }

    private fun loadCustomAnnouncements() {
        customAnnouncementsJob?.cancel()
        customAnnouncementsJob = viewModelScope.launch {
            walletModeService.walletMode.collectLatest { walletMode ->
                val announcements = mutableListOf<CustomAnnouncement>()

                backupPhraseService.shouldBackupPhraseForMode(walletMode).let { shouldBackup ->
                    if (shouldBackup) {
                        announcements.add(
                            CustomAnnouncement(
                                type = CustomAnnouncementType.PHRASE_RECOVERY,
                                title = TextValue.IntResValue(R.string.announcement_recovery_title),
                                subtitle = TextValue.IntResValue(R.string.announcement_recovery_subtitle),
                                icon = ImageValue.Local(Icons.Filled.Unlock.id, tint = Pink600),
                            )
                        )
                    }
                }

                updateState {
                    it.copy(customAnnouncements = announcements)
                }
            }
        }
    }
}
