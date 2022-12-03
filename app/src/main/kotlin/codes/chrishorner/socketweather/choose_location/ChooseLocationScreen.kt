package codes.chrishorner.socketweather.choose_location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.Navigator
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.Screen
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.Error.Permission
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.Error.Submission
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitting
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ClearInput
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ResultSelected
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.util.permissionState
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChooseLocationScreen(
  val showCloseButton: Boolean
) : Screen<ChooseLocationUiEvent, ChooseLocationState> {

  override fun onCreatePresenter(
    context: Context,
    navigator: Navigator,
  ) = ChooseLocationPresenter(showCloseButton, navigator, context)

  @Composable
  override fun Content(state: ChooseLocationState, onEvent: (ChooseLocationUiEvent) -> Unit) {
    ChooseLocationUi(state, onEvent)
  }
}

@Composable
fun ChooseLocationUi(state: ChooseLocationState, eventHandler: (event: ChooseLocationUiEvent) -> Unit = {}) {

  val focusManager = LocalFocusManager.current
  val currentlyIdle = state.loadingStatus == Idle
  val snackbarHostState = remember { SnackbarHostState() }

  BackHandler(enabled = !currentlyIdle) {
    focusManager.clearFocus()
    eventHandler(ClearInput)
  }

  // TODO: Move this calculation into presenter.
  val errorMessage = when (state.error) {
    Permission -> stringResource(R.string.chooseLocation_permissionError)
    Submission -> stringResource(R.string.chooseLocation_submissionError)
    else -> null
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage != null) {
      snackbarHostState.showSnackbar(errorMessage)
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      if (currentlyIdle) {
        TopAppBar(
          title = {},
          navigationIcon = {
            if (state.showCloseButton) {
              IconButton(onClick = { eventHandler(CloseClicked) }) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                  Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.chooseLocation_closeDesc))
                }
              }
            }
          },
        )
      }
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = innerPadding.calculateTopPadding())
    ) {
      Spacer(
        modifier = Modifier
          .animateContentSize()
          .then(if (currentlyIdle) Modifier.weight(1f) else Modifier.height(0.dp))
      )
      AnimatedVisibility(visible = currentlyIdle) {
        Text(
          text = stringResource(R.string.chooseLocation_title),
          style = MaterialTheme.typography.headlineMedium,
          modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 16.dp)
        )
      }
      AnimatedVisibility(visible = currentlyIdle && state.showFollowMeButton) {
        FollowMeButton { hasLocationPermission ->
          eventHandler(ChooseLocationUiEvent.FollowMeClicked(hasLocationPermission))
        }
      }
      OutlinedTextField(
        shape = CircleShape,
        value = state.query,
        label = { Text(text = stringResource(R.string.chooseLocation_searchHint)) },
        onValueChange = { eventHandler(ChooseLocationUiEvent.InputSearch(it)) },
        leadingIcon = {
          Icon(
            Icons.Rounded.Search,
            contentDescription = null,
            modifier = Modifier.padding(start = 24.dp, end = 8.dp)
          )
        },
        singleLine = true,
        modifier = Modifier
          .testTag("search_input")
          .padding(horizontal = 32.dp)
          .padding(top = 16.dp)
          .fillMaxWidth()
      )
      Crossfade(modifier = Modifier.weight(2f), targetState = state.loadingStatus) { loadingStatus ->
        when (loadingStatus) {
          Searching -> SearchLoading()
          Submitting -> SubmittingLocationChoice()
          SearchingError -> SearchError()
          SearchingDone -> SearchResults(state.results) { eventHandler(ResultSelected(it)) }
          else -> {
            // Don't display anything when Idle or Submitted.
          }
        }
      }
      Spacer(modifier = Modifier.imePadding())
    }
  }
}

@Composable
private fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .testTag("search_result_postcode:${result.postcode}")
      .fillMaxWidth()
      .heightIn(min = 64.dp)
      .clickable(onClick = onClick)
      .padding(horizontal = 32.dp)
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .align(Alignment.CenterVertically)
    ) {
      Text(
        text = result.name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleMedium
      )
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Text(
          text = result.state,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.titleSmall
        )
      }
    }
    result.postcode?.let {
      Text(
        text = it,
        modifier = Modifier.align(Alignment.CenterVertically),
        style = MaterialTheme.typography.labelMedium
      )
    }
  }
}

@Composable
private fun SearchResults(results: List<SearchResult>, onClick: (item: SearchResult) -> Unit) {
  if (results.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize()) {
      Text(
        text = stringResource(R.string.chooseLocation_searchEmpty),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .align(Alignment.Center)
          .width(352.dp)
          .padding(horizontal = 32.dp)
      )
    }
  } else {
    LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues()) {
      items(results) {
        SearchResultItem(it) { onClick(it) }
      }
    }
  }
}

@Composable
private fun SearchLoading() {
  Box(modifier = Modifier.fillMaxSize()) {
    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
  }
}

@Composable
private fun SearchError() {
  Box(modifier = Modifier.fillMaxSize()) {
    Text(
      text = stringResource(R.string.chooseLocation_searchError),
      style = MaterialTheme.typography.titleMedium,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .align(Alignment.Center)
        .width(352.dp)
        .padding(horizontal = 32.dp)
    )
  }
}

@Composable
private fun SubmittingLocationChoice() {
  Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    Text(
      text = stringResource(R.string.chooseLocation_submitting),
      style = MaterialTheme.typography.titleMedium,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .padding(top = 16.dp)
    )
  }
}

@Composable
private fun FollowMeButton(onClick: (hasLocationPermission: Boolean) -> Unit) {
  val activityResultRegistry = LocalActivityResultRegistryOwner.current?.activityResultRegistry
  val locationPermissionState = activityResultRegistry?.permissionState(ACCESS_COARSE_LOCATION) { granted ->
    onClick(granted)
  }

  ElevatedButton(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 32.dp)
      .heightIn(min = 56.dp),
    onClick = {
      if (locationPermissionState?.hasPermission == true) {
        onClick(true)
      } else {
        locationPermissionState?.launchPermissionRequest()
      }
    }
  ) {
    Icon(Icons.Rounded.MyLocation, contentDescription = null)
    Spacer(Modifier.size(12.dp))
    Text(stringResource(R.string.chooseLocation_myLocationButton), modifier = Modifier.fillMaxWidth())
  }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_5)
@Composable
fun ChooseLocationPreview() {
  SocketWeatherTheme {
    ChooseLocationUi(
      ChooseLocationState(
        showCloseButton = true,
        showFollowMeButton = true,
        query = "",
        results = emptyList(),
        loadingStatus = Idle
      )
    )
  }
}
