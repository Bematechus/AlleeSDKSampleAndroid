package com.bematech.alleesdksampleandroid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.bematech.alleesdk.AlleeSDK
import com.bematech.alleesdkcore.AlleeSDKCore
import com.bematech.alleesdkcore.Models.*
import com.bematech.alleesdkcore.OrdersBumpStatusListener
import com.bematech.bsockethelper.Callback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_status.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private var items: MutableList<CustomOrder?> = mutableListOf()
    private lateinit var orderStatusAdapter: OrderStatusAdapter;

    private var nextOrderId = (1..1000000).shuffled().first()
    private var sending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etStoreKey.setSelection(etStoreKey.text?.length ?: 0)

        setListener(etStoreKey)
        setListener(etPort)
        setListener(etStation)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels

        rvList.layoutManager = GridLayoutManager(this, width / 116.dpToPx(this))
        rvList.adapter = OrderAdapter(this, items)

        orderStatusAdapter = OrderStatusAdapter(this);

        rvOrderStatus.layoutManager = LinearLayoutManager(this)
        rvOrderStatus.adapter = orderStatusAdapter

        appbar.measure(0, 0)
        bottomSheet.layoutParams.height = height - appbar.measuredHeight - 80.dpToPx(this)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        rvList.alpha = 0f

        val envIndex = Pref.getEnv(this)
        val storeKey = Pref.getStoreKey(this)
        val port = Pref.getPort(this)

        rgEnv.check(envIdByIndex(envIndex))

        etStoreKey.setText(storeKey)
        etPort.setText(port)
        etStation.setText(Pref.getStation(this))

        val env = AlleeSDK.Environment.values()[envIndex]

        AlleeSDK.shared.start(this, storeKey ?: "", port?.toIntOrNull() ?: 0, env)
        AlleeSDK.shared.ordersBumpStatusListener = OrdersBumpStatusListener {
            runOnUiThread {
                orderStatusAdapter.updateStatus(it)
            }
        }

        tvMessage.text = ""

        rgEnv.setOnCheckedChangeListener { _, envSelected ->
            Pref.setEnv(envIndexById(envSelected), this)

            AlertDialog.Builder(this).setTitle(getString(R.string.change_env_title))
                .setMessage(getString(R.string.change_env_msg))
                .setCancelable(false)
                .setPositiveButton(R.string.btn_ok) { _, _ ->
                    finish()
                }
                .show()
        }

        load()
    }

    private fun load() {
        Thread(Runnable {
            items.clear()
            (1..4).forEach { _ -> items.add(null) }

            items.addAll(Pref.getCustomOrders(this))

            runOnUiThread {
                rvList.adapter?.notifyDataSetChanged()

                if (rvList.alpha == 0f) {
                    rvList.animate().apply {
                        duration = 250
                        alpha(1f)
                        start()
                    }

                    spinner.animate().apply {
                        duration = 250
                        alpha(0f)
                        start()
                    }
                }
            }

        }).start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            load()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setListener(editText: EditText?) {
        editText?.setOnFocusChangeListener { view, isFocused ->
            if (!isFocused) {
                val value = (view as EditText).text.toString()

                when (editText.id) {
                    R.id.etStoreKey -> {
                        Pref.setStoreKey(value, this)
                        AlleeSDK.shared.updateStoreKey(value)
                    }

                    R.id.etPort-> {
                        Pref.setPort(value, this)
                        Thread(Runnable {
                            AlleeSDK.shared.updatePort(value.toIntOrNull() ?: 0)
                        }).start()
                    }

                    R.id.etStation-> Pref.setStation(value, this)

                }
            }
        }

        editText?.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                view.clearFocus()
            }

            false
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun actionAdd(button: View) {
        startActivityForResult(Intent(this, FormActivity::class.java), 0)
    }

    @Suppress("UNUSED_PARAMETER")
    fun actionRefresh(button: View) {
        AlleeSDK.shared.requestOrdersStatus(Callback {
            it.let { error -> print(error) }
        })
    }

    private fun callback(orderId: String): Callback {
        return Callback { error ->
            sending = false

            runOnUiThread {
                error?.let {
                    print(error)
                    tvMessage.text = error

                } ?: run {
                    tvMessage.text = getString(R.string.order_sent)
                    orderStatusAdapter.addOrderId(orderId)
                }
            }
        }
    }

    private fun cantSend(): Boolean {
        if (sending) {
            return false
        }

        sending = true

        tvMessage.text = getString(R.string.order_sending)
        return true
    }

    private fun send(order: AlleeOrder) {
        if (!cantSend()) { return }

        AlleeSDK.shared.sendOrder(order, callback(order.id))
    }

    fun sendOrder1() {
        val order = order(listOf(
            item("Veggie Burger", 1, listOf(
                condiment("Lettuce"),
                condiment("Tomatoes"),
                condiment("Pickles"),
                condiment("Hot Pepper Relish")
            ), summary = AlleeSummary("Soy burger", 2), recipe = recipe()),

            item("French Fries", 2, listOf(
                condiment("Mayonnaise")
            )),

            item("Coca-Cola 350ml", 2)
            )
        )

        val customer = AlleeCustomer()
        customer.id = UUID.randomUUID().toString()

        customer.name = "Robert"
        customer.phone = "1111111111"
        customer.phone2 = "222222222"
        customer.address = "USA"
        customer.address2 = "North America"
        customer.city = "New York City"
        customer.country = "NY"
        customer.email = "contact@bematechus.com"
        customer.webmail = "bematechus.com"
        customer.zip = "99999"

        order.customer = customer

        order.items[0].preparationTime = 10.0
        order.items[0].condiments[1].preparationTime = 5.0
        order.items[1].itemType = AlleeItem.ItemType.FIRE

        order.items[0].category = "Burger"

        send(order)
    }

    fun sendOrder2() {
        val order = order(listOf(
            item("Chicken Cheeseburger", 1, listOf(
                condiment("Lettuce"),
                condiment("Tomatoes"),
                condiment("Pickles")
            )),

            item("Grape Juice", 1)
        ))

        send(order)
    }

    fun sendOrder3() {
        val order = order(listOf(
            item("Veggie Burger", 1, listOf(
                condiment("Lettuce")
            ))
        ))

        send(order)
    }

    fun sendOrder4() {
        val order = order(listOf(
            item("Chicken Cheeseburger", 1, listOf(
                condiment("Lettuce"),
                condiment("Tomatoes"),
                condiment("Pickles"),
                condiment("Hot Pepper Relish")
            )),

            item("French Fries", 2, listOf(
                condiment("Mayonnaise")
            )),

            item("Coca-Cola 350ml", 2),
            item("Grilled Chicken Sandwich", 1, listOf(
                condiment("Cheddar Cheese"),
                condiment("Natual Bacon")
            )),

            item("Orange Organic Juice", 2),
            item("Caesar Salad", 2, listOf(
                condiment("Grilled Chicken")
            )),

            item("Cheese Breads", 2),
            item("Coffees With Milk", 2),
            item("Cup Cakes", 5, listOf(
                condiment("Cream Cheese"),
                condiment("Strawberries")
            )),
            item("Chicken Cheeseburger", 1, listOf(
                condiment("Lettuce"),
                condiment("Tomatoes"),
                condiment("Pickles"),
                condiment("Hot Pepper Relish")
            )),

            item("French Fries", 2, listOf(
                condiment("Mayonnaise")
            )),

            item("Coca-Cola 350ml", 2),
            item("Grilled Chicken Sandwich", 1, listOf(
                condiment("Cheddar Cheese"),
                condiment("Natual Bacon")
            )),

            item("Orange Organic Juice", 2),
            item("Caesar Salad", 2, listOf(
                condiment("Grilled Chicken")
            )),

            item("Cheese Breads", 2),
            item("Coffees With Milk", 2),
            item("Cup Cakes", 5, listOf(
                condiment("Cream Cheese"),
                condiment("Strawberries")
            )),
            item("Chicken Cheeseburger", 1, listOf(
                condiment("Lettuce"),
                condiment("Tomatoes"),
                condiment("Pickles"),
                condiment("Hot Pepper Relish")
            )),

            item("French Fries", 2, listOf(
                condiment("Mayonnaise")
            )),

            item("Coca-Cola 350ml", 2),
            item("Grilled Chicken Sandwich", 1, listOf(
                condiment("Cheddar Cheese"),
                condiment("Natual Bacon")
            )),

            item("Orange Organic Juice", 2),
            item("Caesar Salad", 2, listOf(
                condiment("Grilled Chicken")
            )),

            item("Cheese Breads", 2),
            item("Coffees With Milk", 2),
            item("Cup Cakes", 5, listOf(
                condiment("Cream Cheese"),
                condiment("Strawberries")
            ))
        ))

        send(order)
    }

    fun sendOrderXML(xml: String) {
        if (!cantSend()) { return }

        var xmlString = xml.replace(CustomOrder.Key.ORDER_ID.value, "$nextOrderId")
        xmlString = xmlString.replace(CustomOrder.Key.STATION_ID.value, etStation.text.toString())

        AlleeSDK.shared.sendOrderXML(xmlString, callback("$nextOrderId"))

        nextOrderId += 1
    }

    private fun recipe(): AlleeItemRecipe {
        val recipe = AlleeItemRecipe()
        recipe.image = "https://bit.ly/2I9dkxH"
        recipe.ingredients = mutableListOf("1/2 medium yellow onion, chopped", "3/4 c. panko bread crumbs", "1 tomato, sliced")
        recipe.steps = mutableListOf("In a food processor, pulse black beans, onion, and garlic until finely chopped.",
            "Transfer to a large bowl and combine with egg, 2 tablespoons mayo, and panko. Season generously with salt and pepper and form the mixture into 4 patties. Refrigerate until firm, about 15 minutes.",
            "In a large skillet over medium heat, heat oil. Add patties and cook until golden and warmed through, about 5 minutes per side.")

        return recipe
    }

    private fun condiment(name: String): AlleeCondiment {
        val condiment = AlleeCondiment()
        condiment.id = UUID.randomUUID().toString()
        condiment.name = name

        return condiment
    }

    private fun item(name: String, qty: Int, condiments: List<AlleeCondiment>? = null,
                     summary: AlleeSummary? = null, recipe: AlleeItemRecipe? = null): AlleeItem {

        val item = AlleeItem()
        item.id = UUID.randomUUID().toString()
        item.name = name
        item.kdsStation = etStation.text.toString()
        item.quantity = qty
        item.condiments = condiments

        item.summary = summary
        item.itemRecipe = recipe

        return item
    }

    private fun order(items: List<AlleeItem>): AlleeOrder {
        val order = AlleeOrder()
        order.id = "$nextOrderId"
        order.items = items

        nextOrderId += 1

        return order
    }

    private fun envIndexById(id: Int): Int {
        return when (id) {
            R.id.rbDev -> 2
            R.id.rbStage -> 1
            else -> 0
        }
    }

    private fun envIdByIndex(index: Int): Int {
        return when (index) {
            2 -> R.id.rbDev
            1 -> R.id.rbStage
            else -> R.id.rbProd
        }
    }

    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    fun Int.pxToDp(context: Context): Int = (this / context.resources.displayMetrics.density).toInt()
}
