package codes.chrishorner.socketweather.util

import android.app.Application
import android.content.Context

val Context.app: Application
  get() = applicationContext as Application
