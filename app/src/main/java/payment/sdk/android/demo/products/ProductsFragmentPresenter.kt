package payment.sdk.android.demo.products

import io.reactivex.disposables.CompositeDisposable
import payment.sdk.android.demo.dependency.configuration.Configuration
import payment.sdk.android.demo.products.data.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Random
import javax.inject.Inject
import kotlin.collections.ArrayList

class ProductsFragmentPresenter @Inject constructor(
        private val view: ProductsFragmentContract.View,
        private val configuration: Configuration

) : ProductsFragmentContract.Presenter {

    private val subscriptions = CompositeDisposable()

    override fun init() {
        view.showProgress(false)
        view.bindData(this.getProducts())
    }

    fun getProducts(): List<ProductDomain> {
        val settings = Pair(configuration.locale, configuration.currency)
        val locale = settings.first
        val currency = settings.second
//        val language = locale.language.toLowerCase()
        val products = ArrayList<ProductDomain>()
        val min = 0.1
        val max = 3.0
        for (i in 1..6) {
            val randomValue = min + (max - min) * Random().nextDouble()
            val priceInDouble = BigDecimal(randomValue).setScale(2, RoundingMode.UP).toDouble()
            val price = Price(currency, BigDecimal(priceInDouble) , BigDecimal(0))
            products.add(ProductDomain("$i", "Furniture $i", "Just a furniture", listOf(price, price), "file:///android_asset/images/0$i.jpg" ))
        }
        products.add(ProductDomain("${products.size + 2}", "Furniture 6", "Just a furniture", listOf(Price(currency, BigDecimal(2220),  BigDecimal(0))), "file:///android_asset/images/02.jpg" ))
        products.add(ProductDomain("${products.size + 3}", "Furniture 7", "Just a furniture", listOf(Price(currency, BigDecimal(1500),  BigDecimal(0))), "file:///android_asset/images/02.jpg" ))
        products.add(ProductDomain("${products.size + 4}", "Furniture 8", "Just a furniture", listOf(Price(currency, BigDecimal(450),  BigDecimal(0))), "file:///android_asset/images/02.jpg" ))
        products.add(ProductDomain("${products.size + 5}", "Furniture 9", "Just a furniture", listOf(Price(currency, BigDecimal(3000),  BigDecimal(0))), "file:///android_asset/images/02.jpg" ))
        return  products
    }

    override fun cleanup() {
        subscriptions.dispose()
    }
}
