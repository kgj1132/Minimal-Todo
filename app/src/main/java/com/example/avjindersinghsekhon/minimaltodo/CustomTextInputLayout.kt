package com.example.avjindersinghsekhon.minimaltodo


import android.content.Context
import android.graphics.Canvas
import android.support.design.widget.TextInputLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

class CustomTextInputLayout : TextInputLayout {

    private var mIsHintSet: Boolean = false
    private var mHint: CharSequence? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is EditText) {
            // Since hint will be nullify on EditText once on parent addView, store hint value locally
            mHint = child.hint
        }
        super.addView(child, index, params)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!mIsHintSet && ViewCompat.isLaidOut(this)) {
            // We have to reset the previous hint so that equals check pass
            hint = null

            // In case that hint is changed programatically
            val currentEditTextHint = editText!!.hint

            if (currentEditTextHint != null && currentEditTextHint.isNotEmpty()) {
                mHint = currentEditTextHint
            }
            hint = mHint
            mIsHintSet = true
        }
    }

}

