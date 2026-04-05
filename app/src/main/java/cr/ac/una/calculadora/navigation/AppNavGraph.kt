package cr.ac.una.calculadora.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cr.ac.una.calculadora.view.AdvancedCalculatorScreen
import cr.ac.una.calculadora.view.CalculatorScreen
import cr.ac.una.calculadora.viewmodel.CalculatorViewModel

object AppRoutes {
    const val Basic = "basic"
    const val Advanced = "advanced"
}

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val calculatorViewModel: CalculatorViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Basic,
        modifier = modifier
    ) {
        composable(AppRoutes.Basic) {
            CalculatorScreen(
                viewModel = calculatorViewModel,
                onOpenAdvanced = { navController.navigate(AppRoutes.Advanced) }
            )
        }

        composable(AppRoutes.Advanced) {
            AdvancedCalculatorScreen(
                viewModel = calculatorViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

