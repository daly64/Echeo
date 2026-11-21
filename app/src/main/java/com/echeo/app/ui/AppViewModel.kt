package com.echeo.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.echeo.app.EcheoApp
import com.echeo.app.data.ClientEntity
import com.echeo.app.data.EcheoDatabase
import com.echeo.app.data.EcheoRepository
import com.echeo.app.data.LicenceService
import com.echeo.app.data.RenewalEntity
import com.echeo.app.data.Settings
import com.echeo.app.data.SubscriptionTypeEntity
import com.echeo.app.domain.ClientSummary
import com.echeo.app.domain.DashboardCounts
import com.echeo.app.domain.SubView
import com.echeo.app.domain.buildSubViews
import com.echeo.app.domain.clientSummaries
import com.echeo.app.domain.counts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** Tout ce que l'interface affiche, recalculé à chaque changement de la base. */
private data class SettingsNow(val shop: String, val manager: String, val warn: Int, val day: Long)

data class AppData(
    val shopName: String = Settings.DEFAULT_SHOP,
    val managerName: String = Settings.DEFAULT_MANAGER,
    val clients: List<ClientEntity> = emptyList(),
    val summaries: List<ClientSummary> = emptyList(),
    val subViews: List<SubView> = emptyList(),
    val types: List<SubscriptionTypeEntity> = emptyList(),
    val counts: DashboardCounts = DashboardCounts(),
    val subscriptionCount: Int = 0,
    val today: Long = LocalDate.now().toEpochDay(),
)

class AppViewModel(app: EcheoApp) : ViewModel() {
    val settings: Settings = app.settings
    val repo: EcheoRepository = app.repository
    val licence: LicenceService = app.licence
    private val db: EcheoDatabase = app.database

    private val today = MutableStateFlow(LocalDate.now().toEpochDay())

    fun refreshDate() {
        today.value = LocalDate.now().toEpochDay()
    }

    fun renewalsFor(subId: Long): Flow<List<RenewalEntity>> = db.renewalDao().observeForSubscription(subId)

    val data: StateFlow<AppData> = combine(
        db.clientDao().observeAll(),
        db.subscriptionDao().observeAll(),
        db.subscriptionTypeDao().observeAll(),
        combine(settings.shopName, settings.managerName, settings.warnDays, today) { shop, manager, warn, day ->
            SettingsNow(shop, manager, warn, day)
        },
    ) { clients, subs, types, settingsNow ->
        val views = buildSubViews(clients, subs, types, settingsNow.day, settingsNow.warn)
        val summaries = clientSummaries(clients, views)
        AppData(
            shopName = settingsNow.shop,
            managerName = settingsNow.manager,
            clients = clients,
            summaries = summaries,
            subViews = views,
            types = types,
            counts = summaries.counts(),
            subscriptionCount = subs.size,
            today = settingsNow.day,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppData())

    companion object {
        fun factory(app: EcheoApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { AppViewModel(app) }
        }
    }
}
