package com.ufpso.finanapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ufpso.finanapp.ui.components.BottomDestination
import com.ufpso.finanapp.ui.components.FinanBottomBar
import com.ufpso.finanapp.ui.theme.ExpenseRed
import com.ufpso.finanapp.ui.theme.FINANAPPTheme
import com.ufpso.finanapp.ui.theme.GreenDark
import com.ufpso.finanapp.ui.theme.GreenPrimary
import com.ufpso.finanapp.ui.theme.IncomeGreen
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

internal const val MAX_DESCRIPTION = 60
internal const val MAX_NOTE = 200
internal const val MAX_AMOUNT_DIGITS = 12

/** Datos del formulario, listos para convertirse en una entidad de Room. */
data class NewMovement(
    val description: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val date: LocalDate,
    val note: String
)

internal data class CategoryOption(val name: String, val icon: ImageVector)

internal val expenseCategories = listOf(
    CategoryOption("Alimentación", Icons.Filled.Restaurant),
    CategoryOption("Vivienda", Icons.Filled.Home),
    CategoryOption("Transporte", Icons.Filled.DirectionsBus),
    CategoryOption("Servicios", Icons.Filled.Lightbulb),
    CategoryOption("Salud", Icons.Filled.Favorite),
    CategoryOption("Ocio", Icons.Filled.SportsEsports),
    CategoryOption("Educación", Icons.Filled.MenuBook),
    CategoryOption("Compras", Icons.Filled.ShoppingBag),
    CategoryOption("Otros gastos", Icons.Filled.LocalOffer)
)

