package com.ufpso.finanapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ufpso.finanapp.ui.theme.ExpenseRed
import com.ufpso.finanapp.ui.theme.FINANAPPTheme
import com.ufpso.finanapp.ui.theme.IncomeGreen
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/** Totales de un día. Los calcula el ViewModel; aquí solo se dibujan. */
data class DailyStatUi(
    val date: LocalDate,
    val income: Double,
    val expense: Double
)

private val ChartHeight = 150.dp
private val AxisWidth = 52.dp
private const val TREND_INTERVALS = 4
private val trendLocale = Locale("es", "CO")

/**
 * Barras agrupadas: ingresos (verde) y gastos (rojo) de cada uno de los últimos 7 días.
 * Al tocar un día se muestra su detalle debajo del gráfico.
 */
@Composable
fun WeeklyTrendCard(
    days: List<DailyStatUi>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(days.size) { mutableStateOf(days.lastIndex) }
    val hasData = days.any { it.income > 0.0 || it.expense > 0.0 }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Actividad de los últimos 7 días",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ingresos y gastos por día. Toca un día para ver el detalle.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Leyenda
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(color = IncomeGreen, label = "Ingresos")
                LegendDot(color = ExpenseRed, label = "Gastos")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasData) {
                Text(
                    text = "Aún no hay movimientos en los últimos 7 días.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Card
            }

            val axisMax = remember(days) {
                trendAxisMax(days.maxOf { maxOf(it.income, it.expense) })
            }
            val gridColor = MaterialTheme.colorScheme.outlineVariant

            // ----- Gráfico -----
            Row(modifier = Modifier.fillMaxWidth()) {
                // Eje vertical
                Column(
                    modifier = Modifier
                        .width(AxisWidth)
                        .height(ChartHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in TREND_INTERVALS downTo 0) {
                        Text(
                            text = trendAxisLabel(axisMax * i / TREND_INTERVALS),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(ChartHeight)
                ) {
                    // Líneas guía punteadas
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        for (i in 0..TREND_INTERVALS) {
                            val y = size.height * i / TREND_INTERVALS
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dash
                            )
                        }
                    }

                    // Barras
                    Row(modifier = Modifier.fillMaxSize()) {
                        days.forEachIndexed { index, day ->
                            val dimmed = index != selectedIndex
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedIndex = index },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                TrendBar(day.income, axisMax, IncomeGreen, dimmed)
                                Spacer(modifier = Modifier.width(2.dp))
                                TrendBar(day.expense, axisMax, ExpenseRed, dimmed)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Etiquetas del eje horizontal: día de la semana y número
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AxisWidth + 6.dp)
            ) {
                days.forEachIndexed { index, day ->
                    val isToday = day.date == LocalDate.now()
                    val selected = index == selectedIndex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedIndex = index },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = weekdayShort(day.date),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday || selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ----- Detalle del día seleccionado -----
            days.getOrNull(selectedIndex)?.let { day ->
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = longDate(day.date),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DayAmount("Ingresos", day.income, IncomeGreen)
                        DayAmount("Gastos", day.expense, ExpenseRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendBar(value: Double, axisMax: Double, color: Color, dimmed: Boolean) {
    val fraction = (value / axisMax).toFloat().coerceIn(0f, 1f)
    val barHeight: Dp = if (value <= 0.0) 0.dp else (ChartHeight * fraction).coerceAtLeast(3.dp)
    Box(
        modifier = Modifier
            .width(10.dp)
            .height(barHeight)
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(color.copy(alpha = if (dimmed) 0.4f else 1f))
    )
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DayAmount(label: String, amount: Double, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = trendMoney(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Start
        )
    }
}

/* ------------------------- Utilidades (solo para dibujar) ------------------ */

// "lun" -> "Lun"
private fun weekdayShort(date: LocalDate): String =
    date.dayOfWeek.getDisplayName(TextStyle.SHORT, trendLocale)
        .take(3)
        .replaceFirstChar { it.uppercase() }

// "miércoles 04 de noviembre"
private fun longDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", trendLocale))
        .replaceFirstChar { it.uppercase() }

private fun trendMoney(amount: Double): String {
    val format = NumberFormat.getNumberInstance(trendLocale).apply { maximumFractionDigits = 0 }
    return "$ ${format.format(amount)}"
}

/** Máximo "redondo" del eje para 4 tramos iguales. */
private fun trendAxisMax(maxValue: Double): Double {
    if (maxValue <= 0.0) return TREND_INTERVALS.toDouble()
    val rawStep = maxValue / TREND_INTERVALS
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val step = listOf(1.0, 1.5, 2.0, 2.5, 3.0, 5.0, 10.0)
        .map { it * magnitude }
        .first { it >= rawStep }
    return step * TREND_INTERVALS
}

// 0 -> "$ 0" | 1500000 -> "$ 1,5 M" | 250000 -> "$ 250 mil"
private fun trendAxisLabel(value: Double): String = when {
    value <= 0.0 -> "$ 0"
    value >= 1_000_000 -> "$ ${trendCompact(value / 1_000_000)} M"
    else -> "$ ${trendCompact(value / 1_000)} mil"
}

private fun trendCompact(x: Double): String {
    val rounded = (x * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString()
    else rounded.toString().replace('.', ',')
}

@Preview(showBackground = true, name = "Últimos 7 días")
@Composable
private fun WeeklyTrendPreview() {
    val today = LocalDate.now()
    FINANAPPTheme {
        WeeklyTrendCard(
            days = listOf(
                DailyStatUi(today.minusDays(6), 0.0, 85_000.0),
                DailyStatUi(today.minusDays(5), 2_820_000.0, 120_000.0),
                DailyStatUi(today.minusDays(4), 0.0, 0.0),
                DailyStatUi(today.minusDays(3), 150_000.0, 342_000.0),
                DailyStatUi(today.minusDays(2), 0.0, 60_000.0),
                DailyStatUi(today.minusDays(1), 300_000.0, 0.0),
                DailyStatUi(today, 0.0, 185_000.0)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}