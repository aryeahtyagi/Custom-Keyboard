package com.atria.software.rise

import android.R.attr.label
import android.R.attr.text
import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData


class MyKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var textBox: EditText
    private lateinit var secTextBox: EditText
    private lateinit var lockButton: ImageView
    private lateinit var secretMessageLayout: LinearLayout
    private lateinit var linearLayout: LinearLayout
    private lateinit var secretButton: ImageView
    private lateinit var secretTextView: TextView
    private lateinit var heading_text_view: TextView
    private lateinit var keyboarview: KeyboardView

    private var focusView: EditText? = null
    private val lock_icon = R.drawable.ic_lock
    private val secretIcon = R.drawable.ic_dec

    private var isModeOpen: Boolean = false
    private var isSecretOpen: Boolean = false
    private var isCapsOn = false
    private lateinit var sendButton: ImageView

    private lateinit var linearLayoutTwo: LinearLayout
    private lateinit var messageLayoutTwo: TextView

    override fun onViewClicked(focusChanged: Boolean) {
        target?.isCursorVisible = false
        target = null
        super.onViewClicked(focusChanged)
    }

    // 0 means started
    private var stateCallback = 0

    private enum class CASE_STATE {
        LOWER,
        UPPERONE,
        UPPERALL
    }

    private var state: CASE_STATE = CASE_STATE.LOWER

    companion object {
        private const val TAG = "MyKeyboardService"

    }

    private var specialKeyboardMode = false

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        keyboarview.visibility = View.VISIBLE
        secretMessageLayout.visibility = View.GONE
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.i(TAG, "onStartInputView: $isSecretOpen + $isModeOpen")
        if (isSecretOpen) {
            secretButton.performClick()
        }
    }


    override fun onCreateInputView(): View {
        stateCallback = 0
        val constView =
            layoutInflater.inflate(R.layout.keyboard_view, LinearLayout(this)) as LinearLayout
        keyboarview = constView.findViewById<KeyboardView>(R.id.keyboardView)
        val topbar = constView.findViewById<LinearLayout>(R.id.topBar)
        sendButton = constView.findViewById<ImageView>(R.id.sendButton)
        secretTextView = constView.findViewById(R.id.messageTextView)
        secretButton = constView.findViewById<ImageView>(R.id.secretButton)
        linearLayout = constView.findViewById<LinearLayout>(R.id.textLinearLayout)
        textBox = constView.findViewById(R.id.inputBox)
        secTextBox = constView.findViewById(R.id.box2)
        lockButton = constView.findViewById(R.id.lockButton)
        secretMessageLayout = constView.findViewById(R.id.secretMessageLayout)
        heading_text_view = topbar.findViewWithTag("heading")

        linearLayoutTwo = constView.findViewById(R.id.linearLayout2)
        messageLayoutTwo = constView.findViewById(R.id.messageLayoutTwo)
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.description.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            val text = clip.getItemAt(0).text.toString()
            if (text.isBlank() || text.trim() == "") {
                messageLayoutTwo.text =
                    "No Message Selected. Please copy message to clipboard and then press decrypt button"
            } else {
                messageLayoutTwo.text = text
            }
        }


        if (isModeOpen) {

            // here it means that some bug has happened
            lockButton.setImageResource(R.drawable.ic_baseline_arrow_back_24)
            linearLayout.visibility = View.VISIBLE
            secretButton.visibility = View.GONE
            sendButton.visibility = View.VISIBLE
            Log.i(TAG, "onCreateInputView: $target")
            textBox.setText(firstText)
            secTextBox.setText(secText)
            if (target?.tag == "one") {
                target = textBox
                textBox.requestFocus()
                textBox.isCursorVisible = true
                textBox.setSelection(focusView?.text?.length ?: 1)
                textBox.setBackgroundResource(R.drawable.border)
            } else if (target?.tag == "two") {
                target = secTextBox
                secTextBox.requestFocus()
                secTextBox.isCursorVisible = true
                secTextBox.setSelection(focusView?.text?.length ?: 1)
                secTextBox.setBackgroundResource(R.drawable.border)
            }

        }

        secretButton.setOnClickListener {
            isSecretOpen = !isSecretOpen

            if (isSecretOpen) {
                heading_text_view.text = "Secret Message"
                val clipboard: ClipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip
                if (clip != null && clip.description.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                    val text = clip.getItemAt(0).text.toString()
                    if (text.isBlank() || text.trim() == "") {
                        secretTextView.text =
                            "No Message Selected. Please copy message to clipboard and then press decrypt button"
                    } else {
                        secretTextView.text = text
                    }
                }
                lockButton.visibility = View.GONE
                secretButton.setImageResource(R.drawable.ic_baseline_keyboard_hide_24)
                keyboarview.visibility = View.GONE
                secretMessageLayout.visibility = View.VISIBLE

                if (secretMessageLayout.visibility == View.VISIBLE) {
                    messageLayoutTwo.visibility = View.GONE
                } else {
                    messageLayoutTwo.visibility = View.VISIBLE
                }

            } else {
                heading_text_view.text = ""
                lockButton.visibility = View.VISIBLE
                secretButton.setImageResource(secretIcon)
                keyboarview.visibility = View.VISIBLE
                secretMessageLayout.visibility = View.GONE
            }
        }

        sendButton.setOnClickListener {
            target = null
            val inputConnection = currentInputConnection
            val first = textBox.text.toString();
            val sec = secTextBox.text.toString();
            if (TextUtils.isEmpty(first)) {
                textBox.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
                return@setOnClickListener
            } else if (TextUtils.isEmpty(sec)) {
                secTextBox.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
                return@setOnClickListener
            }
            inputConnection.commitText(
                textBox.text.toString() + " " + secTextBox.text.toString(),
                1
            )
            lockButton.setImageResource(lock_icon)
            secretButton.visibility = View.VISIBLE
            linearLayout.visibility = View.GONE
            sendButton.visibility = View.GONE
            textBox.setText("")
            secTextBox.setText("")
            isModeOpen = false
        }

        textBox.setOnTouchListener { v, event ->
            target = textBox
            target?.requestFocus()
            target?.isCursorVisible = true
            target?.setSelection(target?.text?.length ?: 1)
            textBox.setBackgroundResource(R.drawable.border)
            secTextBox.setBackgroundColor(resources.getColor(R.color.white))
            return@setOnTouchListener true
        }
        secTextBox.setOnTouchListener { v, event ->
            target = secTextBox
            target?.requestFocus()
            target?.isCursorVisible = true
            target?.setSelection(target?.text?.length ?: 1)
            secTextBox.setBackgroundResource(R.drawable.border)
            textBox.setBackgroundColor(resources.getColor(R.color.white))
            return@setOnTouchListener true
        }




        lockButton.setOnClickListener {
            isModeOpen = !isModeOpen
            textBox.setBackgroundColor(resources.getColor(R.color.white))
            secTextBox.setBackgroundColor(resources.getColor(R.color.white))
            isModeOpen.doOperation()
        }

        val keyboard = if (!specialKeyboardMode) {
            if (!isCapsOn) {
                Keyboard(this, R.xml.custom_pad)
            } else {
                Keyboard(this, R.xml.upper_pad)
            }
        } else {
            Keyboard(this, R.xml.special_pad)
        }
        keyboarview.keyboard = keyboard
        keyboarview.setOnKeyboardActionListener(this)
        stateCallback = 1
        return constView

    }

    private fun Boolean.doOperation() {
        if (this) {
            lockButton.setImageResource(R.drawable.ic_baseline_arrow_back_24)
            linearLayout.visibility = View.VISIBLE
            secretButton.visibility = View.GONE
            sendButton.visibility = View.VISIBLE
        } else {
            target = null
            lockButton.setImageResource(lock_icon)
            textBox.setText("")
            secTextBox.setText("")
            linearLayout.visibility = View.GONE
            secretButton.visibility = View.VISIBLE
            sendButton.visibility = View.GONE

        }
    }


    var firstText = ""
    var secText = ""

    override fun onPress(i: Int) {}

    override fun onRelease(i: Int) {}

    // 0 points to next
    // 1 points to send
    var target: EditText? = null

    override fun onKey(primatyCode: Int, keyCodes: IntArray?) {
        val inputConnection = currentInputConnection

        when (primatyCode) {
            999 -> {
                requestHideSelf(0)
            }
            9 -> {
                val imeManager: InputMethodManager = applicationContext.getSystemService(
                    INPUT_METHOD_SERVICE
                ) as InputMethodManager
                imeManager.showInputMethodPicker()
            }
            998 -> {
                specialKeyboardMode = !specialKeyboardMode
                firstText = textBox.text.toString()
                secText = secTextBox.text.toString()
                focusView = target
                val v = onCreateInputView()
                setInputView(v)
            }
            997 -> {
                if (stateCallback == 1) {
                    isCapsOn = !isCapsOn
                    val onCreateInputView = onCreateInputView()
                    setInputView(onCreateInputView)
                }
            }
            Keyboard.KEYCODE_DELETE -> {
                if (isModeOpen && target != null) {
                    target?.setText(target?.text?.removeLast())
                    target?.setSelection(target?.text?.length ?: 1)
                } else {
                    val selectedText = inputConnection.getSelectedText(0)
                    if (TextUtils.isEmpty(selectedText)) {
                        inputConnection.deleteSurroundingText(1, 0)
                    } else {
                        inputConnection.commitText("", 1)
                    }
                }
            }
            else -> {
                val char = primatyCode.toChar()
                if (isModeOpen && target != null) {
                    target?.setText(target?.text.toString() + char.toString())
                    target?.setSelection(target?.text?.length ?: 1)
                } else {
                    val code = primatyCode.toChar()
                    inputConnection.commitText(code.toString(), 1)
                }
            }
        }

    }


    private fun Editable.removeLast(): String {
        return this.toString().dropLast(1)
    }


    override fun onText(charSequence: CharSequence?) {}

    override fun swipeLeft() {}

    override fun swipeRight() {}

    override fun swipeDown() {}

    override fun swipeUp() {}

}