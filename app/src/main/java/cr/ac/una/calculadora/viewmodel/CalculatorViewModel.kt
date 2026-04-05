package cr.ac.una.calculadora.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import cr.ac.una.calculadora.model.CalculatorState
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorViewModel : ViewModel() {

    var state = mutableStateOf(CalculatorState())
    var showDigitLimitNotice = mutableStateOf(false)
    private var justCalculated = false
    private val maxDigitsPerOperand = 15

    fun onButtonClick(label: String) {
        when (label) {
            "AC" -> clear()
            "=" -> calculate()
            "⌫" -> deleteLast()
            "+/-" -> toggleSign()
            else -> enterCharacter(label)
        }
    }

    private fun clear() {
        state.value = CalculatorState("0")
        justCalculated = false
    }

    private fun deleteLast() {
        justCalculated = false
        val current = state.value.operation
        if (current.length <= 1) {
            state.value = CalculatorState("0")
        } else {
            state.value = state.value.copy(operation = current.dropLast(1))
        }
    }

    private fun enterCharacter(char: String) {
        val current = state.value.operation
        val operators = setOf("+", "-", "×", "÷", "%")
        val isDigit = char.all { it.isDigit() }
        val isOperator = char in operators

        if (justCalculated) {
            when {
                isDigit -> {
                    state.value = CalculatorState(char)
                    justCalculated = false
                    return
                }

                char == "," -> {
                    state.value = CalculatorState("0,")
                    justCalculated = false
                    return
                }

                isOperator -> {
                    state.value = state.value.copy(operation = current + char)
                    justCalculated = false
                    return
                }
            }
        }

        if (char == "," && hasDecimalInCurrentOperand(current)) {
            return
        }

        if (isDigit && shouldBlockDigitInput(current)) {
            showDigitLimitNotice.value = true
            return
        }

        // Si hay un error previo o el valor es "0", reiniciamos con el nuevo caracter
        if (current == "0" || current == "-0" || current == "Error") {
            if (isDigit || char == "-") {
                state.value = when {
                    current == "-0" && isDigit -> state.value.copy(operation = "-$char")
                    else -> state.value.copy(operation = char)
                }
            } else if (char == ",") {
                state.value = state.value.copy(operation = if (current == "-0") "-0," else "0,")
            } else {
                // Si es un operador (+, ×, etc) sobre un "0", lo concatenamos
                state.value = state.value.copy(operation = current + char)
            }
        } else {
            state.value = state.value.copy(operation = current + char)
        }

        justCalculated = false
    }

    private fun toggleSign() {
        justCalculated = false
        val current = state.value.operation

        if (current == "Error") {
            state.value = CalculatorState("0")
            return
        }

        if (current == "0") {
            state.value = CalculatorState("-0")
            return
        }

        val operators = setOf('+', '-', '×', '÷', '%')
        val lastChar = current.lastOrNull() ?: return

        if (lastChar in operators) {
            val hasPendingUnaryMinus =
                lastChar == '-' &&
                    current.length >= 2 &&
                    current[current.length - 2] in operators

            state.value = if (hasPendingUnaryMinus) {
                state.value.copy(operation = current.dropLast(1))
            } else {
                state.value.copy(operation = current + "-")
            }
            return
        }

        var i = current.length - 1
        while (i >= 0 && (current[i].isDigit() || current[i] == ',')) {
            i--
        }

        val numberStart = i + 1
        if (numberStart >= current.length) {
            return
        }

        val signIndex = numberStart - 1
        val hasUnaryMinus = signIndex >= 0 &&
            current[signIndex] == '-' &&
            (signIndex == 0 || current[signIndex - 1] in operators)

        val updated = if (hasUnaryMinus) {
            current.removeRange(signIndex, signIndex + 1)
        } else {
            current.substring(0, numberStart) + "-" + current.substring(numberStart)
        }

        state.value = state.value.copy(operation = updated)
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
            justCalculated = true
        } catch (_: Exception) {
            // Si la expresión es inválida, mostrar un mensaje de error
            state.value = CalculatorState("Error")
            justCalculated = false
        }
    }

    private fun hasDecimalInCurrentOperand(expression: String): Boolean {
        if (expression == "Error") return false

        val operators = setOf('+', '-', '×', '÷', '%')
        var i = expression.length - 1

        while (i >= 0) {
            val c = expression[i]
            when {
                c == ',' -> return true
                c in operators -> {
                    val isUnaryMinus = c == '-' && (i == 0 || expression[i - 1] in operators)
                    if (!isUnaryMinus) return false
                }
            }
            i--
        }

        return false
    }

    private fun shouldBlockDigitInput(current: String): Boolean {
        val replacingStates = current == "0" || current == "-0" || current == "Error"
        if (replacingStates) return false

        return currentOperandDigitCount(current) >= maxDigitsPerOperand
    }

    private fun currentOperandDigitCount(expression: String): Int {
        val operators = setOf('+', '-', '×', '÷', '%')
        var i = expression.length - 1
        var digits = 0

        while (i >= 0) {
            val c = expression[i]
            when {
                c.isDigit() -> digits++
                c == ',' -> Unit
                c in operators -> {
                    val isUnaryMinus = c == '-' && (i == 0 || expression[i - 1] in operators)
                    if (!isUnaryMinus) break
                }
            }
            i--
        }

        return digits
    }

    fun consumeDigitLimitNotice() {
        showDigitLimitNotice.value = false
    }
}
