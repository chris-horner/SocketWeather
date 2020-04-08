package codes.chrishorner.socketweather.debug

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.BaseAdapter
import android.widget.Spinner
import android.widget.TextView
import codes.chrishorner.socketweather.util.inflate

fun Spinner.onItemSelected(action: (position: Int) -> Unit) {

  onItemSelectedListener = object : OnItemSelectedListener {

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
      action(position)
    }
  }
}

abstract class SpinnerAdapter<T> : BaseAdapter() {

  abstract override fun getItem(position: Int): T

  abstract fun bindView(item: T, position: Int, view: TextView)

  final override fun getItemId(position: Int): Long = position.toLong()

  final override fun getView(position: Int, view: View?, container: ViewGroup): View {
    val textView: TextView = view as TextView? ?: container.inflate(android.R.layout.simple_spinner_item)
    bindView(getItem(position), position, textView)
    return textView
  }

  final override fun getDropDownView(position: Int, view: View?, container: ViewGroup): View {
    val textView: TextView = view as TextView? ?: container.inflate(android.R.layout.simple_spinner_dropdown_item)
    bindView(getItem(position), position, textView)
    return textView
  }
}

