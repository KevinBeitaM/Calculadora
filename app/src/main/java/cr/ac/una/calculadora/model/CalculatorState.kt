package cr.ac.una.calculadora.model

data class CalculatorState(val operation : String = "0"){
    fun display() : String{
        return operation.replace("sqrt", "√")
    }
}
