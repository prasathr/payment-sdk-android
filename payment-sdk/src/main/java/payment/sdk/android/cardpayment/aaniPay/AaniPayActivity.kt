package payment.sdk.android.cardpayment.aaniPay

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import payment.sdk.android.cardpayment.CardPaymentData
import payment.sdk.android.cardpayment.aaniPay.model.AaniPayActivityArgs
import payment.sdk.android.cardpayment.aaniPay.model.AaniPayVMState
import payment.sdk.android.cardpayment.widget.CircularProgressDialog
import payment.sdk.android.sdk.R

class AaniPayActivity : AppCompatActivity() {

    private val inputArgs: AaniPayActivityArgs? by lazy {
        AaniPayActivityArgs.fromIntent(intent = intent)
    }

    private val viewModel: AaniPayViewModel by viewModels { AaniPayViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = runCatching {
            requireNotNull(inputArgs) {
                "AaniPayActivity input arguments were not found"
            }
        }.getOrElse {
            finishWithData(CardPaymentData(CardPaymentData.STATUS_PAYMENT_FAILED))
            return
        }

        setContent {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.aani),
                                color = colorResource(id = R.color.payment_sdk_pay_button_text_color)
                            )
                        },
                        backgroundColor = colorResource(id = R.color.payment_sdk_toolbar_color),
                        navigationIcon = {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    tint = colorResource(id = R.color.payment_sdk_toolbar_icon_color),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { contentPadding ->
                Column(
                    modifier = Modifier.padding(contentPadding)
                ) {
                    val state by viewModel.state.collectAsState()
                    when (state) {
                        is AaniPayVMState.Authorized -> {
                            AaniPayScreen { alias, value ->
                                viewModel.onSubmit(
                                    args = args,
                                    alias = alias,
                                    value = value,
                                    accessToken = (state as AaniPayVMState.Authorized).accessToken
                                )
                            }
                        }

                        is AaniPayVMState.Error -> {
                            finishWithData(
                                CardPaymentData(
                                    CardPaymentData.STATUS_PAYMENT_FAILED,
                                    (state as AaniPayVMState.Error).message
                                )
                            )
                        }

                        AaniPayVMState.Init -> viewModel.authorize(args.authUrl, args.payPageUrl)
                        is AaniPayVMState.Pooling -> {
                            AaniPayTimerScreen(
                                (state as AaniPayVMState.Pooling).amount,
                                (state as AaniPayVMState.Pooling).currencyCode
                            )
                            if (isAaniAppInstalled()) {
                                startActivity(Intent(Intent.ACTION_VIEW).apply {
                                    intent.setData(Uri.parse(((state as AaniPayVMState.Pooling).deepLink)))
                                })
                            }
                        }

                        is AaniPayVMState.Loading -> {
                            CircularProgressDialog((state as AaniPayVMState.Loading).message)
                        }

                        AaniPayVMState.Success -> {
                            finishWithData(CardPaymentData(CardPaymentData.STATUS_PAYMENT_CAPTURED))
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.data?.let { uri ->
            if (uri.scheme == "niannipay" && uri.host == "open") {
            }
        }
    }

    fun isAaniAppInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo("ae.aletihadpayments.aani", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun finishWithData(cardPaymentData: CardPaymentData) {
        val intent = Intent().apply {
            putExtra(CardPaymentData.INTENT_DATA_KEY, cardPaymentData)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}