package codes.chrishorner.socketweather

import android.app.Activity
import android.view.ViewGroup
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout

object BuildTypeConfig {

  fun getRootContainerFor(activity: Activity): ViewGroup {
    val content = activity.findViewById(android.R.id.content) as ViewGroup
    val container = ChangeHandlerFrameLayout(activity)
    content.addView(container)
    return container
  }
}
