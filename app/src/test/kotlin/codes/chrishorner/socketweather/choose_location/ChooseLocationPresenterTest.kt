package codes.chrishorner.socketweather.choose_location

import codes.chrishorner.socketweather.choose_location.ChooseLocationState.Error
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.FollowMeClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ResultSelected
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.update
import codes.chrishorner.socketweather.home.HomeScreen
import codes.chrishorner.socketweather.test.FakeForecastLoader
import codes.chrishorner.socketweather.test.FakeNavigator
import codes.chrishorner.socketweather.test.FakeStore
import codes.chrishorner.socketweather.test.TestApi
import codes.chrishorner.socketweather.test.TestApi.ResponseMode
import codes.chrishorner.socketweather.test.TestData
import codes.chrishorner.socketweather.test.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class ChooseLocationPresenterTest {

  private val clock: Clock
  private val api: TestApi
  private val navigator = FakeNavigator(ChooseLocationScreen(showCloseButton = false))
  private val forecastLoader = FakeForecastLoader()
  private val currentSelection = FakeStore<LocationSelection>(LocationSelection.None)
  private val savedSelections = FakeStore<Set<LocationSelection>>(emptySet())

  init {
    val fixedDateTime = ZonedDateTime.of(2022, 2, 20, 8, 0, 0, 0, ZoneId.of("Australia/Melbourne"))
    clock = Clock.fixed(fixedDateTime.toInstant(), fixedDateTime.zone)
    api = TestApi(clock)
  }

  private fun createPresenter() = ChooseLocationPresenter(
    showCloseButton = false, navigator, api, forecastLoader, currentSelection, savedSelections
  )

  @Test fun `Follow Me button only shows when LocationSelection-FollowMe not saved`() = runBlocking {
    createPresenter().test {
      assertThat(awaitItem().showFollowMeButton).isTrue()
    }

    savedSelections.update { it + LocationSelection.FollowMe }
    currentSelection.set(LocationSelection.FollowMe)

    createPresenter().test {
      assertThat(awaitItem().showFollowMeButton).isFalse()
    }
  }

  @Test fun `loading state changes to searching on character entry`() = runBlocking {
    createPresenter().test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)
      sendEvent(InputSearch("A"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
    }
  }

  @Test fun `clicking close pops screen`() = runBlocking {
    navigator.setStack(HomeScreen, ChooseLocationScreen(showCloseButton = true))
    createPresenter().test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)
      sendEvent(CloseClicked)
      assertThat(navigator.awaitStackChange()).containsExactly(HomeScreen)
    }
  }

  @Test fun `search not executed until third character entered`() = runBlocking {
    createPresenter().test {
      assertThat(awaitItem().results).isEmpty()
      sendEvent(InputSearch("F"))
      assertThat(awaitItem().results).isEmpty()
      sendEvent(InputSearch("Fa"))
      assertThat(awaitItem().results).isEmpty()
      sendEvent(InputSearch("Fak"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
      with(awaitItem()) {
        assertThat(loadingStatus).isEqualTo(LoadingStatus.SearchingDone)
        assertThat(results.single().name).isEqualTo("Fakezroy")
      }
    }
  }

  @Test fun `search with network failure shows error`() = runBlocking {
    createPresenter().test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      api.responseMode = ResponseMode.NETWORK_ERROR

      sendEvent(InputSearch("Fakezroy"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
      with(awaitItem()) {
        assertThat(loadingStatus).isEqualTo(LoadingStatus.SearchingError)
        assertThat(results).isEmpty()
      }
      navigator.assertNoChanges()
    }
  }

  @Test fun `selecting search result saves selection`() = runBlocking {
    createPresenter().test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      // Searching for "Fakezroy" should emit a single search result from TestApi.
      sendEvent(InputSearch("Fakezroy"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
      val item = awaitItem()
      assertThat(item.loadingStatus).isEqualTo(LoadingStatus.SearchingDone)
      val result = item.results.single()
      assertThat(result.name).isEqualTo("Fakezroy")

      // Selecting that result should change the loading status and save the selection.
      sendEvent(ResultSelected(result))
      // TODO: Work out how to assert Submitting state.
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Submitted)
      val expectedSelection = LocationSelection.Static(TestData.location1)
      assertThat(savedSelections.data.value).containsExactly(expectedSelection)
      assertThat(currentSelection.data.value).isEqualTo(expectedSelection)
      assertThat(navigator.awaitStackChange()).containsExactly(HomeScreen)
    }
  }

  @Test fun `selecting search result with network failure shows error`() = runBlocking {
    createPresenter().test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      // Searching for "Fakezroy" should emit a single search result from TestApi.
      sendEvent(InputSearch("Fakezroy"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
      val item = awaitItem()
      assertThat(item.loadingStatus).isEqualTo(LoadingStatus.SearchingDone)
      val result = item.results.single()
      assertThat(result.name).isEqualTo("Fakezroy")

      api.responseMode = ResponseMode.NETWORK_ERROR

      sendEvent(ResultSelected(result))
      // TODO: Work out how to assert Submitting state.
      assertThat(awaitItem().error).isEqualTo(Error.Submission)
      navigator.assertNoChanges()
    }
  }

  @Test fun `selecting Follow Me with location permission saves selection`() = runBlocking {
    createPresenter().test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      sendEvent(FollowMeClicked(hasLocationPermission = true))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Submitted)
      assertThat(savedSelections.data.value).containsExactly(LocationSelection.FollowMe)
      assertThat(currentSelection.data.value).isEqualTo(LocationSelection.FollowMe)
      assertThat(navigator.awaitStackChange()).containsExactly(HomeScreen)
    }
  }

  @Test fun `selecting Follow Me without location permission shows error`() = runBlocking {
    createPresenter().test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      sendEvent(FollowMeClicked(hasLocationPermission = false))
      with(awaitItem()) {
        assertThat(loadingStatus).isEqualTo(LoadingStatus.Idle)
        assertThat(error).isEqualTo(Error.Permission)
      }
      assertThat(savedSelections.data.value).isEmpty()
      assertThat(currentSelection.data.value).isEqualTo(LocationSelection.None)
      navigator.assertNoChanges()
    }
  }

  @Test fun `selecting Follow Me pops to HomeScreen if possible`() = runBlocking {
    navigator.setStack(HomeScreen, ChooseLocationScreen(showCloseButton = true))

    createPresenter().test {
      awaitItem()
      sendEvent(FollowMeClicked(hasLocationPermission = true))
      awaitItem()
      assertThat(navigator.awaitStackChange()).containsExactly(HomeScreen)
    }
  }

  @Test fun `selecting search result pops to HomeScreen if possible`() = runBlocking {
    navigator.setStack(HomeScreen, ChooseLocationScreen(showCloseButton = true))

    createPresenter().test {
      awaitItem()
      sendEvent(InputSearch("Fakezroy"))
      awaitItem()
      val result = awaitItem().results.single()
      sendEvent(ResultSelected(result))
      awaitItem()
      assertThat(navigator.awaitStackChange()).containsExactly(HomeScreen)
    }
  }

  @Test fun `selecting search result triggers refresh`() = runBlocking {
    createPresenter().test {
      awaitItem()
      sendEvent(InputSearch("Fakezroy"))
      awaitItem()
      val result = awaitItem().results.single()
      sendEvent(ResultSelected(result))
      awaitItem()
      forecastLoader.refreshCalls.awaitItem()
    }
  }

  @Test fun `selecting Follow Me triggers refresh`() = runBlocking {
    createPresenter().test {
      awaitItem()
      sendEvent(FollowMeClicked(hasLocationPermission = true))
      awaitItem()
      forecastLoader.refreshCalls.awaitItem()
    }
  }
}
