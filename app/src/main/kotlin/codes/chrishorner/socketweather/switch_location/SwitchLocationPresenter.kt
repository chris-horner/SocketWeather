package codes.chrishorner.socketweather.switch_location

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.util.updatePaddingWithInsets

class SwitchLocationPresenter(view: View, selections: List<LocationSelection>) {

  init {
    view.updatePaddingWithInsets(left = true, top = true, right = true, bottom = true)
    val recycler: RecyclerView = view.findViewById(R.id.switchLocation_recycler)
    recycler.adapter = SwitchLocationAdapter(selections)
    recycler.layoutManager = LinearLayoutManager(view.context)
  }
}
