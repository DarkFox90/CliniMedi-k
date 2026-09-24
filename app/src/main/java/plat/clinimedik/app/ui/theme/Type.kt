package plat.clinimedik.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import plat.clinimedik.app.R

val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

private val Base = Typography()

val CliniMedikTypography = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    displayMedium = Base.displayMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    displaySmall = Base.displaySmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    headlineLarge = Base.headlineLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    headlineMedium = Base.headlineMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    headlineSmall = Base.headlineSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    titleLarge = Base.titleLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    titleMedium = Base.titleMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.Normal),
    bodyMedium = Base.bodyMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Normal),
    bodySmall = Base.bodySmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Normal),
    labelLarge = Base.labelLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    labelMedium = Base.labelMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    labelSmall = Base.labelSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Medium)
)