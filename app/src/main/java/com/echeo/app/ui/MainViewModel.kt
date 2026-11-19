package com.echeo.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.echeo.app.EcheoApp
import com.echeo.app.data.EcheoDatabase
import com.echeo.app.data.Settings
import com.echeo.app.domain.ClientSummary
import com.echeo.app.domain.DashboardCounts
import com.echeo.app.domain.counts
import com.echeo.app.domain.summary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HomeUiState(
    val loaded: Boolean = false,
    val shopName: String = Settings.DEFAULT_SHOP,
    val clients: List<ClientSummary> = emptyList(),
    val counts: DashboardCounts = DashboardCounts(),
    val subscriptionCount: Int = 0,
)

class MainViewModel(db: EcheoDatabase, settings: Settings) : ViewModel() {

    /** Date du jour, rafraîchie à chaque retour dans l'app. */
    private val today = MutableStateFlow(LocalDate.now().toEpochDay())

    fun refreshDate() {
        today.value = LocalDate.now().toEpochDay()
    }

    val state: StateFlow<HomeUiState> = combine(
        db.clientDao().observeAllWithSubscriptions(),
        db.subscriptionDao().observeCount(),
        settings.shopName,
        settings.warnDays,
        today,
    ) { clients, subCount, shop, warnDays, day ->
        val summaries = clients.map { it.summary(day, warnDays) }
        HomeUiState(
            loaded = true,
            shopName = shop,
            clients = summaries,
            counts = summaries.counts(),
            subscriptionCount = subCount,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    companion object {
        fun factory(app: EcheoApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainViewModel(app.database, app.settings) }
        }
    }
}
