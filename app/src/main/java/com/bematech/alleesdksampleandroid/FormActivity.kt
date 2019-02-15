package com.bematech.alleesdksampleandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager

import kotlinx.android.synthetic.main.activity_form.*
import java.util.*

class FormActivity : AppCompatActivity() {

    private var customOrder: CustomOrder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        setSupportActionBar(toolbar)

        customOrder = intent.getSerializableExtra(Intent.EXTRA_INTENT) as CustomOrder?

        toolbar.title = getString(if (customOrder == null) R.string.activity_form_add else R.string.activity_form_edit)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etXML.hint = getString(R.string.order_xml, CustomOrder.Key.ORDER_ID.value, CustomOrder.Key.STATION_ID.value)

        customOrder.let {
            etName.setText(it?.name)
            etXML.setText(it?.xml)
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_form, menu)
        if (customOrder == null) {
            menu?.findItem(R.id.action_delete)?.isVisible = false
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_delete) {
            AlertDialog.Builder(this).setTitle(getString(R.string.delete_order_title))
                .setMessage(getString(R.string.delete_order_msg, etName.text))
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_delete) { _, _ -> deleteCustomOrder() }
                .show()
                .getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.red))

        } else if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun deleteCustomOrder() {
        val customOrders = Pref.getCustomOrders(this).toMutableList()
        val index = customOrders.indexOfFirst { it.guid == customOrder?.guid }
        if (index >= 0) {
            customOrders.removeAt(index)
        }

        Pref.setCustomOrders(customOrders, this)
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun actionSave(button: View) {
        if (etName.text?.isEmpty() != false) {
            tiName.error = getString(R.string.form_fill_name)
            return
        }

        val customOrders = Pref.getCustomOrders(this).toMutableList()

        val customOrder = customOrder ?: CustomOrder(UUID.randomUUID().toString())
        customOrder.name = etName.text.toString()
        customOrder.xml = etXML.text.toString()
        customOrder.createTime = Date().time.toInt()

        if (this.customOrder == null) {
            customOrders.add(customOrder)

        } else {
            val index = customOrders.indexOfFirst { it.guid == customOrder.guid }
            if (index >= 0) {
                customOrders[index] = customOrder

            } else {
                customOrders.add(customOrder)
            }
        }

        Pref.setCustomOrders(customOrders, this)
        setResult(Activity.RESULT_OK)
        finish()
    }
}
