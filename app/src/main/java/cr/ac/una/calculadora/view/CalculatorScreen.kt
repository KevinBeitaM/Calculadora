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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cr.ac.una.calculadora.viewmodel.CalculatorViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.rememberTextMeasurer

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {

    val viewModel: CalculatorViewModel = viewModel()
    val state by viewModel.state
    val showDigitLimitNotice by viewModel.showDigitLimitNotice

    LaunchedEffect(showDigitLimitNotice) {
        if (showDigitLimitNotice) {
            delay(1400)
            viewModel.consumeDigitLimitNotice()
        }
    }

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


        val displayText = state.display()
        var maxTextWidthPx by remember { mutableStateOf(0f) }
        val textMeasurer = rememberTextMeasurer()
        var displayFontSize by remember(displayText) { mutableStateOf(64.sp) }

        LaunchedEffect(displayText, maxTextWidthPx) {
            if (maxTextWidthPx <= 0f) return@LaunchedEffect

            var candidate = 64f
            while (candidate >= 24f) {
                val result = textMeasurer.measure(
                    text = AnnotatedString(displayText),
                    style = TextStyle(fontSize = candidate.sp),
                    maxLines = 1,
                    softWrap = false
                )
                if (result.size.width <= maxTextWidthPx) break
                candidate -= 1f
            }
            displayFontSize = candidate.coerceAtLeast(24f).sp
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 16.dp)
                .onSizeChanged { maxTextWidthPx = it.width.toFloat() },
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = displayText,
                    color = Color.White,
                    fontSize = displayFontSize,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier.requiredHeight(20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (showDigitLimitNotice) {
                    Text(
                        text = "No se pueden ingresar mas de 15 digitos",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .offset(y = (-2).dp)
                    )
                    }
                }
            }
        }

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
