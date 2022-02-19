package codes.chrishorner.socketweather.choose_location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.Screen
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.Error.Permission
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.Error.Submission
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitted
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitting
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ClearInput
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ResultSelected
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.util.permissionState
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding

@Composable
fun ChooseLocationScreen(viewModel: ChooseLocationViewModel, navController: NavController) {
  val state: ChooseLocationState by viewModel.states.collectAsState()

  if (state.loadingStatus == Submitted) navController.navigate(Screen.Home.getRoute()) {
    launchSingleTop = true
    popUpTo(Screen.ChooseLocation.routeDefinition) { inclusive = true }
  }

  ChooseLocationUi(state) { event ->
    viewModel.handle(event)
    if (event == CloseClicked) navController.popBackStack()
  }
}

@Composable
fun ChooseLocationUi(state: ChooseLocationState, eventHandler: (event: ChooseLocationUiEvent) -> Unit) {

  val focusManager = LocalFocusManager.current
  val currentlyIdle = state.loadingStatus == Idle

  BackHandler(enabled = !currentlyIdle) {
    focusManager.clearFocus()
    eventHandler(ClearInput)
  }

  Surface(
    color = MaterialTheme.colors.background,
    modifier = Modifier
      .statusBarsPadding()
      .navigationBarsPadding(start = true, end = true, bottom = false)
  ) {
    Box {
      Column {
        if (state.showCloseButton) {
          AnimatedVisibility(visible = currentlyIdle) {
            IconButton(onClick = { eventHandler(CloseClicked) }) {
              CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.chooseLocation_closeDesc))
              }
            }
          }
        }
        Spacer(
          modifier = Modifier
            .animateContentSize()
            .then(if (currentlyIdle) Modifier.weight(1f) else Modifier.height(0.dp))
        )
        AnimatedVisibility(visible = currentlyIdle) {
          Text(
            text = stringResource(R.string.chooseLocation_title),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 16.dp)
          )
        }
        AnimatedVisibility(visible = currentlyIdle && state.showFollowMeButton) {
          FollowMeButton { hasLocationPermission ->
            eventHandler(ChooseLocationUiEvent.FollowMeClicked(hasLocationPermission))
          }
        }
        OutlinedTextField(
          value = state.query,
          label = { Text(text = stringResource(R.string.chooseLocation_searchHint)) },
          onValueChange = { eventHandler(ChooseLocationUiEvent.InputSearch(it)) },
          leadingIcon = {
            Icon(
              Icons.Rounded.Search,
              contentDescription = null,
              modifier = Modifier.padding(start = 2.dp)
            )
          },
          singleLine = true,
          modifier = Modifier
            .padding(horizontal = 32.dp)
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
        Spacer(modifier = Modifier.navigationBarsWithImePadding())
      }

      ErrorMessagesSnackbar(
        error = state.error,
        modifier = Modifier
          .align(Alignment.BottomStart)
          .navigationBarsWithImePadding()
          .padding(16.dp)
      )
    }
  }
}

@Composable
private fun ErrorMessagesSnackbar(
  error: ChooseLocationState.Error?,
  modifier: Modifier = Modifier,
) {
  val hostState = remember { SnackbarHostState() }

  val message = when (error) {
    Permission -> stringResource(R.string.chooseLocation_permissionError)
    Submission -> stringResource(R.string.chooseLocation_submissionError)
    else -> null
  }

  if (message != null) {
    LaunchedEffect(message) { hostState.showSnackbar(message) }
  }

  SnackbarHost(hostState = hostState, modifier = modifier) {
    Snackbar { Text(it.message) }
  }
}

@Composable
private fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(64.dp)
      .clickable(onClick = onClick)
      .padding(horizontal = 32.dp)
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .align(Alignment.CenterVertically)
    ) {
      Text(text = result.name, maxLines = 1)
      Text(text = result.state, maxLines = 1)
    }
    result.postcode?.let {
      Text(text = it, modifier = Modifier.align(Alignment.CenterVertically))
    }
  }
}

@Composable
private fun SearchResults(results: List<SearchResult>, onClick: (item: SearchResult) -> Unit) {
  if (results.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize()) {
      Text(
        text = stringResource(R.string.chooseLocation_searchEmpty),
        style = MaterialTheme.typography.subtitle1,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .align(Alignment.Center)
          .width(352.dp)
          .padding(horizontal = 32.dp)
      )
    }
  } else {
    LazyColumn {
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
      style = MaterialTheme.typography.subtitle1,
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
      style = MaterialTheme.typography.subtitle1,
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

  Button(
    colors = ButtonDefaults.buttonColors(
      backgroundColor = MaterialTheme.colors.primaryVariant
    ),
    modifier = Modifier
      .padding(start = 32.dp, end = 32.dp, bottom = 16.dp)
      .fillMaxWidth()
      .height(48.dp),
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
    ProvideWindowInsets {
      ChooseLocationUi(
        ChooseLocationState(
          showCloseButton = true,
          showFollowMeButton = true,
          query = "",
          results = emptyList(),
          loadingStatus = Idle
        )
      ) {
        // Don't handle events in Preview.
      }
    }
  }
}
