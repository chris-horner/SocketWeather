package codes.chrishorner.socketweather

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import codes.chrishorner.socketweather.NavEntry.About
import codes.chrishorner.socketweather.NavEntry.ChooseLocation
import codes.chrishorner.socketweather.NavEntry.Home
import codes.chrishorner.socketweather.NavEntry.SwitchLocation
import codes.chrishorner.socketweather.choose_location.ChooseLocationScreen
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeUi
import com.github.zsoltk.compose.backpress.BackPressHandler
import com.github.zsoltk.compose.router.Router
import com.zachklipp.compose.backstack.Backstack

sealed class NavEntry {
  object Home : NavEntry()
  data class ChooseLocation(val showAsRoot: Boolean = false) : NavEntry()
  object SwitchLocation : NavEntry()
  object About : NavEntry()
}

@Composable
fun NavThing(currentSelection: LocationSelection) {
  val navController = rememberNavController()

  val initialScreen =
      if (currentSelection == LocationSelection.None) "choose_location"
      else "home"

  NavHost(
      navController = navController,
      startDestination = initialScreen
  ) {
    composable("home") {
    }
    composable("choose_location") { ChooseLocationScreen() }
  }
}

@Composable
fun ScreenNavigation(backPressHandler: BackPressHandler, currentSelection: LocationSelection) {

  val initialScreen =
      if (currentSelection == LocationSelection.None) ChooseLocation(showAsRoot = true)
      else Home

  Router(initialScreen) { backstack ->
    Backstack(
        backstack = backstack.elements
    ) { navEntry ->
      when (navEntry) {
        Home -> HomeUi()
        is ChooseLocation -> ChooseLocationScreen()
        SwitchLocation -> TODO()
        About -> TODO()
      }
    }
  }
}