internal val incomeCategories = listOf(
    CategoryOption("Salario", Icons.Filled.Payments),
    CategoryOption("Freelance", Icons.Filled.Work),
    CategoryOption("Inversiones", Icons.Filled.TrendingUp),
    CategoryOption("Regalos", Icons.Filled.CardGiftcard),
    CategoryOption("Ahorros", Icons.Filled.Savings),
    CategoryOption("Otros ingresos", Icons.Filled.LocalOffer)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMovementScreen(
    onBack: () -> Unit, // vuelve al Panel
    modifier: Modifier = Modifier,
    onNavigateToManagement: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onSave: (NewMovement) -> Unit = {}
) {
    // ---------- Estado del formulario ----------
    var isIncome by rememberSaveable { mutableStateOf(false) }
    var description by rememberSaveable { mutableStateOf("") }
    var amountDigits by rememberSaveable { mutableStateOf("") }
    var dateEpochDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val accent = if (isIncome) IncomeGreen else ExpenseRed
    val categories = if (isIncome) incomeCategories else expenseCategories
    val amountValue = amountDigits.toLongOrNull() ?: 0L
    val date = LocalDate.ofEpochDay(dateEpochDay)

    // ---------- Validaciones (null = válido) ----------
    val descriptionError = if (description.isBlank()) "Ingresa una descripción" else null
    val amountError = when {
        amountDigits.isEmpty() -> "Ingresa el valor del movimiento"
        amountValue <= 0L -> "El valor debe ser mayor que cero"
        else -> null
    }
    val categoryError = if (category == null) "Selecciona una categoría" else null

    fun attemptSave() {
        showErrors = true
        if (descriptionError == null && amountError == null && categoryError == null) {
            onSave(
                NewMovement(
                    description = description.trim(),
                    amount = amountValue.toDouble(),
                    isIncome = isIncome,
                    category = category!!,
                    date = date,
                    note = note.trim()
                )
            )
            // Limpiamos el formulario para un nuevo registro
            description = ""
            amountDigits = ""
            category = null
            note = ""
            showErrors = false
            scope.launch { snackbarHostState.showSnackbar("Movimiento guardado") }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            FinanBottomBar(
                selected = BottomDestination.Registrar,
                onSelect = { destination ->
                    when (destination) {
                        BottomDestination.Panel -> onBack()
                        BottomDestination.Registrar -> Unit
                        BottomDestination.Historial -> onNavigateToManagement()
                        BottomDestination.Estadisticas -> onNavigateToStatistics()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            MovementHeader(onBack = onBack)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // ----- Título de la tarjeta -----
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Datos del movimiento",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Los campos con * son obligatorios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // ----- Tipo de movimiento -----
                    Text(
                        text = "TIPO DE MOVIMIENTO",
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TypeOption(
                            label = "Gasto",
                            icon = Icons.Filled.ArrowDownward,
                            color = ExpenseRed,
                            selected = !isIncome,
                            onClick = {
                                if (isIncome) {
                                    isIncome = false
                                    category = null // las categorías cambian según el tipo
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        TypeOption(
                            label = "Ingreso",
                            icon = Icons.Filled.ArrowUpward,
                            color = IncomeGreen,
                            selected = isIncome,
                            onClick = {
                                if (!isIncome) {
                                    isIncome = true
                                    category = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ----- Descripción -----
                    FieldLabel(text = "Descripción", required = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it.take(MAX_DESCRIPTION) },
                        placeholder = { Text("Ej. Mercado del mes") },
                        singleLine = true,
                        isError = showErrors && descriptionError != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (showErrors && descriptionError != null) descriptionError
                                    else "Un nombre corto te ayudará a reconocerlo.",
                                    modifier = Modifier.weight(1f)
                                )
                                Text("${description.length}/$MAX_DESCRIPTION")
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- Valor -----
                    FieldLabel(text = "Valor en pesos", required = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = amountDigits,
                        onValueChange = { new ->
                            // Solo dígitos, con un máximo para evitar desbordes
                            amountDigits = new.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS)
                        },
                        placeholder = { Text("0") },
                        prefix = { Text("$ ") },
                        singleLine = true,
                        visualTransformation = ThousandsSeparatorTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = showErrors && amountError != null,
                        supportingText = {
                            Text(
                                if (showErrors && amountError != null) amountError
                                else "Solo números, mayor a cero."
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- Fecha -----
                    FieldLabel(text = "Fecha", required = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box {
                        OutlinedTextField(
                            value = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Elegir fecha",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            supportingText = { Text("Puedes registrar fechas pasadas o de hoy.") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Capa transparente: el campo es de solo lectura y abre el calendario
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(bottom = 22.dp)
                                .clickable { showDatePicker = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ----- Categoría -----
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FieldLabel(text = "Categoría", required = true)
                        Text(
                            text = if (isIncome) "INGRESOS" else "GASTOS",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = accent
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categories.chunked(2).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowItems.forEach { option ->
                                    CategoryChip(
                                        option = option,
                                        selected = category == option.name,
                                        accent = accent,
                                        onClick = { category = option.name },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Si la fila queda con un solo elemento, mantenemos el ancho
                                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    if (showErrors && categoryError != null) {
                        Text(
                            text = categoryError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ----- Nota (opcional) -----
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("Nota ") }
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                append("(opcional)")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it.take(MAX_NOTE) },
                        placeholder = { Text("Detalle adicional, por ejemplo el medio de pago o el cliente.") },
                        minLines = 3,
                        maxLines = 5,
                        supportingText = {
                            Text(
                                text = "${note.length}/$MAX_NOTE",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ----- Resumen del efecto en el saldo -----
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.10f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.Filled.ArrowUpward
                            else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isIncome) "Este ingreso sumará a tu saldo"
                            else "Este gasto restará de tu saldo",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = (if (isIncome) "+ " else "- ") + formatMoney(amountValue.toDouble()),
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ----- Guardar -----
                    Button(
                        onClick = { attemptSave() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guardar movimiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // ---------- Calendario ----------
    if (showDatePicker) {
        // El DatePicker trabaja en UTC; impedimos elegir fechas futuras
        val todayUtcMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayUtcMillis
                override fun isSelectableYear(year: Int) = year <= LocalDate.now().year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        dateEpochDay = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toEpochDay()
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// ============================================================
// Componentes
// ============================================================

@Composable
internal fun MovementHeader(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.verticalGradient(listOf(GreenPrimary, GreenDark)))
            .statusBarsPadding()
            .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver al panel",
                tint = Color.White
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = "NUEVO REGISTRO",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
            Text(
                text = "Registrar movimiento",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Agrega un ingreso o un gasto. Validamos cada campo antes de guardar.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
internal fun FieldLabel(text: String, required: Boolean = false) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(text) }
            if (required) {
                withStyle(SpanStyle(color = ExpenseRed, fontWeight = FontWeight.Bold)) { append(" *") }
            }
        },
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
internal fun TypeOption(
    label: String,
    icon: ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(if (selected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) color else MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun CategoryChip(
    option: CategoryOption,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(if (selected) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) accent else MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = null,
            tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = option.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

// ============================================================
// Utilidades
// ============================================================

/** Muestra 1234567 como "1.234.567" sin modificar el texto real (solo dígitos). */
internal class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val formatted = original.reversed().chunked(3).joinToString(".").reversed()
        val n = original.length

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Cuántos puntos quedan a la izquierda del cursor
                val dots = (1..minOf(offset, n - 1)).count { (n - it) % 3 == 0 }
                return offset + dots
            }

            override fun transformedToOriginal(offset: Int): Int =
                formatted.take(offset).count { it.isDigit() }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

// 2820000.0 -> "$ 2.820.000"
internal fun formatMoney(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }
    return "$ ${format.format(amount)}"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
internal fun RegisterMovementScreenPreview() {
    FINANAPPTheme {
        RegisterMovementScreen(onBack = {})
    }
}