package codes.chrishorner.socketweather

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import codes.chrishorner.socketweather.choose_location.ChooseLocationScreen
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeScreen
import codes.chrishorner.socketweather.home.HomeViewModel

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
      val viewModel = createVm { context -> HomeViewModel(context.appSingletons.forecaster) }
      HomeScreen(navController, viewModel)
    }
    composable(
        route = Screen.ChooseLocation.routeDefinition,
        arguments = listOf(navArgument(NavArgs.SHOW_CLOSE_BUTTON) { type = NavType.BoolType })
    ) { entry ->
      val viewModel = createVm { context ->
        ChooseLocationViewModel(
            displayAsRoot = entry.arguments!!.getBoolean(NavArgs.SHOW_CLOSE_BUTTON),
            api = context.appSingletons.networkComponents.api,
            locationChoices = context.appSingletons.locationChoices
        )
      }
      ChooseLocationScreen(navController, viewModel)
    }
  }
}

/**
 * Simplifies the creation of a [ViewModelProvider.Factory] by simply delegating to a lambda.
 */
@Composable
@Suppress("UNCHECKED_CAST")
private inline fun <reified VM : ViewModel> createVm(crossinline creator: (context: Context) -> VM): VM {
  val context = AmbientContext.current
  return viewModel(factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return creator(context) as T
    }
  })
}
