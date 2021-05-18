package codes.chrishorner.socketweather.home

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.Screen
import codes.chrishorner.socketweather.data.ForecastError
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeEvent.AddLocation
import codes.chrishorner.socketweather.home.HomeEvent.Refresh
import codes.chrishorner.socketweather.home.HomeEvent.ViewAbout
import codes.chrishorner.socketweather.home.HomeState2.Content
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.util.InsetAwareTopAppBar
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import codes.chrishorner.socketweather.home.HomeState2 as HomeState
import codes.chrishorner.socketweather.home.HomeViewModel2 as HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavController) {
  val state: HomeState by viewModel.states.collectAsState()
  HomeUi(state) { event ->
    when (event) {
      AddLocation -> navController.navigate(Screen.ChooseLocation.getRoute())
      ViewAbout -> {
        // TODO: Navigate to About screen.
      }
      else -> viewModel.handleEvent(event)
    }
  }
}

@Composable
private fun HomeUi(state: HomeState, onEvent: (event: HomeEvent) -> Unit) {

  val scrollState = rememberScrollState()
  val toolbarElevation by animateDpAsState(targetValue = if (scrollState.value > 0) 4.dp else 0.dp)
  var locationChooserVisible by rememberSaveable { mutableStateOf(false) }

  Box {
    Surface(color = MaterialTheme.colors.background) {
      Scaffold(
        topBar = {
          InsetAwareTopAppBar(
            title = { ToolbarTitles(state) { locationChooserVisible = true } },
            backgroundColor = MaterialTheme.colors.background,
            elevation = toolbarElevation,
            actions = { Menu(onEvent) }
          )
        }
      ) {
        Content(state.content, scrollState)
      }
    }
    LocationSwitcher(
      visible = locationChooserVisible,
      currentLocation = state.currentLocation,
      savedLocations = state.savedLocations,
      onDismissRequest = { locationChooserVisible = false },
      onEvent = {
        locationChooserVisible = false
        onEvent(it)
      }
    )
  }
}

@Composable
private fun Menu(eventHandler: (event: HomeEvent) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  IconButton(onClick = { expanded = true }) {
    Icon(Icons.Default.MoreVert, contentDescription = null)
  }
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = false },
    offset = DpOffset(0.dp, (-56).dp),
  ) {
    DropdownMenuItem(onClick = {
      eventHandler(Refresh)
      expanded = false
    }) {
      Text(stringResource(R.string.home_refresh))
    }
    DropdownMenuItem(onClick = {
      eventHandler(ViewAbout)
      expanded = false
    }) {
      Text(stringResource(R.string.home_about))
    }
  }
}

@Composable
private fun ToolbarTitles(state: HomeState, onClick: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp)
      .clickable { onClick() },
    verticalArrangement = Arrangement.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(state.toolbarTitle, style = MaterialTheme.typography.h5)
      Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
    }
    state.toolbarSubtitle?.let { Text(it, style = MaterialTheme.typography.caption) }
  }
}

@Composable
private fun Content(state: Content, scrollState: ScrollState) {
  when (state) {
    is Content.Error -> Error(state.type)
    is Content.Refreshing -> ForecastUi(state.conditions, scrollState)
    is Content.Loaded -> ForecastUi(state.conditions, scrollState)
    else -> Loading()
  }
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
private fun Error(type: ForecastError) {
  val title: String
  val message: String
  val image: Painter

  when (type) {
    ForecastError.DATA -> {
      title = stringResource(R.string.home_error_data_title)
      message = stringResource(R.string.home_error_data_message)
      image = painterResource(R.drawable.gfx_data_error)
    }
    ForecastError.NETWORK -> {
      title = stringResource(R.string.home_error_network_title)
      message = stringResource(R.string.home_error_network_message)
      image = painterResource(R.drawable.gfx_network_error)
    }
    ForecastError.LOCATION -> {
      title = stringResource(R.string.home_error_location_title)
      message = stringResource(R.string.home_error_location_message)
      image = painterResource(R.drawable.gfx_location_error)
    }
    ForecastError.NOT_AUSTRALIA -> {
      title = stringResource(R.string.home_error_unknownLocation_title)
      message = stringResource(R.string.home_error_unknownLocation_message)
      image = painterResource(R.drawable.gfx_unknown_location)
    }
  }

  Column(
    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(title, style = MaterialTheme.typography.h4, textAlign = TextAlign.Center)
    Text(
      message,
      style = MaterialTheme.typography.body1,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .width(280.dp)
        .padding(top = 8.dp, bottom = 16.dp)
    )
    Icon(image, contentDescription = null)
    Button(modifier = Modifier.width(200.dp), onClick = { /*TODO*/ }) {
      Text(stringResource(R.string.home_error_retryButton))
    }
  }
}

@Preview(device = Devices.NEXUS_5, showBackground = true)
@Composable
private fun ErrorPreview() {
  Error(ForecastError.NOT_AUSTRALIA)
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_5)
@Composable
private fun HomePreview() {
  SocketWeatherTheme {
    ProvideWindowInsets {
      HomeUi(
        HomeState(
          "Melbourne",
          "Just now",
          LocationEntry(LocationSelection.FollowMe, "Melbourne", "VIC"),
          emptyList(),
          Content.Loading
        )
      ) { /* Don't handle events in preview. */ }
    }
  }
}
