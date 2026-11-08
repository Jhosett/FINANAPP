package com.ufpso.finanapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ufpso.finanapp.R
import com.ufpso.finanapp.ui.components.BottomDestination
import com.ufpso.finanapp.ui.components.FinanBottomBar
import com.ufpso.finanapp.ui.theme.ExpenseRed
import com.ufpso.finanapp.ui.theme.FINANAPPTheme
import com.ufpso.finanapp.ui.theme.GreenDark
import com.ufpso.finanapp.ui.theme.GreenPrimary
import com.ufpso.finanapp.ui.theme.IncomeGreen
import java.text.NumberFormat
import java.util.Locale

// Modelo SOLO para la UI. Cuando tengas TransactionEntity lo mapeas a este.
data class MovementUi(
    val id: Int,
    val title: String,
    val category: String,
    val date: String,
    val amount: Double,
    val isIncome: Boolean
)

@Composable
fun DashboardScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToManagement: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    modifier: Modifier = Modifier,
    // Valores vacíos por defecto: se llenarán desde el ViewModel/Room
    userName: String = "",
    balance: Double = 0.0,
    totalIncome: Double = 0.0,
    totalExpense: Double = 0.0,
    lastMovements: List<MovementUi> = emptyList()
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        // El encabezado verde se encarga de la barra de estado
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            FinanBottomBar(
                selected = BottomDestination.Panel,
                onSelect = { destination ->
                    when (destination) {
                        BottomDestination.Panel -> Unit // ya estamos aquí
                        BottomDestination.Registrar -> onNavigateToRegister()
                        BottomDestination.Historial -> onNavigateToManagement()
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
                BalanceHeader(
                    userName = userName,
                    balance = balance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    onRegisterClick = onNavigateToRegister
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Últimos movimientos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToManagement) {
                        Text("Ver todo", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (lastMovements.isEmpty()) {
                item { EmptyMovements(onRegisterClick = onNavigateToRegister) }
            } else {
                items(lastMovements, key = { it.id }) { movement ->
                    MovementItem(
                        movement = movement,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun BalanceHeader(
    userName: String,
    balance: Double,
    totalIncome: Double,
    totalExpense: Double,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.verticalGradient(listOf(GreenPrimary, GreenDark)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Fila superior: logo + saludo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.finanapp),
                contentDescription = "Logo de FinanApp",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (userName.isBlank()) "Hola" else "Hola, $userName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "FINANZAS PERSONALES",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Saldo
        Text(
            text = "SALDO DISPONIBLE",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = formatCurrency(balance),
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Calculado con todos tus movimientos registrados",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Ingresos y gastos
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                label = "INGRESOS",
                amount = totalIncome,
                icon = { Icon(Icons.Filled.ArrowUpward, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "GASTOS",
                amount = totalExpense,
                icon = { Icon(Icons.Filled.ArrowDownward, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRegisterClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = GreenDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar movimiento", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Double,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
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

@Composable
private fun MovementItem(movement: MovementUi, modifier: Modifier = Modifier) {
    val accent = if (movement.isIncome) IncomeGreen else ExpenseRed

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (movement.isIncome) Icons.Filled.ArrowUpward
                    else Icons.Filled.ArrowDownward,
                    contentDescription = if (movement.isIncome) "Ingreso" else "Gasto",
                    tint = accent
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movement.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${movement.category} • ${movement.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = (if (movement.isIncome) "+ " else "- ") + formatCurrency(movement.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun EmptyMovements(onRegisterClick: () -> Unit) {
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
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Aún no tienes movimientos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Registra tu primer ingreso o gasto para ver tu resumen aquí.",
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

@Preview(showBackground = true, showSystemUi = true, name = "Vacío")
@Composable
private fun DashboardEmptyPreview() {
    FINANAPPTheme {
        DashboardScreen(
            onNavigateToRegister = {},
            onNavigateToManagement = {},
            onNavigateToStatistics = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Con datos")
@Composable
private fun DashboardWithDataPreview() {
    FINANAPPTheme {
        DashboardScreen(
            onNavigateToRegister = {},
            onNavigateToManagement = {},
            onNavigateToStatistics = {},
            userName = "Laura",
            balance = 487_000.0,
            totalIncome = 2_820_000.0,
            totalExpense = 2_333_000.0,
            lastMovements = listOf(
                MovementUi(1, "Salario", "Trabajo", "08 nov", 2_820_000.0, true),
                MovementUi(2, "Mercado", "Alimentación", "07 nov", 185_000.0, false),
                MovementUi(3, "Transporte", "Movilidad", "06 nov", 42_000.0, false)
            )
        )
    }
}