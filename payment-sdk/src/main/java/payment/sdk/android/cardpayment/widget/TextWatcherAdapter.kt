package payment.sdk.android.cardpayment.widget

import android.text.Editable
import android.text.TextWatcher

internal abstract class TextWatcherAdapter : TextWatcher {

    abstract override fun afterTextChanged(s: Editable?)

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}