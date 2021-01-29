package codes.chrishorner.socketweather.home

import android.content.res.Configuration
import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.Screen
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.data.Forecaster.State.ErrorType
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeEvent.ChooseLocation
import codes.chrishorner.socketweather.home.HomeEvent.Refresh
import codes.chrishorner.socketweather.home.HomeEvent.ViewAbout
import codes.chrishorner.socketweather.home.HomeState.RefreshTime
import codes.chrishorner.socketweather.home.HomeState.RefreshTime.Failed
import codes.chrishorner.socketweather.home.HomeState.RefreshTime.InProgress
import codes.chrishorner.socketweather.home.HomeState.RefreshTime.JustNow
import codes.chrishorner.socketweather.home.HomeState.RefreshTime.TimeAgo
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.util.InsetAwareTopAppBar
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
  val state: HomeState by viewModel.states2.collectAsState()
  HomeUi(state) { event ->
    when (event) {
      Refresh -> viewModel.forceRefresh()
      ChooseLocation -> navController.navigate(Screen.ChooseLocation.getRoute())
      ViewAbout -> {
        // TODO: Navigate to About screen.
      }
    }
  }
}

@Composable
private fun HomeUi(state: HomeState, eventHandler: (event: HomeEvent) -> Unit) {
  Surface(color = MaterialTheme.colors.background) {
    Scaffold(
        topBar = {
          InsetAwareTopAppBar(
              title = { LocationDropdown(state) },
              backgroundColor = MaterialTheme.colors.background,
              actions = { Menu(eventHandler) }
          )
        }
    ) {
      Content(state.forecasterState)
    }
  }
}

@Composable
private fun Menu(eventHandler: (event: HomeEvent) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  val menuButton = @Composable {
    IconButton(onClick = { expanded = true }) {
      Icon(Icons.Default.MoreVert, contentDescription = null)
    }
  }

  DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      toggle = menuButton,
      dropdownOffset = DpOffset(0.dp, (-56).dp)
  ) {
    DropdownMenuItem(onClick = { eventHandler(HomeEvent.Refresh) }) {
      Text(stringResource(R.string.home_refresh))
    }
    DropdownMenuItem(onClick = { eventHandler(HomeEvent.ViewAbout) }) {
      Text(stringResource(R.string.home_about))
    }
  }
}

@Composable
private fun LocationDropdown(state: HomeState) {
  var expanded by remember { mutableStateOf(false) }

  DropdownMenu(
      toggle = {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()) {
          Column {
            Text(getToolbarTitle(state.forecasterState), style = MaterialTheme.typography.h5)
            state.refreshTime.asText()?.let { Text(it, style = MaterialTheme.typography.caption) }
          }
          Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
        }
      },
      expanded = expanded,
      onDismissRequest = { expanded = false },
      toggleModifier = Modifier
          .fillMaxHeight()
          .clickable { }
  ) {
    // TODO: Show location picker.
  }
}

@Composable
private fun Content(state: Forecaster.State) {
  when (state) {
    is Forecaster.State.Error -> Error(state.type)
    is Forecaster.State.Refreshing -> Forecast(state.previousForecast)
    is Forecaster.State.Loaded -> Forecast(state.forecast)
    else -> Loading()
  }
}

@Composable
private fun Forecast(forecast: Forecast) {

}

@Composable
private fun Loading() {
  Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
  ) {
    CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
    Text(stringResource(R.string.home_loading), style = MaterialTheme.typography.subtitle1)
  }
}

@Composable
private fun Error(type: ErrorType) {
  val title: String
  val message: String
  val image: ImageVector

  when (type) {
    ErrorType.DATA -> {
      title = stringResource(R.string.home_error_data_title)
      message = stringResource(R.string.home_error_data_message)
      image = vectorResource(R.drawable.gfx_data_error)
    }
    ErrorType.NETWORK -> {
      title = stringResource(R.string.home_error_network_title)
      message = stringResource(R.string.home_error_network_message)
      image = vectorResource(R.drawable.gfx_network_error)
    }
    ErrorType.LOCATION -> {
      title = stringResource(R.string.home_error_location_title)
      message = stringResource(R.string.home_error_location_message)
      image = vectorResource(R.drawable.gfx_location_error)
    }
    ErrorType.NOT_AUSTRALIA -> {
      title = stringResource(R.string.home_error_unknownLocation_title)
      message = stringResource(R.string.home_error_unknownLocation_message)
      image = vectorResource(R.drawable.gfx_unknown_location)
    }
  }

  Column(
      modifier = Modifier.fillMaxHeight(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(title, style = MaterialTheme.typography.h4, textAlign = TextAlign.Center)
    Text(
        message,
        style = MaterialTheme.typography.body1,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .preferredWidth(280.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    )
    Icon(image, contentDescription = null)
    Button(modifier = Modifier.preferredWidth(200.dp), onClick = { /*TODO*/ }) {
      Text(stringResource(R.string.home_error_retryButton))
    }
  }
}

@Composable
private fun getToolbarTitle(state: Forecaster.State): String = when (state) {
  is Forecaster.State.FindingLocation -> stringResource(R.string.home_findingLocation)
  is Forecaster.State.Refreshing -> state.previousForecast.location.name
  is Forecaster.State.Loaded -> state.forecast.location.name
  is Forecaster.State.Error -> {
    when (val selection = state.selection) {
      is LocationSelection.Static -> selection.location.name
      is LocationSelection.FollowMe -> stringResource(R.string.home_findingLocation)
      is LocationSelection.None -> throw IllegalStateException("Cannot display LocationSelection of None.")
    }
  }
  else -> stringResource(R.string.home_loading)
}

@Composable
private fun RefreshTime.asText(): String? = when (this) {
  InProgress -> stringResource(R.string.home_updatingNow)
  JustNow -> stringResource(R.string.home_justUpdated)
  Failed -> null
  is TimeAgo -> DateUtils.getRelativeTimeSpanString(time.toEpochMilli()).toString()
}

@Preview(device = Devices.NEXUS_5, showBackground = true)
@Composable
private fun ErrorPreview() {
  Error(ErrorType.NOT_AUSTRALIA)
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_5)
@Composable
private fun HomePreview() {
  SocketWeatherTheme {
    ProvideWindowInsets {
      HomeUi(HomeState(RefreshTime.InProgress, Forecaster.State.Idle)) { /* Don't handle events in preview. */ }
    }
  }
}
