package codes.chrishorner.socketweather.switch_location

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.switch_location.SwitchLocationAdapter.ViewHolder
import codes.chrishorner.socketweather.util.inflate
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class SwitchLocationAdapter(private val items: List<LocationSelection>) : RecyclerView.Adapter<ViewHolder>() {

  private val clicksChannel = BroadcastChannel<LocationSelection>(1)

  fun clicks(): Flow<LocationSelection> = clicksChannel.asFlow()

  override fun getItemCount(): Int = items.size

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view: View = parent.inflate(R.layout.switch_location_item)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position], position)
  }

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val title: TextView = view.findViewById(R.id.switchLocationItem_title)
    private val subtitle: TextView = view.findViewById(R.id.switchLocationItem_subtitle)
    private val locationIcon: View = view.findViewById(R.id.switchLocationItem_locationIcon)
    private val dropUpIcon: View = view.findViewById(R.id.switchLocationItem_dropUpIcon)

    private var item: LocationSelection? = null

    init {
      view.setOnClickListener { clicksChannel.offer(requireNotNull(item)) }
    }

    fun bind(selection: LocationSelection, position: Int) {
      item = selection
      dropUpIcon.isVisible = position == 0

      when (selection) {
        is LocationSelection.FollowMe -> {
          title.setText(R.string.switchLocation_followMeTitle)
          subtitle.setText(R.string.switchLocation_followMeSubtitle)
          locationIcon.isVisible = true
        }

        is LocationSelection.Static -> {
          title.text = selection.location.name
          subtitle.text = selection.location.state
          locationIcon.isVisible = false
        }

        is LocationSelection.None -> {
          throw IllegalArgumentException("Cannot display LocationSelection.None")
        }
      }
    }
  }
}
