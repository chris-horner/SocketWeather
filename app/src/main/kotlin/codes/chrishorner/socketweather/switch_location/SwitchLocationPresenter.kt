package codes.chrishorner.socketweather.switch_location

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.switch_location.SwitchLocationPresenter.Event.AddLocationClicked
import codes.chrishorner.socketweather.switch_location.SwitchLocationPresenter.Event.DismissClicked
import codes.chrishorner.socketweather.switch_location.SwitchLocationPresenter.Event.LocationClicked
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import reactivecircus.flowbinding.android.view.clicks

class SwitchLocationPresenter(view: View, selections: List<LocationSelection>) {

  val events: Flow<Event>

  init {
    view.updatePaddingWithInsets(left = true, top = true, right = true, bottom = true)
    val recycler: RecyclerView = view.findViewById(R.id.switchLocation_recycler)
    val adapter = SwitchLocationAdapter(selections)
    recycler.adapter = adapter
    recycler.layoutManager = LinearLayoutManager(view.context)
    recycler.addItemDecoration(SwitchLocationDecorator(view.context))

    val container: View = view.findViewById(R.id.switchLocation_container)
    val addButton: View = view.findViewById(R.id.switchLocation_addButton)

    events = merge(
        container.clicks().map { DismissClicked },
        addButton.clicks().map { AddLocationClicked },
        adapter.clicks().map { LocationClicked(it) }
    )
  }

  sealed class Event {
    data class LocationClicked(val selection: LocationSelection) : Event()
    object AddLocationClicked : Event()
    object DismissClicked : Event()
  }
}
