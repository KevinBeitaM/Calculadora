package cr.ac.una.calculadora.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import cr.ac.una.calculadora.model.CalculatorState
import net.objecthunter.exp4j.ExpressionBuilder
import java.util.Locale

class CalculatorViewModel : ViewModel() {

    var state = mutableStateOf(CalculatorState())
    var showDigitLimitNotice = mutableStateOf(false)
    private var justCalculated = false
    private val maxDigitsPerOperand = 15
    private val operators = setOf("+", "-", "×", "÷", "^")
    private val operatorChars = setOf('+', '-', '×', '÷', '^')

    fun onButtonClick(label: String) {
        when (label) {
            "AC" -> clear()
            "=" -> calculate()
            "⌫" -> deleteLast()
            "+/-" -> toggleSign()
            "%" -> applyPercentToCurrentOperand()
            "√" -> enterFunction("sqrt(")
            "sin" -> enterFunction("sin(")
            "cos" -> enterFunction("cos(")
            "tan" -> enterFunction("tan(")
            "(" -> enterOpenParenthesis()
            ")" -> enterCloseParenthesis()
            "x²" -> enterPowerShortcut(2)
            "x³" -> enterPowerShortcut(3)
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

        if (isOperator) {
            val last = current.lastOrNull()
            if (last != null && last.toString() in operators) {
                if (char == "-" && last != '-') {
                    state.value = state.value.copy(operation = current + char)
                } else {
                    state.value = state.value.copy(operation = current.dropLast(1) + char)
                }
                justCalculated = false
                return
            }

            if (last == '(' && char != "-") {
                return
            }
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

    private fun enterFunction(functionToken: String) {
        val current = state.value.operation

        if (justCalculated || current == "0" || current == "-0" || current == "Error") {
            state.value = CalculatorState(functionToken)
            justCalculated = false
            return
        }

        val last = current.lastOrNull()
        val needsMultiplication = last?.isDigit() == true || last == ')'
        val updated = if (needsMultiplication) "$current×$functionToken" else "$current$functionToken"
        state.value = state.value.copy(operation = updated)
        justCalculated = false
    }

    private fun enterOpenParenthesis() {
        val current = state.value.operation

        if (justCalculated || current == "Error") {
            state.value = CalculatorState("(")
            justCalculated = false
            return
        }

        val updated = when {
            current == "0" || current == "-0" -> "("
            current.lastOrNull()?.isDigit() == true || current.lastOrNull() == ')' -> "$current×("
            else -> "$current("
        }

        state.value = state.value.copy(operation = updated)
        justCalculated = false
    }

    private fun enterCloseParenthesis() {
        val current = state.value.operation
        if (current == "Error") return
        if (openParenthesisCount(current) <= 0) return

        val last = current.lastOrNull() ?: return
        if (last.toString() in operators || last == '(') return

        state.value = state.value.copy(operation = "$current)")
        justCalculated = false
    }

    private fun enterPowerShortcut(exponent: Int) {
        val current = state.value.operation
        if (current == "Error") return

        val base = if (justCalculated) {
            justCalculated = false
            current
        } else {
            current
        }

        val last = base.lastOrNull() ?: return
        if (last.isDigit() || last == ')') {
            state.value = state.value.copy(operation = "$base^$exponent")
        }
    }

    private fun applyPercentToCurrentOperand() {
        val current = state.value.operation
        if (current == "Error") return

        val end = current.length
        if (end == 0) return

        var i = end - 1
        while (i >= 0 && (current[i].isDigit() || current[i] == ',')) {
            i--
        }

        val numberStart = i + 1
        if (numberStart >= end) return

        val signIndex = numberStart - 1
        val hasUnaryMinus = signIndex >= 0 &&
            current[signIndex] == '-' &&
            (signIndex == 0 || current[signIndex - 1] in operatorChars || current[signIndex - 1] == '(')

        val tokenStart = if (hasUnaryMinus) signIndex else numberStart
        val token = current.substring(tokenStart, end).replace(',', '.')
        val rightValue = token.toDoubleOrNull() ?: return

        val binaryOperatorIndex = findLastBinaryOperatorBefore(current, tokenStart)
        val replacementValue = if (binaryOperatorIndex == -1) {
            rightValue / 100.0
        } else {
            val operator = current[binaryOperatorIndex]
            val leftExpression = current.substring(0, binaryOperatorIndex)
            val leftValue = evaluateExpression(leftExpression) ?: return

            when (operator) {
                '+', '-' -> leftValue * rightValue / 100.0
                '×', '÷' -> rightValue / 100.0
                else -> rightValue / 100.0
            }
        }

        val replacement = formatResult(replacementValue)
        val updated = current.substring(0, tokenStart) + replacement

        state.value = state.value.copy(operation = updated)
        justCalculated = false
    }

    private fun findLastBinaryOperatorBefore(expression: String, endExclusive: Int): Int {
        var depth = 0

        for (i in endExclusive - 1 downTo 0) {
            when (val c = expression[i]) {
                ')' -> depth++
                '(' -> if (depth > 0) depth--
                '+', '-', '×', '÷', '^' -> {
                    if (depth != 0) continue

                    val isUnaryMinus = c == '-' &&
                        (i == 0 || expression[i - 1] in operatorChars || expression[i - 1] == '(')

                    if (!isUnaryMinus) return i
                }
            }
        }

        return -1
    }

    private fun evaluateExpression(expression: String): Double? {
        if (expression.isBlank()) return null

        return try {
            val normalized = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace(",", ".")

            ExpressionBuilder(normalized)
                .build()
                .evaluate()
        } catch (_: Exception) {
            null
        }
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

        val lastChar = current.lastOrNull() ?: return

        if (lastChar in operatorChars) {
            val hasPendingUnaryMinus =
                lastChar == '-' &&
                    current.length >= 2 &&
                    current[current.length - 2] in operatorChars

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
            (signIndex == 0 || current[signIndex - 1] in operatorChars)

        val updated = if (hasUnaryMinus) {
            current.removeRange(signIndex, signIndex + 1)
        } else {
            current.substring(0, numberStart) + "-" + current.substring(numberStart)
        }

        state.value = state.value.copy(operation = updated)
    }

    fun calculate() {
        try {
            val rawExpression = state.value.operation
            if (openParenthesisCount(rawExpression) != 0) {
                state.value = CalculatorState("Error")
                justCalculated = false
                return
            }

            val expression = state.value.operation
                .replace("×", "*")
                .replace("÷", "/")
                .replace(",", ".")

            val result = ExpressionBuilder(expression)
                .build()
                .evaluate()

            if (result.isNaN() || result.isInfinite()) {
                state.value = CalculatorState("Error")
                justCalculated = false
                return
            }

            val formattedResult = formatResult(result)

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
        var i = expression.length - 1

        while (i >= 0) {
            val c = expression[i]
            when {
                c == ',' -> return true
                c == '(' || c == ')' -> return false
                c in operatorChars -> {
                    val isUnaryMinus = c == '-' && (i == 0 || expression[i - 1] in operatorChars)
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
        var i = expression.length - 1
        var digits = 0

        while (i >= 0) {
            val c = expression[i]
            when {
                c.isDigit() -> digits++
                c == ',' -> Unit
                c == '(' || c == ')' -> break
                c in operatorChars -> {
                    val isUnaryMinus = c == '-' && (i == 0 || expression[i - 1] in operatorChars)
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

    private fun openParenthesisCount(expression: String): Int {
        val opens = expression.count { it == '(' }
        val closes = expression.count { it == ')' }
        return opens - closes
    }

    private fun formatResult(value: Double): String {
        if (value % 1 == 0.0) return value.toLong().toString()

        return String.format(Locale.US, "%.8f", value)
            .trimEnd('0')
            .trimEnd('.')
            .replace('.', ',')
    }
}
