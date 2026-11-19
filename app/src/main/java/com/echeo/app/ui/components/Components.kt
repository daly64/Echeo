package com.echeo.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echeo.app.R
import com.echeo.app.domain.SubStatus
import com.echeo.app.ui.theme.EcheoColors

/** En-tête bleu nuit en haut de chaque écran (passe sous l'encoche en plein écran). */
@Composable
fun EcheoHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EcheoColors.BleuNuit)
            .windowInsetsPadding(
                WindowInsets.statusBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Top),
            )
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
        content = content,
    )
}

@Composable
fun HeaderTitle(title: String, subtitle: String?) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontSize = 22.sp))
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSurBleu),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** Bouton principal turquoise, coins légèrement arrondis. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(EcheoColors.Turquoise)
            .clickable(onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontSize = 15.sp))
    }
}

/** Carte blanche bordée, utilisée pour les compteurs et les listes. */
@Composable
fun EcheoCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, EcheoColors.Bordure, RoundedCornerShape(12.dp)),
        content = content,
    )
}

fun statusText(status: SubStatus): String = when (status) {
    SubStatus.ACTIVE -> "Actif"
    SubStatus.SOON -> "Bientôt"
    SubStatus.EXPIRED -> "Expiré"
}

fun statusColors(status: SubStatus?): Pair<Color, Color> = when (status) {
    SubStatus.ACTIVE -> EcheoColors.ActifTexte to EcheoColors.ActifFond
    SubStatus.SOON -> EcheoColors.BientotTexte to EcheoColors.BientotFond
    SubStatus.EXPIRED -> EcheoColors.ExpireTexte to EcheoColors.ExpireFond
    null -> EcheoColors.TexteSecondaire to EcheoColors.NeutreFond
}

@Composable
fun StatusBadge(status: SubStatus, modifier: Modifier = Modifier) {
    val (fg, bg) = statusColors(status)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(statusText(status), style = MaterialTheme.typography.labelLarge.copy(color = fg, fontWeight = FontWeight.Bold))
    }
}

/** Pastille ronde avec les initiales ; colorée selon l'état, ou neutre. */
@Composable
fun Avatar(initials: String, status: SubStatus?, colored: Boolean, modifier: Modifier = Modifier) {
    val (fg, bg) = if (colored) statusColors(status) else EcheoColors.Texte to EcheoColors.NeutreFond
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = MaterialTheme.typography.titleSmall.copy(color = fg, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun RowDivider() {
    HorizontalDivider(thickness = 1.dp, color = EcheoColors.Separateur)
}

enum class Tab(val label: String, @DrawableRes val icon: Int) {
    Home("Accueil", R.drawable.ic_home),
    Clients("Clients", R.drawable.ic_users),
    Reminders("Relances", R.drawable.ic_bell),
    Settings("Paramètres", R.drawable.ic_settings),
}

/** Navigation du bas : Accueil, Clients, Relances, Paramètres. */
@Composable
fun EcheoBottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        RowDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Tab.entries.forEach { tab ->
                val isSelected = tab == selected
                val color = if (isSelected) EcheoColors.BleuNuit else EcheoColors.TexteDiscret
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(tab) }
                        .padding(vertical = 4.dp),
                ) {
                    Icon(painterResource(tab.icon), contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = color,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                    )
                }
            }
        }
    }
}

/** Ligne de liste générique : avatar, deux lignes de texte, badge d'état. */
@Composable
fun ClientRow(
    initials: String,
    title: String,
    subtitle: String,
    status: SubStatus?,
    coloredAvatar: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(initials, status, coloredAvatar)
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        if (status != null) StatusBadge(status)
    }
}

@Composable
fun EmptyIllustration(@DrawableRes icon: Int) {
    Icon(
        painterResource(icon),
        contentDescription = null,
        tint = EcheoColors.Turquoise,
        modifier = Modifier.size(72.dp),
    )
}

/** Icône utilisée dans les en-têtes (ex. cloche avec pastille). */
@Composable
fun HeaderIconButton(@DrawableRes icon: Int, showDot: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(EcheoColors.SurBleu)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        if (showDot) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 9.dp, end = 10.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(EcheoColors.Turquoise)
                    .border(1.5.dp, EcheoColors.BleuNuit, CircleShape),
            )
        }
    }
}
