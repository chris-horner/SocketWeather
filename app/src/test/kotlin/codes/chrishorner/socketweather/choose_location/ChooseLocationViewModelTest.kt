package codes.chrishorner.socketweather.choose_location

import app.cash.turbine.test
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus
import codes.chrishorner.socketweather.data.FakeLocationSelectionStore
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.TestApi
import codes.chrishorner.socketweather.data.TestApi.ResponseMode
import codes.chrishorner.socketweather.data.runCancellingBlockingTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ChooseLocationViewModelTest {

  private val clock: Clock
  private val api: TestApi
  private val locationSelectionStore = FakeLocationSelectionStore()

  init {
    val fixedDateTime = LocalDateTime.of(2021, 6, 6, 22, 0)
    val fixedInstant = ZonedDateTime.of(fixedDateTime, ZoneId.of("Australia/Melbourne")).toInstant()
    clock = Clock.fixed(fixedInstant, ZoneId.of("Australia/Melbourne"))
    api = TestApi(clock)
  }

  private fun createViewModel(scope: CoroutineScope) = ChooseLocationViewModel(
    showCloseButton = false,
    api = api,
    locationSelectionStore = locationSelectionStore,
    overrideScope = scope
  )

  @Test fun `loading state changes to searching on character entry`() = runCancellingBlockingTest {
    val viewModel = createViewModel(this)
    viewModel.states.test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)
      viewModel.handle(ChooseLocationUiEvent.InputSearch("A"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
    }
  }

  @Test fun `search query not executed until third character entered`() = runCancellingBlockingTest {
    val viewModel = createViewModel(this)
    viewModel.states.test {
      assertThat(awaitItem().results).isEmpty()

      // When entering one or two characters, search results should remain empty.
      viewModel.handle(ChooseLocationUiEvent.InputSearch("F"))
      assertThat(awaitItem().results).isEmpty()
      viewModel.handle(ChooseLocationUiEvent.InputSearch("Fa"))
      assertThat(awaitItem().results).isEmpty()

      // After entering three characters, we should transition from Searching -> SearchingDone with results.
      viewModel.handle(ChooseLocationUiEvent.InputSearch("Fak"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
      with(awaitItem()) {
        assertThat(loadingStatus).isEqualTo(LoadingStatus.SearchingDone)
        assertThat(results.single().name).isEqualTo("Fakezroy")
      }
    }
  }

  @Test fun `selecting search result saves selection`() = runCancellingBlockingTest {
    val viewModel = createViewModel(this)
    viewModel.states.test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      // Searching for "Fakezroy" should emit a single search result from TestApi.
      viewModel.handle(ChooseLocationUiEvent.InputSearch("Fakezroy"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
      val result = awaitItem().results.single()
      assertThat(result.name).isEqualTo("Fakezroy")

      // Selecting that result should change the loading status and save the selection.
      viewModel.handle(ChooseLocationUiEvent.ResultSelected(result))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Submitted)
      val expectedSelection = LocationSelection.Static(api.location1)
      assertThat(locationSelectionStore.savedSelections.value).containsExactly(expectedSelection)
      assertThat(locationSelectionStore.currentSelection.value).isEqualTo(expectedSelection)
    }
  }

  @Test fun `selecting follow me with location permission saves selection`() = runCancellingBlockingTest {
    val viewModel = createViewModel(this)
    viewModel.states.test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      // Clicking "follow me" should change the loading status and save the selection.
      viewModel.handle(ChooseLocationUiEvent.FollowMeClicked(hasLocationPermission = true))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Submitted)
      assertThat(locationSelectionStore.savedSelections.value).containsExactly(LocationSelection.FollowMe)
      assertThat(locationSelectionStore.currentSelection.value).isEqualTo(LocationSelection.FollowMe)
    }
  }

  @Test fun `selecting follow me without location permission shows error`() = runCancellingBlockingTest {
    val viewModel = createViewModel(this)
    viewModel.states.test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      // Clicking "follow me" without location permission should produce an error.
      viewModel.handle(ChooseLocationUiEvent.FollowMeClicked(hasLocationPermission = false))
      with(awaitItem()) {
        assertThat(loadingStatus).isEqualTo(LoadingStatus.Idle)
        assertThat(error).isEqualTo(ChooseLocationState.Error.Permission)
      }

      // And the saved location selections should remain empty.
      assertThat(locationSelectionStore.savedSelections.value).isEmpty()
      assertThat(locationSelectionStore.currentSelection.value).isEqualTo(LocationSelection.None)
    }
  }

  @Test fun `searching with failing network shows error`() = runCancellingBlockingTest {
    val viewModel = createViewModel(this)
    api.responseMode = ResponseMode.NETWORK_ERROR

    viewModel.states.test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      // Entering a query should transition from Searching -> Searching Error.
      viewModel.handle(ChooseLocationUiEvent.InputSearch("Fakezroy"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.SearchingError)
    }
  }

  @Test fun `choosing result with failing network shows error`() = runCancellingBlockingTest {
    val viewModel = createViewModel(this)
    viewModel.states.test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      // Searching for "Fakezroy" should emit a single search result from TestApi.
      viewModel.handle(ChooseLocationUiEvent.InputSearch("Fakezroy"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
      val result = awaitItem().results.single()
      assertThat(result.name).isEqualTo("Fakezroy")

      // Setting network responses to fail and selecting the result should show an error.
      api.responseMode = ResponseMode.NETWORK_ERROR
      viewModel.handle(ChooseLocationUiEvent.ResultSelected(result))
      assertThat(awaitItem().error).isEqualTo(ChooseLocationState.Error.Submission)
    }
  }
}
