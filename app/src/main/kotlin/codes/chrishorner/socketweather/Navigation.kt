package codes.chrishorner.socketweather

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import codes.chrishorner.socketweather.about.AboutScreen
import codes.chrishorner.socketweather.choose_location.ChooseLocationScreen
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeScreen
import codes.chrishorner.socketweather.home.HomeViewModel2

private object NavArgs {
  const val SHOW_CLOSE_BUTTON = "show_close_button"
}

sealed class Screen(val routeDefinition: String) {

  object Home : Screen("home") {
    fun getRoute() = routeDefinition
  }

  object ChooseLocation : Screen("choose_location/{${NavArgs.SHOW_CLOSE_BUTTON}}") {
    fun getRoute(showCloseButton: Boolean = true) = "choose_location/$showCloseButton"
  }

  object About : Screen("about") {
    fun getRoute() = routeDefinition
  }
}

@Composable
fun NavGraph(currentSelection: LocationSelection) {
  val navController = rememberNavController()
  val initialRoute =
    if (currentSelection != LocationSelection.None) Screen.Home.getRoute()
    else Screen.ChooseLocation.routeDefinition

  NavHost(
    navController = navController,
    startDestination = initialRoute
  ) {
    composable(Screen.Home.routeDefinition) {
      val viewModel = createVm { context -> HomeViewModel2(context) }
      HomeScreen(viewModel, navController)
    }
    composable(
      route = Screen.ChooseLocation.routeDefinition,
      arguments = listOf(navArgument(NavArgs.SHOW_CLOSE_BUTTON) { type = NavType.BoolType })
    ) { entry ->
      val viewModel = createVm { context ->
        ChooseLocationViewModel(context, showCloseButton = entry.arguments!!.getBoolean(NavArgs.SHOW_CLOSE_BUTTON))
      }
      ChooseLocationScreen(viewModel, navController)
    }
    composable(route = Screen.About.routeDefinition) {
      AboutScreen(navController)
    }
  }
}

/**
 * Simplifies the creation of a [ViewModelProvider.Factory] by simply delegating to a lambda.
 */
@Composable
@Suppress("UNCHECKED_CAST")
private inline fun <reified VM : ViewModel> createVm(crossinline creator: (context: Context) -> VM): VM {
  val context = LocalContext.current
  return viewModel(factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return creator(context) as T
    }
  })
}