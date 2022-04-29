package ru.vladleesi.ultimatescanner

import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation

object Animations {
    val ROTATE = RotateAnimation(0f, 350f, 15f, 15f).apply {
        interpolator = LinearInterpolator()
        repeatCount = Animation.INFINITE
        duration = 700
    }
}
