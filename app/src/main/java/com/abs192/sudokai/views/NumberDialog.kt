package com.abs192.sudokai.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import com.abs192.sudokai.R
import com.abs192.sudokai.listeners.NumberDialogSelectedListener
import java.lang.Exception

class NumberDialog(
    context: Context,
    private var numberDialogSelectedListener: NumberDialogSelectedListener
) :
    Dialog(context), View.OnClickListener {

    private var buttonEditNum1: Button? = null
    private var buttonEditNum2: Button? = null
    private var buttonEditNum3: Button? = null
    private var buttonEditNum4: Button? = null
    private var buttonEditNum5: Button? = null
    private var buttonEditNum6: Button? = null
    private var buttonEditNum7: Button? = null
    private var buttonEditNum8: Button? = null
    private var buttonEditNum9: Button? = null

    private var buttonEditDel: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.layout_dialog_numbers)
        buttonEditNum1 = findViewById(R.id.buttonEditNum1)
        buttonEditNum2 = findViewById(R.id.buttonEditNum2)
        buttonEditNum3 = findViewById(R.id.buttonEditNum3)
        buttonEditNum4 = findViewById(R.id.buttonEditNum4)
        buttonEditNum5 = findViewById(R.id.buttonEditNum5)
        buttonEditNum6 = findViewById(R.id.buttonEditNum6)
        buttonEditNum7 = findViewById(R.id.buttonEditNum7)
        buttonEditNum8 = findViewById(R.id.buttonEditNum8)
        buttonEditNum9 = findViewById(R.id.buttonEditNum9)
        buttonEditDel = findViewById(R.id.buttonEditDel)
        buttonEditNum1?.setOnClickListener(this)
        buttonEditNum2?.setOnClickListener(this)
        buttonEditNum3?.setOnClickListener(this)
        buttonEditNum4?.setOnClickListener(this)
        buttonEditNum5?.setOnClickListener(this)
        buttonEditNum6?.setOnClickListener(this)
        buttonEditNum7?.setOnClickListener(this)
        buttonEditNum8?.setOnClickListener(this)
        buttonEditNum9?.setOnClickListener(this)
        buttonEditDel?.setOnClickListener {
            numberDialogSelectedListener.numberSelected(-1)
            dismiss()
        }
    }

    override fun onClick(v: View?) {
        val button: Button? = v as Button?
        val num = try {
            button?.text.toString().toInt()
        } catch (e: Exception) {
            -1
        }
        numberDialogSelectedListener.numberSelected(num)
        dismiss()
    }

}