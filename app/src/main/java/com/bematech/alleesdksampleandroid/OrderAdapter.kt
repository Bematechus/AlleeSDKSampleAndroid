package com.bematech.alleesdksampleandroid

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.custom_order_view_item.view.*

class OrderAdapter(var context: MainActivity, var items: List<CustomOrder?>) : RecyclerView.Adapter<OrderAdapter.OrderHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): OrderHolder {
        return OrderHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.custom_order_view_item, viewGroup, false))
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(orderHolder: OrderHolder, position: Int) {
        val item = items[position]
        if (item != null) {
            orderHolder.tvTitle.text = item.name

            orderHolder.tvTitle.setOnLongClickListener {
                val intent = Intent(context, FormActivity::class.java)
                intent.putExtra(Intent.EXTRA_INTENT, items[position])
                context.startActivityForResult(intent, 0)

                true
            }

        } else {
            orderHolder.tvTitle.text = context.getString(R.string.default_order, position + 1)
        }

        orderHolder.tvTitle.setOnClickListener {
            when (position) {
                0 -> context.sendOrder1()
                1 -> context.sendOrder2()
                2 -> context.sendOrder3()
                3 -> context.sendOrder4()
                else -> context.sendOrderXML(item?.xml ?: "")
            }
        }
    }

    class OrderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvTitle: TextView = itemView.title
    }
}