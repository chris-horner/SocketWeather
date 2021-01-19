package codes.chrishorner.socketweather.choose_location

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.choose_location.ChooseLocationSearchAdapter.ViewHolder
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.util.inflate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class ChooseLocationSearchAdapter : RecyclerView.Adapter<ViewHolder>() {

  private val clicksFlow = MutableSharedFlow<SearchResult>(extraBufferCapacity = 1)
  private var items: List<SearchResult> = emptyList()

  fun clicks(): Flow<SearchResult> = clicksFlow

  fun set(items: List<SearchResult>) {
    this.items = items
    notifyDataSetChanged()
  }

  override fun getItemCount(): Int = items.size

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view: View = parent.inflate(R.layout.search_result)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val nameView: TextView = view.findViewById(R.id.searchResult_name)
    private val stateView: TextView = view.findViewById(R.id.searchResult_state)
    private val postcodeView: TextView = view.findViewById(R.id.searchResult_postcode)

    private var item: SearchResult? = null

    init {
      view.setOnClickListener { clicksFlow.tryEmit(requireNotNull(item)) }
    }

    fun bind(result: SearchResult) {
      item = result
      nameView.text = result.name
      stateView.text = result.state
      postcodeView.text = result.postcode
    }
  }
}
