package it.unicampania.util

import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.Transformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Fragment.snackBarMessage(message: () -> String) {
    Snackbar.make(this.view!!, message(), Snackbar.LENGTH_LONG).show()
}

fun Fragment.snackBarMessageIf(bool: Boolean, message: () -> String) {
    if (bool) {
        Snackbar.make(this.view!!, message(), Snackbar.LENGTH_LONG).show()
    }
}

fun <T : Any> T.info(message: () -> String) {
    Log.i(this.javaClass.canonicalName, message())
}


fun <T : Any> T.error(message: () -> String) {
    Log.e(this.javaClass.canonicalName, message())
}

fun View.expand() {
    this.measure(
        View.MeasureSpec.makeMeasureSpec((parent as View).width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    val targetHeight = this.measuredHeight
    this.layoutParams.height = 1
    this.visibility = View.VISIBLE

    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            this@expand.layoutParams.height = if (interpolatedTime == 1f) {
                WindowManager.LayoutParams.WRAP_CONTENT
            } else {
                (targetHeight * interpolatedTime).toInt()
            }
            this@expand.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = 500.toLong()
    this.startAnimation(a)
}

fun View.collapse() {
    val initialHeight = this.measuredHeight

    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                this@collapse.visibility = View.GONE
            } else {
                this@collapse.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                this@collapse.requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = 500.toLong()
    this.startAnimation(a)
}

fun View.setAnimateOnClickListener(viewToAnimate: View, viewToRotate: View, bool: () -> Boolean, block: () -> Unit) {
    setOnClickListener {
        if (bool()) {
            viewToAnimate.collapse()
            viewToRotate.animate().rotation(0f).apply { duration = 800 }.start()
        } else {
            viewToAnimate.expand()
            viewToRotate.animate().rotation(90f).apply { duration = 800 }.start()
        }
        block()
    }
}


suspend fun <T> onDefault(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.Default) {
        block()
    }
}

suspend fun <T> onIO(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.IO) {
        block()
    }
}

suspend fun <T> onMain(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.Main) {
        block()
    }
}