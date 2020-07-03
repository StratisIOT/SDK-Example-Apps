package com.example.sdkexample

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

open class SettingsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    internal var accessToken: String? = null
    internal var propertyID: String? = null
    private var serverEnvironment: String? = null

    private var accessTokenTextWatcher: SettingsActivityTextWatcher? = null
    private var propertyIDTextWatcher: SettingsActivityTextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        accessToken = intent.getStringExtra(ACCESS_TOKEN)
        propertyID = intent.getStringExtra(PROPERTY_ID)

        val accessTokenInput = findViewById<EditText>(R.id.access_token_input)
        accessTokenTextWatcher = SettingsActivityTextWatcher(accessTokenInput)
        accessTokenInput.addTextChangedListener(accessTokenTextWatcher)
        accessTokenInput.setText(accessToken)

        val propertyIDInput = findViewById<EditText>(R.id.property_id_input)
        propertyIDTextWatcher = SettingsActivityTextWatcher(propertyIDInput)
        propertyIDInput.addTextChangedListener(propertyIDTextWatcher)
        propertyIDInput.setText(propertyID)

        val spinner: Spinner = findViewById(R.id.server_environment_spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.server_environments,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = this
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("accessToken", accessTokenTextWatcher?.accessToken)
        intent.putExtra("propertyID", propertyIDTextWatcher?.propertyID)
        intent.putExtra("serverEnvironment", serverEnvironment)
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position != 0) {
            val item = parent?.getItemAtPosition(position)
            serverEnvironment = item.toString()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
}

private class SettingsActivityTextWatcher(private val view: View) : TextWatcher, SettingsActivity() {
    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun afterTextChanged(editable: Editable) {
        val text = editable.toString()
        when (view.id) {
            R.id.access_token_input -> accessToken = text
            R.id.property_id_input -> propertyID = text
        }
    }
}
