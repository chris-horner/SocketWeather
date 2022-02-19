package codes.chrishorner.socketweather.choose_location

import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Idle
import codes.chrishorner.socketweather.data.SearchResult

data class ChooseLocationState(
  val showCloseButton: Boolean,
  val showFollowMeButton: Boolean,
  val query: String = "",
  val results: List<SearchResult> = emptyList(),
  val loadingStatus: LoadingStatus = Idle,
  val error: Error? = null
) {
  enum class LoadingStatus { Idle, Searching, SearchingError, SearchingDone, Submitting, Submitted }
  enum class Error { Permission, Submission }
}

sealed class ChooseLocationUiEvent {
  data class ResultSelected(val result: SearchResult) : ChooseLocationUiEvent()
  data class InputSearch(val query: String) : ChooseLocationUiEvent()
  object ClearInput : ChooseLocationUiEvent()
  data class FollowMeClicked(val hasLocationPermission: Boolean) : ChooseLocationUiEvent()
  object CloseClicked : ChooseLocationUiEvent()
}
