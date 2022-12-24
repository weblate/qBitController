package dev.bartuzen.qbitcontroller.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import dev.bartuzen.qbitcontroller.R
import java.io.Serializable
import kotlin.math.ceil

fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)

fun Int.toPx(context: Context) = ceil(this * context.resources.displayMetrics.density).toInt()

fun Int.toDp(context: Context) = ceil(this / context.resources.displayMetrics.density).toInt()

inline fun <reified T : Parcelable> Intent.getParcelable(name: String) = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
    getParcelableExtra(name, T::class.java)
} else {
    @Suppress("DEPRECATION")
    getParcelableExtra(name)
}

inline fun <reified T : Parcelable> Bundle.getParcelableCompat(name: String) =
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
        getParcelable(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(name)
    }

inline fun <reified T : Serializable> Bundle.getSerializableCompat(name: String) =
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
        getSerializable(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializable(name)
    }

val Activity.view: View get() = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)

fun Fragment.requireAppCompatActivity() = requireActivity() as AppCompatActivity

fun FragmentTransaction.setDefaultAnimations() {
    setCustomAnimations(
        R.anim.slide_in_right,
        R.anim.slide_out_left,
        R.anim.slide_in_left,
        R.anim.slide_out_right
    )
}
