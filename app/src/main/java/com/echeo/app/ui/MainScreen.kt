package com.echeo.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.echeo.app.ui.clients.ClientFilter
import com.echeo.app.ui.clients.ClientsContent
import com.echeo.app.ui.components.EcheoBottomBar
import com.echeo.app.ui.components.EcheoHeader
import com.echeo.app.ui.components.HeaderTitle
import com.echeo.app.ui.components.Tab
import com.echeo.app.ui.dashboard.DashboardContent
import com.echeo.app.ui.theme.EcheoColors

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ClientFilter.All) }
    val context = LocalContext.current

    // Écrans des prochaines étapes : un simple message en attendant.
    val comingSoon: (String) -> Unit = { what ->
        Toast.makeText(context, "$what : prochaine étape du développement", Toast.LENGTH_SHORT).show()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(EcheoColors.GrisClair),
    ) {
        Box(Modifier.weight(1f)) {
            if (state.loaded) {
                when (tab) {
                    Tab.Home -> DashboardContent(
                        shopName = state.shopName,
                        counts = state.counts,
                        clients = state.clients,
                        onOpenReminders = { tab = Tab.Reminders },
                        onOpenClient = { comingSoon("Fiche client") },
                        onAddClient = { comingSoon("Nouveau client") },
                    )
                    Tab.Clients -> ClientsContent(
                        clients = state.clients,
                        subscriptionCount = state.subscriptionCount,
                        query = query,
                        filter = filter,
                        onQueryChange = { query = it },
                        onFilterChange = { filter = it },
                        onAddClient = { comingSoon("Nouveau client") },
                        onOpenClient = { comingSoon("Fiche client") },
                    )
                    Tab.Reminders -> PlaceholderScreen("Relances")
                    Tab.Settings -> PlaceholderScreen("Paramètres")
                }
            }
        }
        EcheoBottomBar(selected = tab, onSelect = { tab = it })
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Column(Modifier.fillMaxSize()) {
        EcheoHeader { HeaderTitle(title, null) }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Cet écran arrive dans une prochaine version.",
                style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
                textAlign = TextAlign.Center,
            )
        }
    }
}
