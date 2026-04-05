package cr.ac.una.calculadora.view

import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cr.ac.una.calculadora.viewmodel.CalculatorViewModel

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {

    val viewModel: CalculatorViewModel = viewModel()
    val state by viewModel.state

    val buttons = listOf(
        listOf("⌫","AC","%","÷"),
        listOf("7","8","9","×"),
        listOf("4","5","6","-"),
        listOf("1","2","3","+"),
        listOf("+/-","0",",","=")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C))
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {


        Text(
            text = state.display(),
            color = Color.White,
            fontSize = 64.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Column {

            buttons.forEach { row ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    row.forEach { label ->



                        CalculatorButton(
                            text = label,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.onButtonClick(label)
                            }
                        )

                    }

                }

            }

        }

    }

}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {

    val backgroundColor = when (text) {

        "+","-","×","÷","=" ->
            Color(0xFFFF9500)

        "⌫","AC","%","+/-" ->
            Color(0xFFA5A5A5)

        else ->
            Color(0xFF333333)
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
            fontSize = 26.sp
        )

    }

}
