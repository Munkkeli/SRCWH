package com.example.srcwh.dialog

import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import com.example.srcwh.*

typealias DialogActionHandler = (action: DialogAction) -> Unit

enum class DialogViewTemplate {
    DEFAULT,
    LOADING,
    SUCCESS,
    QUESTION,
    ERROR
}

/*
 * This is a helper class to make editing the custom dialog simpler and better on the eyes.
 */
class DialogBuilder(private val dialog: DialogContainerFragment) {
    private val DC = DialogConstants(dialog.context!!)

    private var title = ""
    private var icon = DC.ICON_LOADING
    private var text = ""
    private var location: String? = null

    private var primary: String? = DC.BUTTON_OK
    private var secondary: String? = null
    private var cancel: String? = null

    private var actionHandler: DialogActionHandler? = null

    private var isLoading = false
    private var isSuccess = false

    fun begin(): DialogBuilder {
        var view = dialog.containerView
        if (dialog.isAdded && !dialog.isHidden && view != null) {
            dialog.lastHeight = view.height
        }

        setViewTemplate(DialogViewTemplate.LOADING)
        actionHandler = null
        isLoading = false

        return this
    }

    // Change to one of the predefined dialog layouts
    fun setViewTemplate(template: DialogViewTemplate): DialogBuilder {
        var title = ""
        var icon = DC.ICON_LOADING
        var text = ""

        isLoading = false
        isSuccess = false

        when (template) {
            DialogViewTemplate.LOADING -> {
                title = DC.TITLE_LOADING

                isLoading = true

                setActions()
                setDismissible(false)
            }
            DialogViewTemplate.SUCCESS -> {
                icon = DC.ICON_CHECK

                isSuccess = true

                setActions(DC.BUTTON_OK)
                setDismissible()
            }
            DialogViewTemplate.QUESTION -> {
                title = DC.TITLE_CONFIRM

                setActions(DC.BUTTON_CONFIRM, null, DC.BUTTON_CANCEL)
                setDismissible(false)
            }
            DialogViewTemplate.ERROR -> {
                icon = DC.ICON_ERROR
                title = DC.TITLE_ERROR
                text = DC.TEXT_ERROR

                setActions(DC.BUTTON_OK)
                setDismissible()
            }
            else -> {
                setActions(DC.BUTTON_OK)
                setDismissible()
            }
        }

        this.title = title
        this.icon = icon
        this.text = text

        return this
    }

    fun setTitle(title: String): DialogBuilder {
        this.title = title

        return this
    }

    fun setIcon(icon: Int): DialogBuilder {
        this.icon = icon
        this.location = null

        return this
    }

    fun setText(text: String): DialogBuilder {
        this.text = text

        return this
    }

    fun setLocation(location: String): DialogBuilder {
        this.location = location

        return this
    }

    fun setAction(action: DialogAction, text: String?): DialogBuilder {
        this.primary = when (action) {
            DialogAction.PRIMARY -> text
            else -> this.primary
        }

        this.secondary = when (action) {
            DialogAction.SECONDARY -> text
            else -> this.primary
        }

        this.cancel = when (action) {
            DialogAction.CANCEL -> text
            else -> this.cancel
        }

        return this
    }

    fun setActions(
        primary: String? = null,
        secondary: String? = null,
        cancel: String? = null
    ): DialogBuilder {
        this.primary = primary
        this.secondary = secondary
        this.cancel = cancel

        return this
    }

    fun setDismissible(isDismissible: Boolean = true): DialogBuilder {
        dialog.isCancelable = isDismissible

        return this
    }

    fun setOnActionHandler(actionHandler: DialogActionHandler?): DialogBuilder {
        this.actionHandler = actionHandler

        return this
    }

    fun build() {
        updateContentFragment()
        updateActionFragment()
    }

    private fun defaultActionHandler(action: DialogAction) {
        if (action == DialogAction.PRIMARY || action == DialogAction.CANCEL) {
            dialog.close()
        }
    }

    private fun setMinHeight(height: Int) {
        var view = dialog.containerView
        if (dialog.isAdded && !dialog.isHidden && view != null) {
            view.minimumHeight = height
        }
    }

    private fun updateContentFragment() {
        // Keep the dialog from jumping around when loading
        var minHeight: Int? = null
        if (isLoading && dialog.lastHeight > 0) {
            minHeight = dialog.lastHeight
        }

        var accessibilityTitle = this.title
        if (isLoading) accessibilityTitle = "Loading"

        dialog.containerView!!.contentDescription = accessibilityTitle
        dialog.containerView!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)

        changeContentFragment(
            DialogContentFragment(
                this.icon,
                this.title,
                this.text,
                this.location,
                isSuccess,
                dialog.containerView,
                minHeight
            )
        )
    }

    private fun updateActionFragment() {
        changeActionFragment(
            DialogActionFragment(
                this.primary,
                this.secondary,
                this.cancel,
                actionHandler ?: { action -> defaultActionHandler(action) }
            )
        )
    }

    private fun changeContentFragment(fragment: Fragment) {
        if (dialog.isAdded && !dialog.childFragmentManager.isDestroyed) {
            val transaction = dialog.childFragmentManager.beginTransaction()

            Log.d("DIALOG", "Replacing fragment with ${fragment.toString()}")

            if (!dialog.isFirstLoad) {
                transaction.setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
            } else {
                dialog.isFirstLoad = false
            }

            transaction.replace(R.id.dialog_fragment_container, fragment)
            transaction.commit()

            // dialog.childFragmentManager.executePendingTransactions()
        }
    }

    private fun changeActionFragment(fragment: Fragment) {
        if (dialog.isAdded && !dialog.childFragmentManager.isDestroyed) {
            val transaction = dialog.childFragmentManager.beginTransaction()

            transaction.replace(R.id.dialog_action_container, fragment)
            transaction.commit()
        }
    }
}