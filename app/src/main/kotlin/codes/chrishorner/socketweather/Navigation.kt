package codes.chrishorner.socketweather

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import codes.chrishorner.socketweather.choose_location.ChooseLocationScreen
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeScreen
import codes.chrishorner.socketweather.home.HomeViewModel

private object RouteNames {
  const val HOME = "home"
  const val CHOOSE_LOCATION = "choose_location"
  const val ABOUT = "about"
}

private object NavArgs {
  const val SHOW_AS_ROOT = "show_as_root"
}

sealed class Route(val name: String) {
  open val navPath: String = name

  object Home : Route(RouteNames.HOME)

  data class ChooseLocation(val showAsRoot: Boolean = false) : Route(RouteNames.CHOOSE_LOCATION) {
    override val navPath = "$name/$showAsRoot"
  }

  object About : Route(RouteNames.ABOUT)
}

fun NavController.navigate(destination: Route) {
  navigate(destination.navPath)
}

@Composable
fun NavGraph(currentSelection: LocationSelection) {
  val navController = rememberNavController()
  val initialRoute =
      if (currentSelection != LocationSelection.None) Route.Home
      else Route.ChooseLocation(showAsRoot = true)

  NavHost(
      navController = navController,
      startDestination = initialRoute.navPath
  ) {
    composable(RouteNames.HOME) {
      val viewModel = createVm { context -> HomeViewModel(context.appSingletons.forecaster) }
      HomeScreen(navController, viewModel)
    }
    composable(
        route = "${RouteNames.CHOOSE_LOCATION}/{${NavArgs.SHOW_AS_ROOT}}",
        arguments = listOf(navArgument(NavArgs.SHOW_AS_ROOT) { type = NavType.BoolType })
    ) { entry ->
      val viewModel = createVm { context ->
        ChooseLocationViewModel(
            displayAsRoot = entry.arguments!!.getBoolean(NavArgs.SHOW_AS_ROOT),
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
