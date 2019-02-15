package com.bematech.alleesdksampleandroid

import java.io.Serializable

class CustomOrder(var guid: String, var name: String, var xml: String, var createTime: Int) : Serializable {

    constructor(guid: String) : this(guid, "", "", 0)

    enum class Key(var value: String) {
        ORDER_ID("[ORDER_ID]"),
        STATION_ID("[STATION_ID]")
    }

}