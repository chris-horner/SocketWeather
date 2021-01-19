package codes.chrishorner.socketweather.switch_location

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.switch_location.SwitchLocationAdapter.ViewHolder
import codes.chrishorner.socketweather.util.inflate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Assumes the item at position 0 is the current `LocationSelection`.
 */
class SwitchLocationAdapter(private val items: List<LocationSelection>) : RecyclerView.Adapter<ViewHolder>() {

  private val clicksFlow = MutableSharedFlow<LocationSelection>(extraBufferCapacity = 1)

  fun clicks(): Flow<LocationSelection> = clicksFlow

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
    private val icon: ImageView = view.findViewById(R.id.switchLocationItem_icon)
    private val dropUpIcon: View = view.findViewById(R.id.switchLocationItem_dropUpIcon)

    private var item: LocationSelection? = null

    init {
      view.setOnClickListener { clicksFlow.tryEmit(requireNotNull(item)) }
    }

    fun bind(selection: LocationSelection, position: Int) {
      item = selection
      dropUpIcon.isVisible = position == 0

      val iconDrawable: Drawable? = when {
        position == 0 -> AppCompatResources.getDrawable(itemView.context, R.drawable.ic_check_circle_outline_24dp)
        selection is LocationSelection.FollowMe ->
          AppCompatResources.getDrawable(itemView.context, R.drawable.ic_my_location_24dp)
        else -> null
      }

      if (iconDrawable != null) {
        icon.isVisible = true
        icon.setImageDrawable(iconDrawable)
      } else {
        icon.isVisible = false
      }

      when (selection) {
        is LocationSelection.FollowMe -> {
          title.setText(R.string.switchLocation_followMeTitle)
          subtitle.setText(R.string.switchLocation_followMeSubtitle)
          icon.setImageDrawable(iconDrawable)
        }

        is LocationSelection.Static -> {
          title.text = selection.location.name
          subtitle.text = selection.location.state
        }

        is LocationSelection.None -> {
          throw IllegalArgumentException("Cannot display LocationSelection.None")
        }
      }
    }
  }
}
