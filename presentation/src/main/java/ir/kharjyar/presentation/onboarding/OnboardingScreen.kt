package ir.kharjyar.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.kharjyar.core.designsystem.theme.KharjYarDimens
import ir.kharjyar.core.money.ExpressionEvaluator
import ir.kharjyar.core.money.Money
import ir.kharjyar.domain.model.AccountType
import ir.kharjyar.domain.model.CurrencyUnit

/**
 * مدل خروجی ساخت اولین حساب در پایان Onboarding سه‌مرحله‌ای
 */
data class FirstAccountSetupData(
    val accountName: String,
    val accountType: AccountType,
    val initialBalance: Money,
    val selectedCurrency: CurrencyUnit
)

/**
 * صفحه Onboarding سه‌مرحله‌ای با انیمیشن انتقال نرم و ساخت اولین حساب
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: (FirstAccountSetupData) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var accountName by rememberSaveable { mutableStateOf("کارت بانکی اصلی") }
    var selectedAccountType by rememberSaveable { mutableStateOf(AccountType.CARD) }
    var selectedCurrency by rememberSaveable { mutableStateOf(CurrencyUnit.TOMAN) }
    var initialBalanceInput by rememberSaveable { mutableStateOf("0") }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // نشانگر مرحله (Step Indicator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { step ->
                val isCurrent = step == currentStep
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (isCurrent) 32.dp else 10.dp)
                        .clip(CircleShape)
                        .background(
                            if (step <= currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        // محتوای هر مرحله
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                (slideInHorizontally(tween(320)) { width -> -width / 3 } + fadeIn(tween(320)))
                    .togetherWith(slideOutHorizontally(tween(250)) { width -> width / 3 } + fadeOut(tween(250)))
            },
            label = "OnboardingStepContent",
            modifier = Modifier.weight(1f)
        ) { step ->
            when (step) {
                0 -> OnboardingFeatureSlide(
                    icon = Icons.Outlined.Workspaces,
                    title = "مدیریت هوشمند مالی شخصی و کسب‌وکار",
                    description = "با «خرجیار» حساب‌های زندگی شخصی و چندین کسب‌وکار خود را در دو فضای کاری کاملاً مجزا با تم اختصاصی، بودجه‌بندی هوشمند و تقویم شمسی مدیریت کنید."
                )
                1 -> OnboardingFeatureSlide(
                    icon = Icons.Outlined.Analytics,
                    title = "دقت ۱۰۰٪ ریالی، نمودارهای زنده و امنیت کامل",
                    description = "محاسبات مالی بدون خطای اعشاری، ثبت فاکتور و چک، نمودارهای تعاملی، قفل بیومتریک و بکاپ رمزنگاری‌شده خودکار همیشه همراه شماست."
                )
                else -> FirstAccountFormSlide(
                    accountName = accountName,
                    onAccountNameChange = {
                        accountName = it
                        validationError = null
                    },
                    selectedAccountType = selectedAccountType,
                    onAccountTypeSelected = { selectedAccountType = it },
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = { selectedCurrency = it },
                    initialBalanceInput = initialBalanceInput,
                    onInitialBalanceChange = {
                        initialBalanceInput = it
                        validationError = null
                    },
                    validationError = validationError
                )
            }
        }

        // دکمه‌های ناوبری پایین صفحه
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.heightIn(min = KharjYarDimens.MinTouchTarget)
                ) {
                    Text(text = "مرحله قبل")
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = {
                    if (currentStep < 2) {
                        currentStep++
                    } else {
                        val trimmedName = accountName.trim()
                        val evaluatedBalance = ExpressionEvaluator.evaluateToLong(initialBalanceInput)
                        when {
                            trimmedName.isEmpty() -> {
                                validationError = "لطفاً نام اولین حساب خود را وارد کنید."
                            }
                            evaluatedBalance == null || evaluatedBalance < 0L -> {
                                validationError = "موجودی اولیه نمی‌تواند منفی یا نامعتبر باشد."
                            }
                            else -> {
                                onFinishOnboarding(
                                    FirstAccountSetupData(
                                        accountName = trimmedName,
                                        accountType = selectedAccountType,
                                        initialBalance = Money.fromDisplayValue(
                                            evaluatedBalance,
                                            selectedCurrency
                                        ),
                                        selectedCurrency = selectedCurrency
                                    )
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.heightIn(min = KharjYarDimens.MinTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (currentStep < 2) "ادامه" else "ساخت حساب و ورود به خرجیار")
            }
        }
    }
}

@Composable
private fun OnboardingFeatureSlide(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(112.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(54.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FirstAccountFormSlide(
    accountName: String,
    onAccountNameChange: (String) -> Unit,
    selectedAccountType: AccountType,
    onAccountTypeSelected: (AccountType) -> Unit,
    selectedCurrency: CurrencyUnit,
    onCurrencySelected: (CurrencyUnit) -> Unit,
    initialBalanceInput: String,
    onInitialBalanceChange: (String) -> Unit,
    validationError: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ساخت اولین حساب مالی",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            OutlinedTextField(
                value = accountName,
                onValueChange = onAccountNameChange,
                label = { Text("نام حساب (مثلاً کارت ملت، صندوق نقد)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "نوع حساب:",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedAccountType == type,
                        onClick = { onAccountTypeSelected(type) },
                        label = { Text(type.titleFa) }
                    )
                }
            }

            Text(
                text = "واحد پول پیش‌فرض:",
                style = MaterialTheme.typography.labelLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrencyUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = selectedCurrency == unit,
                        onClick = { onCurrencySelected(unit) },
                        label = { Text(unit.symbolFa) }
                    )
                }
            }

            OutlinedTextField(
                value = initialBalanceInput,
                onValueChange = onInitialBalanceChange,
                label = { Text("موجودی اولیه (${selectedCurrency.symbolFa}) — پشتیبانی از ماشین‌حساب") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (validationError != null) {
                Text(
                    text = validationError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
