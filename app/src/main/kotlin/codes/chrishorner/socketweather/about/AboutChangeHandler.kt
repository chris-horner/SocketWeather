package codes.chrishorner.socketweather.about

import android.view.View
import android.view.ViewGroup
import androidx.transition.Transition
import com.bluelinelabs.conductor.changehandler.androidxtransition.TransitionChangeHandler
import com.google.android.material.transition.MaterialSharedAxis

class AboutChangeHandler : TransitionChangeHandler() {
  override fun getTransition(container: ViewGroup, from: View?, to: View?, isPush: Boolean): Transition {
    return MaterialSharedAxis(MaterialSharedAxis.Z, isPush).setDuration(250)
  }
}
