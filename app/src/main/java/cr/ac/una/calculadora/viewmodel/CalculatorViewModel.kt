package cr.ac.una.calculadora.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import cr.ac.una.calculadora.model.CalculatorState
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorViewModel : ViewModel() {

    var state = mutableStateOf(CalculatorState())

    fun onButtonClick(label: String) {
        when (label) {
            "AC" -> clear()
            "=" -> calculate()
            "⌫" -> deleteLast()
            else -> enterCharacter(label)
        }
    }

    private fun clear() {
        state.value = CalculatorState("0")
    }

    private fun deleteLast() {
        val current = state.value.operation
        if (current.length <= 1) {
            state.value = CalculatorState("0")
        } else {
            state.value = state.value.copy(operation = current.dropLast(1))
        }
    }

    private fun enterCharacter(char: String) {
        val current = state.value.operation

        // Si hay un error previo o el valor es "0", reiniciamos con el nuevo caracter
        if (current == "0" || current == "Error") {
            if (char.all { it.isDigit() } || char == "-") {
                state.value = state.value.copy(operation = char)
            } else if (char == ",") {
                state.value = state.value.copy(operation = "0,")
            } else {
                // Si es un operador (+, ×, etc) sobre un "0", lo concatenamos
                state.value = state.value.copy(operation = current + char)
            }
        } else {
            state.value = state.value.copy(operation = current + char)
        }
    }

    fun calculate() {
        try {
            val expression = state.value.operation
                .replace("×", "*")
                .replace("÷", "/")
                .replace(",", ".")

            val result = ExpressionBuilder(expression)
                .build()
                .evaluate()

            // Formatear el resultado
            val formattedResult = if (result % 1 == 0.0) {
                result.toLong().toString()
            } else {
                // Limitar a pocos decimales para que no se salga de la pantalla
                "%.4f".format(result).replace(".", ",").trimEnd('0').trimEnd(',')
            }

            state.value = CalculatorState(formattedResult)
        } catch (e: Exception) {
            // Si la expresión es inválida, mostrar un mensaje de error
            state.value = CalculatorState("Error")
        }
    }
}
