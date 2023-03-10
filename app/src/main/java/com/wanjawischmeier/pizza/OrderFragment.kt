package com.wanjawischmeier.pizza

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import com.google.android.gms.tasks.Task
import kotlin.math.round


const val GROUP_ID = "prenski_12"
const val SHOP_ID = "penny_burgtor"

class OrderFragment : CallableFragment() {
    private var orderChildren = hashMapOf<View, String>()
    private lateinit var order: HashMap<String, Long>
    private lateinit var priceView: TextView
    private lateinit var main: MainActivity
    private var total = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        priceView = topBubble.findViewById(R.id.top_bubble_text)
        main = activity as MainActivity
        return inflater.inflate(R.layout.fragment_order, container, false)
    }

    @Nullable
    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        topBubbleVisible = true
        bottomLayoutVisible = false

        User.getUser(GROUP_ID, main.userId).continueWith {
            order = it.result.orders[SHOP_ID] ?: hashMapOf()
            inflateItems(view)
        }
    }

    @SuppressLint("InflateParams")
    private fun inflateItems(view: View) {
        val itemsList = view.findViewById<LinearLayout>(R.id.items_list)

        val items = main.shop.items
        for ((itemId, item) in items) {

            // Avoid parralel inflations
            activity?.runOnUiThread {
                val inflated = layoutInflater.inflate(R.layout.card_order, null)

                inflated.findViewById<TextView>(R.id.order_name).text = item.name
                inflated.findViewById<TextView>(R.id.order_price).text = getString(R.string.price_format).format(item.price)

                orderChildren += inflated to itemId
                itemsList.addView(inflated)
            }
        }
    }

    override fun onShow(): Task<Unit> {
        topBubbleVisible = true
        bottomLayoutVisible = false

        return User.getUser(GROUP_ID, main.userId).continueWith {
            order = it.result.orders[SHOP_ID] ?: hashMapOf()
            total = 0f

            for ((view, itemId) in orderChildren) {
                val price = main.shop.items[itemId]?.price ?: 0f
                val count = order[itemId] ?: 0L
                view.findViewById<TextView>(R.id.order_count).text = count.toString()
                total = round((total + price * count) * 100) / 100
            }

            priceView.text = getString(R.string.price_format).format(total)
        }
    }

    override fun onHide() {
        // TODO: Not yet implemented
    }

    fun modifyCount(view: View, change: Long) {
        bottomLayoutVisible = true

        val countView = (view.parent as ViewGroup).findViewById<TextView>(R.id.order_count)
        val count = countView.text.toString().toLong() + change
        if (count < 0 || count > 99) return
        countView.text = count.toString()

        val itemId = orderChildren[view.parent.parent as View] ?: return
        val item = main.shop.items[itemId] ?: return
        total = round((total + item.price * change) * 100) / 100
        priceView.text = getString(R.string.price_format).format(total)

        order[itemId] = count
    }

    fun placeOrder() {
        main.swipeRefreshLayout.isRefreshing = true

        order = order.filterValues { it != 0L } as HashMap<String, Long>
        Shop.processOrder(GROUP_ID, main.userId, SHOP_ID, order).continueWith {
            main.swipeRefreshLayout.isRefreshing = false
            bottomLayoutVisible = false
        }
    }
}