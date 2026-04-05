package cr.ac.una.calculadora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun myState(modifier: Modifier){
    //var i : Int = 0
    var i by rememberSaveable { mutableIntStateOf(value=0) }
    Text ("Contador ${i}", modifier.clickable{i+=1})
}

@Composable
fun mitexto(modifier: Modifier){
    var texto by rememberSaveable { mutableStateOf("") }
    TextField(texto,{texto = it},
        label = {
            Text("Escribe algo")

        }
        )
}