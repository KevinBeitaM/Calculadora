package cr.ac.una.calculadora.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cr.ac.una.calculadora.viewmodel.CalculatorViewModel

@Composable
fun AdvancedCalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state

    val rows = listOf(
        listOf("(", ")", "⌫", "AC"),
        listOf("sin", "cos", "tan", "√", "^"),
        listOf("x²", "x³", "%", "÷", "×"),
        listOf("7", "8", "9", "-", "+"),
        listOf("4", "5", "6", ",", "="),
        listOf("1", "2", "3", "0")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C))
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Volver a calculadora")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = state.display(),
                color = Color.White,
                fontSize = 44.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        AdvancedButton(
                            text = label,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onButtonClick(label) }
                        )
                    }

                    if (row.size < 5) {
                        repeat(5 - row.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when (text) {
        "=", "+", "-", "×", "÷", "^" -> Color(0xFFFF9500)
        "AC", "⌫" -> Color(0xFFA5A5A5)
        else -> Color(0xFF333333)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

