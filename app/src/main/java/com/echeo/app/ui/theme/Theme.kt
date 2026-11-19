package com.echeo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.echeo.app.R

/** Palette Echeo (identité visuelle validée). */
object EcheoColors {
    val BleuNuit = Color(0xFF0F2A4A)
    val Turquoise = Color(0xFF19B5A5)
    val Texte = Color(0xFF1B2A41)
    val GrisClair = Color(0xFFF1F4F8)

    val TexteSecondaire = Color(0xFF5B6B80)
    val TexteDiscret = Color(0xFF8593A6)
    val Bordure = Color(0xFFD9E0E8)
    val Separateur = Color(0xFFE6EBF1)
    val Blanc = Color.White

    /** Champs et puces sur fond bleu nuit. */
    val SurBleu = Color(0x1FFFFFFF)
    val TexteSurBleu = Color(0xB3FFFFFF)

    val ActifTexte = Color(0xFF1F6B45)
    val ActifFond = Color(0xFFE3F3EA)
    val BientotTexte = Color(0xFF8A4B0C)
    val BientotFond = Color(0xFFFDEBD3)
    val ExpireTexte = Color(0xFF8E2F2A)
    val ExpireFond = Color(0xFFF8E1DF)
    val NeutreFond = Color(0xFFE6EBF1)
}

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private val base = TextStyle(fontFamily = Inter, color = EcheoColors.Texte)

private val EcheoTypography = Typography(
    headlineMedium = base.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge = base.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleMedium = base.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
    titleSmall = base.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.copy(fontSize = 15.sp),
    bodyMedium = base.copy(fontSize = 14.sp),
    bodySmall = base.copy(fontSize = 13.sp),
    labelLarge = base.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = base.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = base.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun EcheoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = EcheoColors.Turquoise,
            onPrimary = Color.White,
            secondary = EcheoColors.BleuNuit,
            onSecondary = Color.White,
            background = EcheoColors.GrisClair,
            onBackground = EcheoColors.Texte,
            surface = Color.White,
            onSurface = EcheoColors.Texte,
            outline = EcheoColors.Bordure,
        ),
        typography = EcheoTypography,
        content = content,
    )
}
