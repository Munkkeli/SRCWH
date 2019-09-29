package com.example.srcwh

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_success.view.*
import org.jetbrains.anko.layoutInflater

class DialogHandler {
    var context: Context
    var builder: AlertDialog.Builder
    var dialog: AlertDialog
    lateinit var icon: ImageView
    lateinit var title: TextView
    lateinit var text: TextView

    constructor(context: Context) {
        this.context = context

        builder = createBlankDialogBuilder()
        dialog = builder.create()
    }

    fun open() {
        dialog.show()
    }

    fun setCheckIn() {
        title.text = DIALOG_TITLE_ATTENDED

        dialog.setButton(Dialog.BUTTON_POSITIVE, DIALOG_OK) { dialog, which ->
            Log.d("DIALOG", "OK")
        }
    }

    fun setConfirm() {
        title.text = DIALOG_TITLE_CONFIRM

        dialog.setButton(Dialog.BUTTON_POSITIVE, DIALOG_CONFIRM) { dialog, which ->
            Log.d("DIALOG", "Confirm")
        }

        dialog.setButton(Dialog.BUTTON_NEGATIVE, DIALOG_CANCEL) { dialog, which ->
            Log.d("DIALOG", "Cancel")
        }
    }

    fun setErrorLocation() {
        title.text = DIALOG_TITLE_ERROR
        text.text = DIALOG_TEXT_ERROR_LOCATION.replace("LOCATION", "Leiritie 1")

        dialog.setButton(Dialog.BUTTON_POSITIVE, DIALOG_CONFIRM) { dialog, which ->
            Log.d("DIALOG", "Confirm")
        }

        dialog.setButton(Dialog.BUTTON_NEGATIVE, DIALOG_CANCEL) { dialog, which ->
            Log.d("DIALOG", "Cancel")
        }
    }

    fun createBlankDialogBuilder(): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        val layout = context.layoutInflater.inflate(R.layout.dialog_success, null)

        icon = layout.dialog_success_icon
        title = layout.dialog_success_title
        text = layout.dialog_success_text

        builder.setView(layout)
        return builder
    }
}