package codes.chrishorner.socketweather

import android.app.Activity
import android.view.ViewGroup

object BuildTypeConfig {

  fun getRootContainerFor(activity: Activity): ViewGroup {
    return activity.findViewById(android.R.id.content) as ViewGroup
  }
}
