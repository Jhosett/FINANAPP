package com.ufpso.finanapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Modelo completo de un movimiento para la pantalla (no depende de Room). */
data class MovementDetail(
    val id: Int,
    val description: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val date: LocalDate,
    val note: String
)

private enum class TypeFilter(val label: String) {
    All("Todos"), Income("Ingresos"), Expense("Gastos")
}

private val listDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "CO"))
private val fieldDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun ManagementScreen(
    onBack: () -> Unit, // vuelve al Panel
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    movements: List<MovementDetail> = emptyList(),
    totalIncome: Double = 0.0,
    totalExpense: Double = 0.0,
    onUpdate: (id: Int, movement: NewMovement) -> Unit = { _, _ -> },
    onDelete: (id: Int) -> Unit = {}
) {
    var typeFilter by rememberSaveable { mutableStateOf(TypeFilter.All) }
    var categoryFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<MovementDetail?>(null) }
    var deleting by remember { mutableStateOf<MovementDetail?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Categorías disponibles según el tipo elegido
    val categoryOptions = when (typeFilter) {
        TypeFilter.All -> expenseCategories + incomeCategories
        TypeFilter.Income -> incomeCategories
        TypeFilter.Expense -> expenseCategories
    }

    val filtered = remember(movements, typeFilter, categoryFilter) {
        movements.filter { m ->
            val typeOk = typeFilter == TypeFilter.All || (typeFilter == TypeFilter.Income) == m.isIncome
            val categoryOk = categoryFilter == null || m.category == categoryFilter
            typeOk && categoryOk
        }
    }

    val balance = totalIncome - totalExpense

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            FinanBottomBar(
                selected = BottomDestination.Historial,
                onSelect = { destination ->
                    when (destination) {
                        BottomDestination.Panel -> onBack()
                        BottomDestination.Registrar -> onNavigateToRegister()
                        BottomDestination.Historial -> Unit // ya estamos aquí
                        BottomDestination.Estadisticas -> onNavigateToStatistics()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ManagementHeader(
                    balance = balance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense
                )
            }

            item {
                FiltersSection(
                    typeFilter = typeFilter,
                    onTypeChange = {
                        typeFilter = it
                        categoryFilter = null // las categorías cambian con el tipo
                    },
                    categoryOptions = categoryOptions.map { it.name },
                    categoryFilter = categoryFilter,
                    onCategoryChange = { categoryFilter = it }
                )
            }

            item {
                Text(
                    text = if (filtered.size == 1) "1 movimiento" else "${filtered.size} movimientos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            when {
                movements.isEmpty() -> item {
                    EmptyState(
                        title = "Aún no tienes movimientos",
                        message = "Registra tu primer ingreso o gasto para verlo aquí.",
                        actionLabel = "Registrar movimiento",
                        onAction = onNavigateToRegister
                    )
                }
                filtered.isEmpty() -> item {
                    EmptyState(
                        title = "Sin resultados",
                        message = "No hay movimientos con los filtros elegidos.",
                        actionLabel = "Quitar filtros",
                        onAction = {
                            typeFilter = TypeFilter.All
                            categoryFilter = null
                        }
                    )
                }
                else -> items(filtered, key = { it.id }) { movement ->
                    MovementCard(
                        movement = movement,
                        onEdit = { editing = movement },
                        onDelete = { deleting = movement },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    // ----- Hoja de edición -----
    editing?.let { movement ->
        EditMovementSheet(
            movement = movement,
            onDismiss = { editing = null },
            onSave = { updated ->
                onUpdate(movement.id, updated)
                editing = null
                scope.launch { snackbarHostState.showSnackbar("Movimiento actualizado") }
            }
        )
    }

    // ----- Confirmación de borrado -----
    deleting?.let { movement ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Eliminar movimiento") },
            text = {
                Text("¿Seguro que quieres eliminar \"${movement.description}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(movement.id)
                    deleting = null
                    scope.launch { snackbarHostState.showSnackbar("Movimiento eliminado") }
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancelar") }
            }
        )
    }
}

// ============================================================
// Encabezado con balance y totales
// ============================================================

@Composable
private fun ManagementHeader(balance: Double, totalIncome: Double, totalExpense: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.verticalGradient(listOf(GreenPrimary, GreenDark)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "HISTORIAL",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.75f)
        )
        Text(
            text = "Mis movimientos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SALDO DISPONIBLE",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = formatBalance(balance),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TotalBox(
                label = "INGRESOS",
                amount = totalIncome,
                icon = Icons.Filled.ArrowUpward,
                modifier = Modifier.weight(1f)
            )
            TotalBox(
                label = "GASTOS",
                amount = totalExpense,
                icon = Icons.Filled.ArrowDownward,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TotalBox(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 0.8.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = formatMoney(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================
// Filtros
// ============================================================

@Composable
private fun FiltersSection(
    typeFilter: TypeFilter,
    onTypeChange: (TypeFilter) -> Unit,
    categoryOptions: List<String>,
    categoryFilter: String?,
    onCategoryChange: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FILTRAR",
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Por tipo
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypeFilter.entries.forEach { filter ->
                val color = when (filter) {
                    TypeFilter.All -> MaterialTheme.colorScheme.primary
                    TypeFilter.Income -> IncomeGreen
                    TypeFilter.Expense -> ExpenseRed
                }
                FilterChip(
                    selected = typeFilter == filter,
                    onClick = { onTypeChange(filter) },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.15f),
                        selectedLabelColor = color
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Por categoría (se desplaza horizontalmente)
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = categoryFilter == null,
                onClick = { onCategoryChange(null) },
                label = { Text("Todas") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            categoryOptions.forEach { name ->
                FilterChip(
                    selected = categoryFilter == name,
                    onClick = { onCategoryChange(name) },
                    label = { Text(name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

// ============================================================
// Tarjeta de movimiento
// ============================================================

@Composable
private fun MovementCard(
    movement: MovementDetail,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (movement.isIncome) IncomeGreen else ExpenseRed
    val categoryIcon = (expenseCategories + incomeCategories)
        .firstOrNull { it.name == movement.category }?.icon ?: Icons.Filled.LocalOffer

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = movement.category,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movement.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${movement.category} • ${movement.date.format(listDateFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = (if (movement.isIncome) "+ " else "- ") + formatMoney(movement.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }

            if (movement.note.isNotBlank()) {
                Text(
                    text = movement.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 56.dp, top = 6.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar", fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Eliminar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAction) { Text(actionLabel, fontWeight = FontWeight.Bold) }
        }
    }
}

// ============================================================
// Hoja de edición (mismas validaciones que al registrar)
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMovementSheet(
    movement: MovementDetail,
    onDismiss: () -> Unit,
    onSave: (NewMovement) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isIncome by remember { mutableStateOf(movement.isIncome) }
    var description by remember { mutableStateOf(movement.description) }
    var amountDigits by remember { mutableStateOf(movement.amount.toLong().toString()) }
    var date by remember { mutableStateOf(movement.date) }
    var category by remember { mutableStateOf<String?>(movement.category) }
    var note by remember { mutableStateOf(movement.note) }
    var showErrors by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val accent = if (isIncome) IncomeGreen else ExpenseRed
    val categories = if (isIncome) incomeCategories else expenseCategories
    val amountValue = amountDigits.toLongOrNull() ?: 0L

    val descriptionError = if (description.isBlank()) "Ingresa una descripción" else null
    val amountError = when {
        amountDigits.isEmpty() -> "Ingresa el valor del movimiento"
        amountValue <= 0L -> "El valor debe ser mayor que cero"
        else -> null
    }
    val categoryError = if (category == null) "Selecciona una categoría" else null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Editar movimiento",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Los campos con * son obligatorios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tipo
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeOption(
                    label = "Gasto",
                    icon = Icons.Filled.ArrowDownward,
                    color = ExpenseRed,
                    selected = !isIncome,
                    onClick = {
                        if (isIncome) {
                            isIncome = false
                            category = null
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

            Spacer(modifier = Modifier.height(16.dp))

            // Descripción
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
                            text = if (showErrors && descriptionError != null) descriptionError else "",
                            modifier = Modifier.weight(1f)
                        )
                        Text("${description.length}/$MAX_DESCRIPTION")
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Valor
            FieldLabel(text = "Valor en pesos", required = true)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountDigits,
                onValueChange = { new ->
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

            Spacer(modifier = Modifier.height(8.dp))

            // Fecha
            FieldLabel(text = "Fecha", required = true)
            Spacer(modifier = Modifier.height(6.dp))
            Box {
                OutlinedTextField(
                    value = date.format(fieldDateFormatter),
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
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(bottom = 22.dp)
                        .clickable { showDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Categoría
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

            Spacer(modifier = Modifier.height(16.dp))

            // Nota
            Text(
                text = "Nota (opcional)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(MAX_NOTE) },
                placeholder = { Text("Detalle adicional") },
                minLines = 2,
                maxLines = 4,
                supportingText = {
                    Text(
                        text = "${note.length}/$MAX_NOTE",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
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
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar cambios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancelar") }
        }
    }

    if (showDatePicker) {
        MovementDatePickerDialog(
            initial = date,
            onConfirm = {
                date = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovementDatePickerDialog(
    initial: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // El DatePicker trabaja en UTC; impedimos elegir fechas futuras
    val todayUtcMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayUtcMillis
            override fun isSelectableYear(year: Int) = year <= LocalDate.now().year
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { millis ->
                    onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                } ?: onDismiss()
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) {
        DatePicker(state = pickerState)
    }
}

// Si el saldo es negativo: "- $ 150.000"
private fun formatBalance(balance: Double): String =
    if (balance < 0) "- " + formatMoney(-balance) else formatMoney(balance)

@Preview(showBackground = true, showSystemUi = true, name = "Vacío")
@Composable
private fun ManagementEmptyPreview() {
    FINANAPPTheme { ManagementScreen(onBack = {}) }
}

@Preview(showBackground = true, showSystemUi = true, name = "Con datos")
@Composable
private fun ManagementWithDataPreview() {
    FINANAPPTheme {
        ManagementScreen(
            onBack = {},
            totalIncome = 2_820_000.0,
            totalExpense = 227_000.0,
            movements = listOf(
                MovementDetail(1, "Salario", 2_820_000.0, true, "Salario", LocalDate.now(), "Pago quincenal"),
                MovementDetail(2, "Mercado del mes", 185_000.0, false, "Alimentación", LocalDate.now().minusDays(1), ""),
                MovementDetail(3, "Bus y taxi", 42_000.0, false, "Transporte", LocalDate.now().minusDays(2), "Semana del 3 al 7")
            )
        )
    }
}