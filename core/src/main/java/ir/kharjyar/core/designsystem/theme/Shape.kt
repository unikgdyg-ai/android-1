package ir.kharjyar.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val KharjYarShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

object KharjYarDimens {
    val MinTouchTarget = 48.dp
    val ScreenHorizontalPadding = 16.dp
    val CardInnerPadding = 18.dp
    val CardElevation = 2.dp
    val FloatingBottomBarHeight = 72.dp
    val FloatingBottomBarMargin = 16.dp
}
