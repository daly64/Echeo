package com.echeo.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.echeo.app.domain.ClientSummary
import com.echeo.app.domain.SubStatus
import com.echeo.app.domain.counts
import com.echeo.app.ui.clients.ClientFilter
import com.echeo.app.ui.clients.ClientsContent
import com.echeo.app.ui.components.EcheoBottomBar
import com.echeo.app.ui.components.Tab
import com.echeo.app.ui.dashboard.DashboardContent
import com.echeo.app.ui.screens.ActivationScreen
import com.echeo.app.ui.screens.LicenceInvalidScreen
import com.echeo.app.ui.screens.SettingsScreen
import com.echeo.app.ui.theme.EcheoColors
import com.echeo.app.ui.theme.EcheoTheme
import org.junit.Rule
import org.junit.Test

/** Captures d'écran des écrans, générées sur GitHub à chaque version. */
class ScreenshotsTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    private fun c(id: Long, name: String, initials: String, phone: String, subs: Int, status: SubStatus, next: String, day: Long, label: String) =
        ClientSummary(id, name, phone, initials, subs, status, next, day, label)

    private val sample = listOf(
        c(1, "Sami M.", "SM", "+216 22 456 789", 1, SubStatus.EXPIRED, "IPTV 6 mois", -1, "expiré hier"),
        c(2, "Karim B.", "KB", "+216 20 123 456", 2, SubStatus.SOON, "IPTV 12 mois", 3, "dans 3 jours"),
        c(3, "Nadia H.", "NH", "+216 55 987 321", 1, SubStatus.SOON, "Alpha 3 mois", 5, "dans 5 jours"),
        c(4, "Youssef T.", "YT", "+216 50 765 432", 3, SubStatus.ACTIVE, "IPTV 12 mois", 140, "dans 140 jours"),
        c(5, "Amine K.", "AK", "+216 98 111 222", 1, SubStatus.ACTIVE, "Netflix 3 mois", 60, "dans 60 jours"),
    )

    @Composable
    private fun Frame(tab: Tab, content: @Composable () -> Unit) {
        EcheoTheme {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(EcheoColors.GrisClair),
            ) {
                Box(Modifier.weight(1f)) { content() }
                EcheoBottomBar(selected = tab, onSelect = {})
            }
        }
    }

    @Test
    fun tableau_de_bord() = paparazzi.snapshot {
        Frame(Tab.Home) {
            DashboardContent("Ma boutique", sample.counts(), sample, {}, {}, {})
        }
    }

    @Test
    fun tableau_de_bord_vide() = paparazzi.snapshot {
        Frame(Tab.Home) {
            DashboardContent("Ma boutique", emptyList<ClientSummary>().counts(), emptyList(), {}, {}, {})
        }
    }

    @Test
    fun liste_des_clients() = paparazzi.snapshot {
        Frame(Tab.Clients) {
            ClientsContent(sample.sortedBy { it.name }, 8, "", ClientFilter.All, {}, {}, {}, {})
        }
    }

    @Test
    fun liste_des_clients_filtre_bientot() = paparazzi.snapshot {
        Frame(Tab.Clients) {
            ClientsContent(sample.sortedBy { it.name }, 8, "", ClientFilter.Soon, {}, {}, {}, {})
        }
    }

    @Test
    fun clients_etat_vide() = paparazzi.snapshot {
        Frame(Tab.Clients) {
            ClientsContent(emptyList(), 0, "", ClientFilter.All, {}, {}, {}, {})
        }
    }

    @Test
    fun activation() = paparazzi.snapshot {
        EcheoTheme { ActivationScreen("7RFV-JX2C", null, false, false, {}, {}, {}, {}) }
    }

    @Test
    fun activation_cle_refusee() = paparazzi.snapshot {
        EcheoTheme {
            ActivationScreen("7RFV-JX2C", "Votre demande a été refusée. Contactez votre fournisseur.", false, false, {}, {}, {}, {})
        }
    }

    @Test
    fun activation_en_attente() = paparazzi.snapshot {
        EcheoTheme { ActivationScreen("7RFV-JX2C", null, false, true, {}, {}, {}, {}) }
    }

    @Test
    fun licence_non_valide() = paparazzi.snapshot {
        EcheoTheme {
            LicenceInvalidScreen("7RFV-JX2C", "Licence expirée le 20/11/2027. Demandez une nouvelle clé.", {}, {}, {})
        }
    }

    @Test
    fun parametres_licence() = paparazzi.snapshot {
        Frame(Tab.Settings) {
            SettingsScreen(5, 9, true, 11, "7RFV-JX2C", "051GR-YB6WG-XQG2Z-GH6KP", "20/11/2027", {}, {}, {}, {}, {}, {}, {}, {})
        }
    }
}
