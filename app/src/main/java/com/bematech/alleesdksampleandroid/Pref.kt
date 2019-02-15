package com.bematech.alleesdksampleandroid

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class Pref {

    companion object {

        private const val ENV = "ENV"

        private const val STORE_KEY = "STORE_KEY"
        private const val PORT = "PORT"
        private const val STATION = "STATION"

        private const val CUSTOM_ORDERS = "CUSTOM_ORDERS"

        private fun getPrefs(context: Context): SharedPreferences? {
            return context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
        }

        fun setEnv(env: Int, context: Context) {
            getPrefs(context)?.edit()?.putInt(ENV, env)?.apply()
        }

        fun setStoreKey(storeKey: String, context: Context) {
            getPrefs(context)?.edit()?.putString(STORE_KEY, storeKey)?.apply()
        }

        fun setPort(port: String, context: Context) {
            getPrefs(context)?.edit()?.putString(PORT, port)?.apply()
        }

        fun setStation(station: String, context: Context) {
            getPrefs(context)?.edit()?.putString(STATION, station)?.apply()
        }

        fun getEnv(context: Context): Int {
            return getPrefs(context)?.getInt(ENV, 0) ?: 0
        }

        fun getStoreKey(context: Context): String? {
            return getPrefs(context)?.getString(STORE_KEY, "")
        }

        fun getPort(context: Context): String? {
            return getPrefs(context)?.getString(PORT, "")
        }

        fun getStation(context: Context): String? {
            return getPrefs(context)?.getString(STATION, "")
        }

        fun getCustomOrders(context: Context): List<CustomOrder> {
            val customOrders = mutableListOf<CustomOrder>()

            val customOrdersJson = getPrefs(context)?.getStringSet(CUSTOM_ORDERS, emptySet())
            customOrdersJson?.forEach {
                val customOrder = Gson().fromJson(it, CustomOrder::class.java)
                customOrders.add(customOrder)
            }

            return customOrders
        }

        fun setCustomOrders(customOrders: List<CustomOrder>, context: Context) {
            val customOrdersJson = mutableSetOf<String>()
            customOrders.forEach {
                customOrdersJson.add(Gson().toJson(it))
            }

            getPrefs(context)?.edit()?.putStringSet(CUSTOM_ORDERS, customOrdersJson)?.apply()
        }
    }
}