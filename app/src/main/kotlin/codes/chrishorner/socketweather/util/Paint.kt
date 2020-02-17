package codes.chrishorner.socketweather.util

import android.graphics.Paint

// See https://stackoverflow.com/a/42091739 for height calculation rationale
fun Paint.textHeight(): Float = fontMetrics.let { it.descent - it.ascent }
