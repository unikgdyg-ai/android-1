package ir.kharjyar.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ir.kharjyar.core.designsystem.theme.KharjYarDimens
import ir.kharjyar.core.money.Money
import ir.kharjyar.core.money.MoneyFormatter
import ir.kharjyar.domain.model.CurrencyUnit
import ir.kharjyar.domain.model.WorkspaceProfile
import ir.kharjyar.domain.model.WorkspaceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * ۱. نمایشگر انیمیشنی مبالغ (Count-up Animation) با پشتیبانی از Privacy Mode و اعداد فارسی
 */
@Composable
fun AnimatedMoneyText(
    money: Money,
    currency: CurrencyUnit,
    usePersianDigits: Boolean = true,
    privacyMode: Boolean = false,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    // برای انیمیشن نرم ۶۰fps از interpolate روی مقدار نمایشی استفاده می‌کنیم
    val targetDisplay = (money.minorUnits / currency.minorUnitMultiplier).toInt()
    val animatedDisplay by animateIntAsState(
        targetValue = targetDisplay,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "MoneyCountUp"
    )

    val animatedMoney = Money.fromDisplayValue(animatedDisplay.toLong(), currency)
    val formattedText = MoneyFormatter.format(
        money = if (animatedDisplay == targetDisplay) money else animatedMoney,
        currency = currency,
        usePersianDigits = usePersianDigits,
        privacyMode = privacyMode
    )

    AnimatedContent(
        targetState = privacyMode,
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
        label = "PrivacyModeTransition",
        modifier = modifier
    ) { isPrivate ->
        val displayStr = if (isPrivate) {
            MoneyFormatter.format(money, currency, usePersianDigits, privacyMode = true)
        } else {
            formattedText
        }
        Text(
            text = displayStr,
            style = style,
            color = color
        )
    }
}

/**
 * ۲. سوییچر سریع و انیمیشنی بین فضای کاری «شخصی» و «کسب‌وکار» همراه با دکمه Privacy Mode
 */
@Composable
fun WorkspaceHeaderBar(
    activeWorkspace: WorkspaceProfile,
    privacyMode: Boolean,
    onToggleWorkspaceType: () -> Unit,
    onTogglePrivacyMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KharjYarDimens.ScreenHorizontalPadding, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp,
            modifier = Modifier
                .heightIn(min = KharjYarDimens.MinTouchTarget)
                .clip(CircleShape)
                .clickable(
                    role = Role.Switch,
                    onClickLabel = "تغییر فضای کاری"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleWorkspaceType()
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (activeWorkspace.type == WorkspaceType.BUSINESS) {
                        Icons.Outlined.BusinessCenter
                    } else {
                        Icons.Outlined.PersonOutline
                    },
                    contentDescription = activeWorkspace.name,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activeWorkspace.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (activeWorkspace.type == WorkspaceType.BUSINESS) "کسب‌وکار" else "شخصی",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTogglePrivacyMode()
            },
            modifier = Modifier
                .size(KharjYarDimens.MinTouchTarget)
                .semantics {
                    contentDescription = if (privacyMode) "نمایش مبالغ" else "مخفی کردن مبالغ"
                }
        ) {
            Icon(
                imageVector = if (privacyMode) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ۳. افکت بارگذاری Skeleton / Shimmer با انیمیشن ۶۰fps
 */
@Composable
fun ShimmerSkeletonCard(
    modifier: Modifier = Modifier,
    height: Int = 88
) {
    val transition = rememberInfiniteTransition(label = "ShimmerInfinite")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 250f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(brush)
    )
}

/**
 * ۴. انیمیشن ورود پله‌ای آیتم‌های لیست (Staggered List Item Entrance)
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(28f) }

    LaunchedEffect(index) {
        delay((index.coerceAtMost(8) * 45L))
        launch {
            alphaAnim.animateTo(1f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        }
        launch {
            offsetYAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            alpha = alphaAnim.value
            translationY = offsetYAnim.value
        }
    ) {
        content()
    }
}

/**
 * ۵. کانتینر Swipe فنری (Spring Swipe-to-Edit / Swipe-to-Delete) با بازخورد Haptic
 */
@Composable
fun SpringSwipeableItem(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }
    val actionThreshold = 160f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
    ) {
        // پس‌زمینه اکشن‌ها
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "ویرایش",
                tint = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "حذف",
                tint = MaterialTheme.colorScheme.error
            )
        }

        // محتوای اصلی که با فنر جابه‌جا می‌شود
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value > actionThreshold -> {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onEdit()
                                    }
                                    offsetX.value < -actionThreshold -> {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onDelete()
                                    }
                                }
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val next = (offsetX.value + dragAmount).coerceIn(-240f, 240f)
                                offsetX.snapTo(next)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

/**
 * ۶. کامپوننت‌های استاندارد Empty State و Error State برای همه صفحات
 */
@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.size(84.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Inbox,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onActionClick,
                modifier = Modifier.heightIn(min = KharjYarDimens.MinTouchTarget)
            ) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "خطا",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.heightIn(min = KharjYarDimens.MinTouchTarget)
        ) {
            Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "تلاش مجدد")
        }
    }
}
