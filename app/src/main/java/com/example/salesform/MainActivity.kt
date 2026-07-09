package com.example.salesform

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Holds the live UI widgets for one FormField so we can read the answer
 * back out and validate it on submit.
 */
private class FieldBinding(
    val field: FormField,
    val readValue: () -> String,
    val isFilled: () -> Boolean,
    val markError: (Boolean) -> Unit = {}
)

class MainActivity : AppCompatActivity() {

    private val bindings = mutableListOf<FieldBinding>()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val container = findViewById<LinearLayout>(R.id.formContainer)
        buildForm(container)
        addSubmitButton(container)
    }

    // ---------- UI construction ----------

    private fun buildForm(container: LinearLayout) {
        for (field in SalesForm.fields) {
            addLabel(container, field)
            field.helperText?.let { addHelperText(container, it) }

            when (field.type) {
                FieldType.DATE -> addDateField(container, field)
                FieldType.DROPDOWN -> addDropdown(container, field)
                FieldType.TEXT -> addTextField(container, field, InputType.TYPE_CLASS_TEXT)
                FieldType.NUMBER -> addTextField(
                    container, field,
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                )
                FieldType.PHONE -> addTextField(container, field, InputType.TYPE_CLASS_PHONE)
                FieldType.RADIO -> addRadioGroup(container, field, clearable = false)
                FieldType.RADIO_OPTIONAL -> addRadioGroup(container, field, clearable = true)
                FieldType.CHECKBOX -> addCheckboxGroup(container, field)
            }
            addSpacer(container, 20)
        }
    }

    private fun addLabel(container: LinearLayout, field: FormField) {
        val tv = TextView(this)
        val text = if (field.required) "${field.label} *" else field.label
        tv.text = text
        tv.textSize = 16f
        tv.setTypeface(tv.typeface, android.graphics.Typeface.BOLD)
        tv.setTextColor(Color.parseColor("#212121"))
        container.addView(tv)
    }

    private fun addHelperText(container: LinearLayout, text: String) {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 12f
        tv.setTextColor(Color.parseColor("#757575"))
        container.addView(tv)
    }

    private fun addSpacer(container: LinearLayout, heightDp: Int) {
        val space = View(this)
        val density = resources.displayMetrics.density
        space.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, (heightDp * density).toInt()
        )
        container.addView(space)
    }

    private fun styledCard(container: LinearLayout): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = 8
        card.layoutParams = lp
        card.setBackgroundColor(Color.WHITE)
        val pad = (8 * resources.displayMetrics.density).toInt()
        card.setPadding(pad, pad, pad, pad)
        container.addView(card)
        return card
    }

    private fun addDateField(container: LinearLayout, field: FormField) {
        val card = styledCard(container)
        val editText = EditText(this)
        editText.isFocusable = false
        editText.isClickable = true
        editText.hint = "Tap to choose a date"
        card.addView(editText)

        editText.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    editText.setText(String.format("%02d/%02d/%04d", month + 1, day, year))
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        bindings.add(
            FieldBinding(
                field = field,
                readValue = { editText.text.toString().trim() },
                isFilled = { editText.text.toString().isNotBlank() },
                markError = { hasError ->
                    editText.error = if (hasError) "Required" else null
                }
            )
        )
    }

    private fun addDropdown(container: LinearLayout, field: FormField) {
        val card = styledCard(container)
        val spinner = Spinner(this)
        val choose = "Choose"
        val items = listOf(choose) + field.options
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter
        card.addView(spinner)

        bindings.add(
            FieldBinding(
                field = field,
                readValue = {
                    val pos = spinner.selectedItemPosition
                    if (pos <= 0) "" else field.options[pos - 1]
                },
                isFilled = { spinner.selectedItemPosition > 0 }
            )
        )
    }

    private fun addTextField(container: LinearLayout, field: FormField, inputType: Int) {
        val card = styledCard(container)
        val editText = EditText(this)
        editText.inputType = inputType
        editText.hint = "Your answer"
        card.addView(editText)

        bindings.add(
            FieldBinding(
                field = field,
                readValue = { editText.text.toString().trim() },
                isFilled = { editText.text.toString().isNotBlank() },
                markError = { hasError ->
                    editText.error = if (hasError) "Required" else null
                }
            )
        )
    }

    private fun addRadioGroup(container: LinearLayout, field: FormField, clearable: Boolean) {
        val card = styledCard(container)
        val group = RadioGroup(this)
        group.orientation = LinearLayout.VERTICAL

        val buttons = field.options.map { option ->
            val rb = RadioButton(this)
            rb.text = option
            rb.id = View.generateViewId()
            group.addView(rb)
            rb
        }
        card.addView(group)

        if (clearable) {
            val clear = TextView(this)
            clear.text = "Clear selection"
            clear.setTextColor(Color.parseColor("#673AB7"))
            clear.setPadding(0, 8, 0, 0)
            clear.setOnClickListener { group.clearCheck() }
            card.addView(clear)
        }

        bindings.add(
            FieldBinding(
                field = field,
                readValue = {
                    val checkedId = group.checkedRadioButtonId
                    val checked = buttons.find { it.id == checkedId }
                    checked?.text?.toString() ?: ""
                },
                isFilled = { group.checkedRadioButtonId != -1 },
                markError = { hasError ->
                    card.setBackgroundColor(if (hasError) Color.parseColor("#FFEBEE") else Color.WHITE)
                }
            )
        )
    }

    private fun addCheckboxGroup(container: LinearLayout, field: FormField) {
        val card = styledCard(container)
        val boxes = mutableListOf<CheckBox>()

        field.options.forEach { option ->
            val cb = CheckBox(this)
            cb.text = option
            card.addView(cb)
            boxes.add(cb)
        }

        var otherCheckbox: CheckBox? = null
        var otherText: EditText? = null
        if (field.hasOther) {
            val otherRow = LinearLayout(this)
            otherRow.orientation = LinearLayout.HORIZONTAL
            otherRow.gravity = Gravity.CENTER_VERTICAL

            val cb = CheckBox(this)
            cb.text = "Other:"
            otherRow.addView(cb)

            val et = EditText(this)
            et.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            otherRow.addView(et)

            card.addView(otherRow)
            otherCheckbox = cb
            otherText = et
        }

        bindings.add(
            FieldBinding(
                field = field,
                readValue = {
                    val selected = boxes.filter { it.isChecked }.map { it.text.toString() }.toMutableList()
                    if (otherCheckbox?.isChecked == true) {
                        val custom = otherText?.text?.toString()?.trim().orEmpty()
                        selected.add(if (custom.isNotEmpty()) custom else "Other")
                    }
                    selected.joinToString(", ")
                },
                isFilled = {
                    boxes.any { it.isChecked } || otherCheckbox?.isChecked == true
                }
            )
        )
    }

    private fun addSubmitButton(container: LinearLayout) {
        val button = Button(this)
        button.text = "Submit"
        button.setBackgroundColor(Color.parseColor("#673AB7"))
        button.setTextColor(Color.WHITE)
        container.addView(button)
        addSpacer(container, 32)

        button.setOnClickListener { onSubmit() }
    }

    // ---------- Submission ----------

    private fun onSubmit() {
        var firstError: View? = null
        var valid = true

        for (binding in bindings) {
            val required = binding.field.required
            val filled = binding.isFilled()
            if (required && !filled) {
                valid = false
                binding.markError(true)
            } else {
                binding.markError(false)
            }
        }

        if (!valid) {
            Toast.makeText(this, "Please fill all required (*) fields", Toast.LENGTH_LONG).show()
            return
        }

        if (Constants.APPS_SCRIPT_URL.contains("PASTE_YOUR")) {
            Toast.makeText(
                this,
                "Set Constants.APPS_SCRIPT_URL first (see README.md)",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val json = JSONObject()
        for (binding in bindings) {
            json.put(binding.field.key, binding.readValue())
        }
        if (Constants.SHARED_SECRET.isNotBlank()) {
            json.put("secret", Constants.SHARED_SECRET)
        }

        submitToSheet(json)
    }

    private fun submitToSheet(json: JSONObject) {
        val dialog = ProgressDialog(this)
        dialog.setMessage("Submitting...")
        dialog.setCancelable(false)
        dialog.show()

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) { postJson(json) }
            dialog.dismiss()
            if (result.isSuccess) {
                Toast.makeText(this@MainActivity, "Submitted successfully", Toast.LENGTH_LONG).show()
                recreate() // reset the form for the next entry
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Submit failed: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun postJson(json: JSONObject): Result<Unit> {
        return try {
            val mediaType = "application/json; charset=utf-8".toMediaType()

            fun buildRequest(url: String) =
                Request.Builder().url(url).post(json.toString().toRequestBody(mediaType)).build()

            var response = client.newCall(buildRequest(Constants.APPS_SCRIPT_URL)).execute()

            // Apps Script replies to POST with a 302 redirect to a
            // script.googleusercontent.com URL. OkHttp's normal redirect
            // handling would downgrade this to a GET and drop our JSON
            // body, so we follow it manually here, re-POSTing the same
            // body to the new URL.
            var redirects = 0
            while (response.isRedirect && redirects < 5) {
                val location = response.header("Location")
                response.close()
                if (location == null) break
                response = client.newCall(buildRequest(location)).execute()
                redirects++
            }

            response.use { res ->
                val text = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    return Result.failure(IOException("Server returned ${res.code}"))
                }
                val resultJson = try {
                    JSONObject(text)
                } catch (e: Exception) {
                    null
                }
                if (resultJson != null && !resultJson.optBoolean("ok", true)) {
                    return Result.failure(IOException(resultJson.optString("error", "Unknown error")))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
