package codes.chrishorner.socketweather.home

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.data.Forecaster.State.ErrorType.DATA
import codes.chrishorner.socketweather.data.Forecaster.State.ErrorType.LOCATION
import codes.chrishorner.socketweather.data.Forecaster.State.ErrorType.NETWORK
import codes.chrishorner.socketweather.data.Forecaster.State.ErrorType.NOT_AUSTRALIA

class HomeErrorPresenter(view: View) {

  private val title: TextView = view.findViewById(R.id.home_error_title)
  private val message: TextView = view.findViewById(R.id.home_error_message)
  private val image: ImageView = view.findViewById(R.id.home_error_image)
  private val retryButton: Button = view.findViewById(R.id.home_error_retryButton)

  fun display(errorState: Forecaster.State.Error) {

    when (errorState.type) {

      DATA -> {
        title.setText(R.string.home_error_data_title)
        message.setText(R.string.home_error_data_message)
        image.setImageResource(R.drawable.gfx_data_error)
        retryButton.isVisible = true
      }

      NETWORK -> {
        title.setText(R.string.home_error_network_title)
        message.setText(R.string.home_error_network_message)
        image.setImageResource(R.drawable.gfx_network_error)
        retryButton.isVisible = true
      }

      LOCATION -> {
        title.setText(R.string.home_error_location_title)
        message.setText(R.string.home_error_location_message)
        image.setImageResource(R.drawable.gfx_location_error)
        retryButton.isVisible = true
      }

      NOT_AUSTRALIA -> {
        title.setText(R.string.home_error_unknownLocation_title)
        message.setText(R.string.home_error_unknownLocation_message)
        image.setImageResource(R.drawable.gfx_unknown_location)
        retryButton.isVisible = false
      }
    }
  }
}
