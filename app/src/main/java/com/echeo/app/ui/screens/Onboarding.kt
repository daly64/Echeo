package com.echeo.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.echeo.app.R
import com.echeo.app.ui.components.*
import com.echeo.app.ui.theme.EcheoColors

@Composable
fun SplashScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(EcheoColors.BleuNuit),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(96.dp))
        Spacer(Modifier.height(16.dp))
        Text("Echeo", style = MaterialTheme.typography.headlineLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
    }
}

private val WELCOME_PAGES = listOf(
    "Suivez vos abonnements" to "Chaque client a ses abonnements, chacun avec sa propre date d'expiration.",
    "Ne manquez aucune relance" to "Echeo vous prévient avant les expirations. Appelez ou écrivez en un clic.",
    "Tout reste sur ce téléphone" to "Vos données restent ici. Une sauvegarde vous protège en cas de changement de téléphone.",
)

@Composable
fun WelcomeScreen(page: Int, onNext: () -> Unit, onFinish: () -> Unit) {
    val index = page.coerceIn(0, WELCOME_PAGES.lastIndex)
    val (title, text) = WELCOME_PAGES[index]
    val last = index == WELCOME_PAGES.lastIndex
    Column(Modifier.fillMaxSize().background(EcheoColors.GrisClair)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            CenteredMessage(
                icon = R.drawable.ic_bell,
                iconTint = Color.White,
                iconBg = EcheoColors.Turquoise,
                title = title,
                text = text,
            ) {}
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WELCOME_PAGES.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (i == index) EcheoColors.Turquoise else EcheoColors.Bordure),
                    )
                }
            }
            PrimaryButton(if (last) "Commencer" else "Suivant", onClick = if (last) onFinish else onNext)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun OfflineScreen(onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().background(EcheoColors.GrisClair)) {
        CenteredMessage(
            icon = R.drawable.ic_wifi_off,
            iconTint = EcheoColors.BleuNuit,
            iconBg = EcheoColors.Bordure,
            title = "Connexion Internet requise",
            text = "Echeo doit être connecté à Internet pour fonctionner. Activez le Wi-Fi ou les données mobiles.",
        ) {
            PrimaryButton("Réessayer", onRetry)
        }
    }
}
