package com.ufpso.finanapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
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
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/** Datos ya listos para pintar. Los calcula el ViewModel; aquí NO se calcula nada. */
data class StatisticsSummaryUi(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double
)

data class CategoryStatUi(
    val category: String,
    val total: Double,
    val percentage: Float // 0f..1f
)

private val ChartColors = listOf(
    Color(0xFF2E7D32),
    Color(0xFFF9A825),
    Color(0xFF1E88E5),
    Color(0xFF8E24AA),
    Color(0xFFE53935),
    Color(0xFF90A4AE)
)

private const val MAX_CATEGORIES = 5

/* ----------------------------------------------------------------------------
 * PANTALLA
 * -------------------------------------------------------------------------- */

@Composable
fun StatisticsScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToManagement: () -> Unit,
    modifier: Modifier = Modifier,
    // Valores vacíos por defecto: se llenarán desde el ViewModel
    balance: Double = 0.0,
    totalIncome: Double = 0.0,
    totalExpense: Double = 0.0,
    categories: List<CategoryStatUi> = emptyList() // ordenadas de mayor a menor gasto
) {
    val summary = StatisticsSummaryUi(
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        balance = balance
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            FinanBottomBar(
                selected = BottomDestination.Estadisticas,
                onSelect = { destination ->
                    when (destination) {
                        BottomDestination.Panel -> onNavigateToDashboard()
                        BottomDestination.Registrar -> onNavigateToRegister()
                        BottomDestination.Historial -> onNavigateToManagement()
                        BottomDestination.Estadisticas -> Unit // ya estamos aquí
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { StatisticsHeader(summary = summary) }

            if (categories.isEmpty() && totalIncome == 0.0 && totalExpense == 0.0) {
                item { EmptyStatistics(onRegisterClick = onNavigateToRegister) }
            } else {
                item {
                    IncomeVsExpenseCard(
                        summary = summary,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                item {
                    ExpenseDonutCard(
                        categories = categories,
                        totalExpense = summary.totalExpense,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                if (categories.isNotEmpty()) {
                    item {
                        ExpenseHistogramCard(
                            categories = categories,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
                item {
                    TopCategoriesCard(
                        categories = categories,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

/* ---------------------------- Encabezado + balance ------------------------- */

@Composable
private fun StatisticsHeader(summary: StatisticsSummaryUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.verticalGradient(listOf(GreenPrimary, GreenDark)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Resumen de tus finanzas",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "BALANCE DISPONIBLE",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = formatCurrency(summary.balance),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeaderStat(
                label = "INGRESOS",
                amount = summary.totalIncome,
                up = true,
                modifier = Modifier.weight(1f)
            )
            HeaderStat(
                label = "GASTOS",
                amount = summary.totalExpense,
                up = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeaderStat(
    label: String,
    amount: Double,
    up: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (up) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
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
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ------------------------- Ingresos vs Gastos (barras) --------------------- */

@Composable
private fun IncomeVsExpenseCard(summary: StatisticsSummaryUi, modifier: Modifier = Modifier) {
    val max = maxOf(summary.totalIncome, summary.totalExpense, 1.0)

    StatCard(title = "Ingresos vs. gastos", modifier = modifier) {
        HorizontalBar(
            label = "Ingresos",
            amount = summary.totalIncome,
            fraction = (summary.totalIncome / max).toFloat(),
            color = IncomeGreen
        )
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalBar(
            label = "Gastos",
            amount = summary.totalExpense,
            fraction = (summary.totalExpense / max).toFloat(),
            color = ExpenseRed
        )
    }
}

@Composable
private fun HorizontalBar(
    label: String,
    amount: Double,
    fraction: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/* ------------------------- Gráfico circular por categoría ------------------ */

@Composable
private fun ExpenseDonutCard(
    categories: List<CategoryStatUi>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    StatCard(title = "Gastos por categoría", modifier = modifier) {
        if (categories.isEmpty()) {
            Text(
                text = "Aún no hay gastos para graficar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@StatCard
        }

        val slices = categories
        val colors = slices.indices.map { ChartColors[it % ChartColors.size] }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            DonutChart(
                values = slices.map { it.percentage },
                colors = colors,
                modifier = Modifier.size(180.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total gastos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(totalExpense),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Leyenda
        slices.forEachIndexed { index, item ->
            val color = colors[index]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${(item.percentage * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DonutChart(
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 28.dp.toPx()
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val gap = if (values.size > 1) 2f else 0f

        var startAngle = -90f
        values.forEachIndexed { index, value ->
            val sweep = value * 360f
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

/* ------------------------- Ranking de categorías --------------------------- */

@Composable
private fun TopCategoriesCard(
    categories: List<CategoryStatUi>,
    modifier: Modifier = Modifier
) {
    StatCard(title = "Categorías con mayor gasto", modifier = modifier) {
        if (categories.isEmpty()) {
            Text(
                text = "Registra gastos para ver tu ranking de categorías.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@StatCard
        }

        categories.take(MAX_CATEGORIES).forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == 0) ExpenseRed
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (index == 0) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatCurrency(item.total),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { item.percentage.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = ExpenseRed,
                        trackColor = ExpenseRed.copy(alpha = 0.12f)
                    )
                }
            }
            if (index < minOf(categories.size, MAX_CATEGORIES) - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/* ------------------------- Piezas compartidas ------------------------------ */

@Composable
private fun StatCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun EmptyStatistics(onRegisterClick: () -> Unit) {
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
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sin datos para mostrar",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Registra ingresos y gastos para ver tus estadísticas.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRegisterClick) {
                Text("Registrar movimiento", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 12987300.0 -> "$ 12.987.300"
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }
    return "$ ${format.format(amount)}"
}

/* ------------------------- Histograma de gastos ---------------------------- */

private const val MAX_BARS = 6
private const val AXIS_INTERVALS = 4

private val HistogramColors = listOf(
    Color(0xFFD9693A), // naranja
    Color(0xFF9A3A1A), // marrón rojizo
    Color(0xFF2E8B57), // verde
    Color(0xFF8FD3AE), // verde claro
    Color(0xFFA39A80), // gris arena
    Color(0xFFF2A883)  // durazno
)

@Composable
private fun ExpenseHistogramCard(
    categories: List<CategoryStatUi>,
    modifier: Modifier = Modifier
) {
    val items = categories.take(MAX_BARS)
    if (items.isEmpty()) return

    // Escala del eje: solo para dibujar (4 tramos con números "redondos")
    val axisMax = remember(items) { niceAxisMax(items.maxOf { it.total }) }
    val labelWidth = 88.dp
    val rowHeight = 32.dp
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val barShape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Encabezado: título + chip "Top N"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Histograma de gastos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Categorías con mayor gasto acumulado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Top ${items.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Líneas guía punteadas + barras
            Box(modifier = Modifier.fillMaxWidth()) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val left = labelWidth.toPx()
                    val plotWidth = size.width - left
                    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    for (i in 0..AXIS_INTERVALS) {
                        val x = left + plotWidth * i / AXIS_INTERVALS
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dash
                        )
                    }
                }

                Column {
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .width(labelWidth)
                                    .padding(end = 10.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val fraction = (item.total / axisMax).toFloat().coerceIn(0.02f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .height(16.dp)
                                        .clip(barShape)
                                        .background(HistogramColors[index % HistogramColors.size])
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Etiquetas del eje horizontal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = labelWidth),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..AXIS_INTERVALS) {
                    Text(
                        text = axisLabel(axisMax * i / AXIS_INTERVALS),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Máximo "redondo" del eje: 4 tramos iguales (1, 1,5, 2, 2,5, 3, 5 o 10 × potencia de 10). */
private fun niceAxisMax(maxValue: Double): Double {
    if (maxValue <= 0.0) return AXIS_INTERVALS.toDouble()
    val rawStep = maxValue / AXIS_INTERVALS
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val step = listOf(1.0, 1.5, 2.0, 2.5, 3.0, 5.0, 10.0)
        .map { it * magnitude }
        .first { it >= rawStep }
    return step * AXIS_INTERVALS
}

// 0 -> "$ 0" | 1500000 -> "$ 1,5 M" | 250000 -> "$ 250 mil"
private fun axisLabel(value: Double): String = when {
    value <= 0.0 -> "$ 0"
    value >= 1_000_000 -> "$ ${compact(value / 1_000_000)} M"
    else -> "$ ${compact(value / 1_000)} mil"
}

private fun compact(x: Double): String {
    val rounded = (x * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString()
    else rounded.toString().replace('.', ',')
}

/* ----------------------------- PREVIEWS ------------------------------------ */

@Preview(showBackground = true, showSystemUi = true, name = "Vacío")
@Composable
private fun StatisticsEmptyPreview() {
    FINANAPPTheme {
        StatisticsScreen(
            onNavigateToDashboard = {},
            onNavigateToRegister = {},
            onNavigateToManagement = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Con datos")
@Composable
private fun StatisticsWithDataPreview() {
    FINANAPPTheme {
        StatisticsScreen(
            onNavigateToDashboard = {},
            onNavigateToRegister = {},
            onNavigateToManagement = {},
            balance = 487_000.0,
            totalIncome = 2_820_000.0,
            totalExpense = 2_333_000.0,
            categories = listOf(
                CategoryStatUi("Vivienda", 900_000.0, 0.39f),
                CategoryStatUi("Alimentación", 605_000.0, 0.26f),
                CategoryStatUi("Transporte", 342_000.0, 0.15f),
                CategoryStatUi("Compras", 250_000.0, 0.11f),
                CategoryStatUi("Ocio", 236_000.0, 0.09f),
                CategoryStatUi("Servicios", 120_000.0, 0.05f)
            )
        )
    }
}