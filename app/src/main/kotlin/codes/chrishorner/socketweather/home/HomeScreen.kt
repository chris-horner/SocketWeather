package codes.chrishorner.socketweather.home

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.Navigator
import codes.chrishorner.socketweather.Presenter
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.Screen
import codes.chrishorner.socketweather.data.ForecastError
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeEvent.Refresh
import codes.chrishorner.socketweather.home.HomeEvent.ViewAbout
import codes.chrishorner.socketweather.home.HomeState.Content
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.util.permissionState
import kotlinx.parcelize.Parcelize

@Parcelize
object HomeScreen : Screen<HomeEvent, HomeState> {

  override fun onCreatePresenter(context: Context, navigator: Navigator): Presenter<HomeEvent, HomeState> {
    return HomePresenter(context, navigator)
  }

  @Composable
  override fun Content(state: HomeState, onEvent: (HomeEvent) -> Unit) {
    HomeUi(state, onEvent)
  }
}

@Composable
private fun HomeUi(state: HomeState, onEvent: (event: HomeEvent) -> Unit) {

  val scrollState = rememberScrollState()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  val snackbarHostState = remember { SnackbarHostState() }
  var locationChooserVisible by rememberSaveable { mutableStateOf(false) }

  Box {
    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      snackbarHost = { SnackbarHost(snackbarHostState)},
      topBar = {
        TopAppBar(
          title = { ToolbarTitle(state) { locationChooserVisible = true } },
          scrollBehavior = scrollBehavior,
          actions = {
            Menu(
              showColorSwitch = state.showDynamicColorOption,
              colorSwitchChecked = state.dynamicColorEnabled,
              onEvent = onEvent
            )
          },
        )
      }
    ) { innerPadding ->
      Content(state.content, scrollState, snackbarHostState, onEvent, Modifier.padding(innerPadding))
    }
    LocationSwitcher(
      visible = locationChooserVisible,
      currentLocation = state.currentLocation,
      savedLocations = state.savedLocations,
      onDismissRequest = { locationChooserVisible = false },
      onEvent = { event ->
        if (event is HomeEvent.AddLocation || event is HomeEvent.SwitchLocation) locationChooserVisible = false
        onEvent(event)
      }
    )
  }
}

@Composable
private fun Menu(showColorSwitch: Boolean, colorSwitchChecked: Boolean, onEvent: (event: HomeEvent) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  IconButton(onClick = { expanded = true }) {
    Icon(Icons.Default.MoreVert, contentDescription = null)
  }
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = false },
    offset = DpOffset(0.dp, (-56).dp),
  ) {
    DropdownMenuItem(
      text = { Text(stringResource(R.string.home_refresh)) },
      onClick = {
        onEvent(Refresh)
        expanded = false
      },
    )
    DropdownMenuItem(
      text = { Text(stringResource(R.string.home_about)) },
      onClick = {
        onEvent(ViewAbout)
        expanded = false
      },
    )
    if (showColorSwitch) {
      DropdownMenuItem(
        text = {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.home_dynamicColor))
            Switch(
              checked = colorSwitchChecked,
              onCheckedChange = { onEvent(HomeEvent.ToggleDynamicColor) },
            )
          }
        },
        onClick = { onEvent(HomeEvent.ToggleDynamicColor) },
      )
    }
  }
}

@Composable
private fun ToolbarTitle(state: HomeState, onClick: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 56.dp)
      .clickable { onClick() },
  ) {

    Column(
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxHeight()
        .weight(1f)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(state.toolbarTitle, style = MaterialTheme.typography.headlineSmall)
        Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
      }
      state.toolbarSubtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }

    AnimatedVisibility(
      visible = state.showRefreshingIndicator,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      CircularProgressIndicator(
        modifier = Modifier
          .padding(horizontal = 8.dp)
          .size(24.dp)
      )
    }
  }
}

@Composable
private fun Content(
  state: Content,
  scrollState: ScrollState,
  snackbarHostState: SnackbarHostState,
  onEvent: (event: HomeEvent) -> Unit,
  modifier: Modifier,
) {
  when (state) {
    is Content.Error -> Error(state.type, modifier, onRefresh = { onEvent(Refresh) })
    is Content.Loaded -> ForecastUi(state.conditions, scrollState, snackbarHostState, modifier, onEvent)
    else -> Loading(modifier)
  }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
    Text(stringResource(R.string.home_loading), style = MaterialTheme.typography.titleMedium)
  }
}

@Composable
private fun Error(type: ForecastError, modifier: Modifier = Modifier, onRefresh: () -> Unit = {}) {
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

  val activityResultRegistry = LocalActivityResultRegistryOwner.current?.activityResultRegistry
  val locationPermissionState = activityResultRegistry?.permissionState(ACCESS_COARSE_LOCATION) {
    onRefresh()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .navigationBarsPadding(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
    Text(
      message,
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .widthIn(max = 320.dp)
        .padding(top = 8.dp, bottom = 16.dp)
    )
    Image(painter = image, contentDescription = null)
    FilledTonalButton(
      modifier = Modifier.width(200.dp),
      onClick = {
        if (type == ForecastError.LOCATION) {
          locationPermissionState?.launchPermissionRequest()
        } else {
          onRefresh()
        }
      }
    ) {
      Text(stringResource(R.string.home_error_retryButton))
    }
  }
}

@Preview(device = Devices.NEXUS_5, showBackground = true)
@Composable
private fun ErrorPreview() {
  Error(ForecastError.NOT_AUSTRALIA) {}
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_5)
@Composable
private fun HomePreview() {
  SocketWeatherTheme {
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
