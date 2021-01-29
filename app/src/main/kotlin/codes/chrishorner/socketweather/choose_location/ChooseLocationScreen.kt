package codes.chrishorner.socketweather.choose_location

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AmbientContentAlpha
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.Screen
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitted
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitting
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ResultSelected
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.navigationBarsWithImePadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun ChooseLocationScreen(navController: NavController, viewModel: ChooseLocationViewModel) {
  val state: ChooseLocationState by viewModel.states.collectAsState()

  if (state.loadingStatus == Submitted) navController.navigate(Screen.Home.getRoute()) {
    launchSingleTop = true
    popUpTo(Screen.ChooseLocation.routeDefinition) { inclusive = true }
  }

  ChooseLocationUi(state) { event -> viewModel.handle(event) }
}

@Composable
fun ChooseLocationUi(state: ChooseLocationState, eventHandler: (event: ChooseLocationUiEvent) -> Unit) {

  val currentlyIdle = state.loadingStatus == Idle

  Surface(
      color = MaterialTheme.colors.background,
      modifier = Modifier
          .statusBarsPadding()
          .navigationBarsWithImePadding()
  ) {
    Column {
      if (state.showCloseButton) {
        AnimatedVisibility(visible = currentlyIdle) {
          IconButton(onClick = { eventHandler(CloseClicked) }) {
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
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
            modifier = Modifier.padding(horizontal = 32.dp)
        )
      }
      OutlinedTextField(
          value = state.query,
          label = { Text(text = stringResource(R.string.chooseLocation_searchHint)) },
          onValueChange = { eventHandler(ChooseLocationUiEvent.InputSearch(it)) },
          leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier
              .padding(horizontal = 32.dp)
              .fillMaxWidth()
      )
      Crossfade(modifier = Modifier.weight(2f), current = state.loadingStatus) { loadingStatus ->
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
    }
  }
}

@Composable
private fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .preferredHeight(64.dp)
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
              .preferredWidth(352.dp)
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
            .preferredWidth(352.dp)
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

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_5)
@Composable
fun ChooseLocationPreview() {
  SocketWeatherTheme {
    ProvideWindowInsets {
      ChooseLocationUi(
          ChooseLocationState(
              showCloseButton = true,
              showFollowMe = true,
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
