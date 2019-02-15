package com.bematech.alleesdksampleandroid

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bematech.alleesdkcore.Models.AlleeBumpStatus
import com.bematech.alleesdkcore.Models.AlleeOrderBumpStatus
import kotlinx.android.synthetic.main.order_status_view_item.view.*

class OrderStatusAdapter(var context: Context) : RecyclerView.Adapter<OrderStatusAdapter.OrderHolder>() {

    private var orderIds: MutableList<String> = mutableListOf()
    private var ordersStatus: MutableList<AlleeOrderBumpStatus> = mutableListOf()

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): OrderHolder {
        return OrderHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.order_status_view_item, viewGroup, false))
    }

    override fun getItemCount(): Int {
        return orderIds.count()
    }

    override fun onBindViewHolder(orderHolder: OrderHolder, position: Int) {
        val orderId = orderIds[position]
        orderHolder.tvTitle.text = "#$orderId"

        val status = ordersStatus.firstOrNull { it.id == orderId }
        if (status != null) {
            orderHolder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))

            when (status.status) {
                AlleeBumpStatus.NEW -> orderHolder.tvStatus.text = context.getString(R.string.order_status_new)
                AlleeBumpStatus.PREPARED -> orderHolder.tvStatus.text = context.getString(R.string.order_status_prepared)
                else -> orderHolder.tvStatus.text = context.getString(R.string.order_status_done)
            }

        } else {
            orderHolder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
            orderHolder.tvStatus.text = context.getString(R.string.order_status_unknown)
        }
    }

    fun addOrderId(orderId: String) {
        orderIds.add(orderId)
        notifyItemInserted(orderIds.size - 1)
    }

    fun updateStatus(ordersStatus: List<AlleeOrderBumpStatus>) {
        ordersStatus.forEach {
            val orderStatus = it
            val index = this.ordersStatus.indexOfFirst { it.guid == orderStatus.guid }
            if (index >= 0) {
                this.ordersStatus[index] = orderStatus

            } else {
                this.ordersStatus.add(orderStatus)
            }
        }

        notifyDataSetChanged()
    }

    class OrderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvTitle: TextView = itemView.tvTitle
        var tvStatus: TextView = itemView.tvStatus
    }
}