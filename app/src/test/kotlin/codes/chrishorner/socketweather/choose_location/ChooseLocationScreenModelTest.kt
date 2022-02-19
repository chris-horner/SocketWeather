package codes.chrishorner.socketweather.choose_location

import codes.chrishorner.socketweather.choose_location.ChooseLocationState.Error
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ResultSelected
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.update
import codes.chrishorner.socketweather.test.FakeStore
import codes.chrishorner.socketweather.test.TestApi
import codes.chrishorner.socketweather.test.TestApi.ResponseMode
import codes.chrishorner.socketweather.test.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class ChooseLocationScreenModelTest {

  private val clock: Clock
  private val api: TestApi
  private val currentSelection = FakeStore<LocationSelection>(LocationSelection.None)
  private val savedSelections = FakeStore<Set<LocationSelection>>(emptySet())

  init {
    val fixedDateTime = ZonedDateTime.of(2022, 2, 20, 8, 0, 0, 0, ZoneId.of("Australia/Melbourne"))
    clock = Clock.fixed(fixedDateTime.toInstant(), fixedDateTime.zone)
    api = TestApi(clock)
  }

  private fun createScreenModel() = ChooseLocationScreenModel(
    showCloseButton = false, api, currentSelection, savedSelections
  )

  @Test fun `Follow Me button only shows when LocationSelection-FollowMe not saved`() = runBlocking {
    createScreenModel().test {
      assertThat(awaitItem().showFollowMeButton).isTrue()
    }

    savedSelections.update { it + LocationSelection.FollowMe }
    currentSelection.set(LocationSelection.FollowMe)

    createScreenModel().test {
      assertThat(awaitItem().showFollowMeButton).isFalse()
    }
  }

  @Test fun `loading state changes to searching on character entry`() {
    createScreenModel().test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)
      sendEvent(InputSearch("A"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
    }
  }

  @Test fun `search not executed until third character entered`() {
    createScreenModel().test {
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

  @Test fun `search with network failure shows error`() {
    createScreenModel().test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)

      api.responseMode = ResponseMode.NETWORK_ERROR

      sendEvent(InputSearch("Fakezroy"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
      with(awaitItem()) {
        assertThat(loadingStatus).isEqualTo(LoadingStatus.SearchingError)
        assertThat(results).isEmpty()
      }
    }
  }

  @Test fun `selecting search result saves selection`() {
    createScreenModel().test {
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
      val expectedSelection = LocationSelection.Static(api.location1)
      assertThat(savedSelections.data.value).containsExactly(expectedSelection)
      assertThat(currentSelection.data.value).isEqualTo(expectedSelection)
    }
  }

  @Test fun `selecting search result with network failure shows error`() {
    createScreenModel().test {
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
    }
  }
}
