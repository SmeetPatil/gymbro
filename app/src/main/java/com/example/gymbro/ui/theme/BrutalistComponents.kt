package com.example.gymbro.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Shape Constants ────────────────────────────────────────────────
val BrutalistShape = RoundedCornerShape(4.dp)

// ── Dynamic theme-aware colors ─────────────────────────────────────
object BrutalistColors {
    val border: Color @Composable get() = MaterialTheme.colorScheme.outline
    val shadow: Color @Composable get() =
        if (isSystemInDarkTheme()) BrutalistDarkShadow else BrutalistLightShadow
    val card: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val text: Color @Composable get() = MaterialTheme.colorScheme.onBackground
    val textMuted: Color @Composable get() =
        if (isSystemInDarkTheme()) BrutalistDarkTextMuted else BrutalistLightTextMuted
    val background: Color @Composable get() = MaterialTheme.colorScheme.background
    val navBar: Color @Composable get() =
        if (isSystemInDarkTheme()) BrutalistDarkNavBar else BrutalistLightNavBar
}

// ── Hard Shadow Modifier (canvas-based — never clips) ──────────────

/**
 * Draws a hard offset shadow behind the composable using Canvas drawBehind.
 * The shadow is rendered as a solid rounded rectangle shifted by [offsetX]/[offsetY].
 * Padding is added to reserve layout space so the shadow doesn't overlap siblings.
 */
@Composable
fun Modifier.brutalistShadow(
    offsetX: Dp = 5.dp,
    offsetY: Dp = 5.dp,
    cornerRadius: Dp = 4.dp,
): Modifier {
    val shadowColor = BrutalistColors.shadow
    return this
        .padding(end = offsetX, bottom = offsetY)
        .drawBehind {
            drawRoundRect(
                color = shadowColor,
                topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
                size = size,
                cornerRadius = CornerRadius(cornerRadius.toPx()),
            )
        }
}

// ── Brutalist Card ─────────────────────────────────────────────────

@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    shadowOffset: Dp = 5.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val borderColor = BrutalistColors.border
    val cardBg = BrutalistColors.card
    val shadowColor = BrutalistColors.shadow
    val cr = 4.dp

    Card(
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
            .drawBehind {
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(cr.toPx()),
                )
            },
        shape = BrutalistShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(2.dp, accentColor ?: borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}

// ── Brutalist Button ───────────────────────────────────────────────

@Composable
fun BrutalistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = BrutalistYellow,
    textColor: Color = BrutalistLightText,
    shadowOffset: Dp = 4.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val borderColor = BrutalistColors.border
    val shadowColor = BrutalistColors.shadow
    val cr = 4.dp

    Button(
        onClick = onClick,
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
            .drawBehind {
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(cr.toPx()),
                )
            },
        shape = BrutalistShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = textColor,
        ),
        border = BorderStroke(2.dp, borderColor),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        content = content,
    )
}

// ── Brutalist Outlined Button ──────────────────────────────────────

@Composable
fun BrutalistOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val shadowColor = BrutalistColors.shadow
    val cr = 4.dp
    val shadowOffset = 3.dp

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
            .drawBehind {
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(cr.toPx()),
                )
            },
        shape = BrutalistShape,
        border = BorderStroke(2.dp, BrutalistColors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BrutalistColors.text,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        content = content,
    )
}

// ── Brutalist Text Field ───────────────────────────────────────────

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default,
    singleLine: Boolean = true,
) {
    val borderColor = BrutalistColors.border
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        shape = BrutalistShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrutalistYellow,
            unfocusedBorderColor = borderColor,
            focusedLabelColor = BrutalistYellow,
            cursorColor = BrutalistYellow,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
    )
}

// ── Brutalist Divider ──────────────────────────────────────────────

@Composable
fun BrutalistDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier,
        thickness = 2.dp,
        color = BrutalistColors.border,
    )
}

// ── Brutalist Chip ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrutalistFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.border(
            width = 2.dp,
            color = BrutalistColors.border,
            shape = BrutalistShape,
        ),
        shape = BrutalistShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrutalistYellow,
            selectedLabelColor = BrutalistLightText,
            containerColor = BrutalistColors.card,
            labelColor = BrutalistColors.text,
        ),
        border = null, // We draw our own
        elevation = FilterChipDefaults.filterChipElevation(
            elevation = 0.dp,
            pressedElevation = 0.dp,
        ),
    )
}

// ── Brutalist Dialog (wraps AlertDialog) ───────────────────────────

@Composable
fun BrutalistDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = text,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        shape = BrutalistShape,
        containerColor = BrutalistColors.card,
        titleContentColor = BrutalistColors.text,
        textContentColor = BrutalistColors.text,
        tonalElevation = 0.dp,
    )
}
