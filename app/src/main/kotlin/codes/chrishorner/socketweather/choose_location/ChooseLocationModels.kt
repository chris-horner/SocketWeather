package codes.chrishorner.socketweather.choose_location

import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Idle
import codes.chrishorner.socketweather.data.SearchResult

data class ChooseLocationState(
    val showCloseButton: Boolean,
    val showFollowMe: Boolean,
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val loadingStatus: LoadingStatus = Idle
) {
  enum class LoadingStatus { Idle, Searching, SearchingError, SearchingDone, Submitting, Submitted }
}

enum class ChooseLocationDataEvent {
  SubmissionError, SubmissionSuccess, PermissionError
}

sealed class ChooseLocationUiEvent {
  data class ResultSelected(val result: SearchResult) : ChooseLocationUiEvent()
  data class InputSearch(val query: String) : ChooseLocationUiEvent()
  object FollowMeClicked : ChooseLocationUiEvent()
  object CloseClicked : ChooseLocationUiEvent()
}
