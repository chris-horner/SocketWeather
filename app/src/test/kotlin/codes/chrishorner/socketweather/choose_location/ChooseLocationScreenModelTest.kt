package codes.chrishorner.socketweather.choose_location

import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.InputSearch
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.test.FakeStore
import codes.chrishorner.socketweather.test.TestApi
import codes.chrishorner.socketweather.test.TestApi.ResponseMode
import codes.chrishorner.socketweather.test.test
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class ChooseLocationScreenModelTest {

  private val clock: Clock
  private val api: TestApi
  private val currentSelection = FakeStore<LocationSelection>(LocationSelection.None)
  private val savedSelections = FakeStore<Set<LocationSelection>>(emptySet())
  private val screenModel: ChooseLocationScreenModel

  init {
    val fixedDateTime = ZonedDateTime.of(2022, 2, 20, 8, 0, 0, 0, ZoneId.of("Australia/Melbourne"))
    clock = Clock.fixed(fixedDateTime.toInstant(), fixedDateTime.zone)
    api = TestApi(clock)
    screenModel = ChooseLocationScreenModel(false, api, currentSelection, savedSelections)
  }

  @Test fun `loading state changes to searching on character entry`() {
    screenModel.test {
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Idle)
      sendEvent(InputSearch("A"))
      assertThat(awaitItem().loadingStatus).isEqualTo(LoadingStatus.Searching)
    }
  }

  @Test fun `search not executed until third character entered`() {
    screenModel.test {
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
    screenModel.test {
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
}
