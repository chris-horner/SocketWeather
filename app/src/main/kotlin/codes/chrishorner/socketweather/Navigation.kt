package codes.chrishorner.socketweather

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScaleTransition
import codes.chrishorner.socketweather.choose_location.ChooseLocationScreen
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeScreen

@SuppressLint("StateFlowValueCalledInComposition") // We only want to calculate initialScreen once.
@Composable
fun Navigation() {
  val currentSelection = LocalContext.current.appSingletons.stores.currentSelection.data.value
  val initialScreen =
    if (currentSelection != LocationSelection.None) HomeScreen
    else ChooseLocationScreen(showCloseButton = false)

  Navigator(initialScreen) { navigator -> ScaleTransition(navigator) }
}
