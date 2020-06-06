package codes.chrishorner.socketweather.switch_location

import android.view.View
import android.view.ViewGroup
import androidx.transition.Fade
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.util.addTransition
import codes.chrishorner.socketweather.util.transitionSet
import com.bluelinelabs.conductor.changehandler.androidxtransition.TransitionChangeHandler
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.ScaleProvider

class SwitchLocationChangeHandler : TransitionChangeHandler() {

  override fun getTransition(container: ViewGroup, from: View?, to: View?, isPush: Boolean) = transitionSet {

    val cardFadeAndScale = MaterialFade()
    cardFadeAndScale.secondaryAnimatorProvider = ScaleProvider(isPush).apply {
      incomingStartScale = 0.98f
    }

    addTransition(cardFadeAndScale) {
      addTarget(R.id.switchLocation_card)
    }

    addTransition(Fade()) {
      addTarget(R.id.switchLocation_scrim)
      duration = cardFadeAndScale.duration
    }
  }

  override fun removesFromViewOnPush() = false
}
