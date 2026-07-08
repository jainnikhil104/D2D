package com.example.salesform

/** Types of fields we render. Maps closely to the Google Form question types. */
enum class FieldType {
    DATE,
    DROPDOWN,
    TEXT,
    NUMBER,
    PHONE,
    RADIO,          // single choice, required
    RADIO_OPTIONAL, // single choice, clearable / optional
    CHECKBOX        // multiple choice, optional, supports "Other"
}

data class FormField(
    val key: String,           // column header used in the Google Sheet
    val label: String,         // question text shown to the user
    val type: FieldType,
    val required: Boolean = false,
    val options: List<String> = emptyList(),
    val hasOther: Boolean = false,
    val helperText: String? = null
)

/**
 * Page 1 of the Google Form (everything visible before the "Next" button).
 * If your form has more pages, add more fields here the same way.
 */
object SalesForm {
    val fields = listOf(
        FormField(
            key = "Date",
            label = "DATE",
            type = FieldType.DATE,
            required = true
        ),
        FormField(
            key = "Salesman",
            label = "Salesman",
            type = FieldType.DROPDOWN,
            required = true,
            options = listOf(
                "KAPIL", "ARUN", "MUNA", "GANAUR", "NARENDER", "NARESH",
                "BIJENDER", "RAJU", "AMIT ANTIL", "SONU", "JUGMENDER",
                "AMIT", "AJIT"
            )
        ),
        FormField(
            key = "Van No",
            label = "VAN NO",
            type = FieldType.DROPDOWN,
            required = true,
            options = listOf("1", "2", "3", "4", "5", "6", "7")
        ),
        FormField(
            key = "Chassis No",
            label = "CHASSIS NO",
            type = FieldType.TEXT,
            required = true
        ),
        FormField(
            key = "Customer Name",
            label = "CUSTOMER NAME",
            type = FieldType.TEXT
        ),
        FormField(
            key = "Village",
            label = "VILLAGE",
            type = FieldType.TEXT
        ),
        FormField(
            key = "HMR",
            label = "HMR",
            type = FieldType.NUMBER
        ),
        FormField(
            key = "Phone No",
            label = "PHONE NO",
            type = FieldType.PHONE,
            required = true
        ),
        FormField(
            key = "Warranty Type",
            label = "WARRANTY TYPE",
            type = FieldType.RADIO,
            required = true,
            options = listOf("1", "0"),
            helperText = "0 FOR WARRANTY TR & 1 FOR OUT OF WARRANTY TR"
        ),
        FormField(
            key = "Service",
            label = "SERVICE",
            type = FieldType.RADIO,
            required = true,
            options = listOf("1", "0"),
            helperText = "1 FOR SERVICE & 0 FOR NON SERVICE"
        ),
        FormField(
            key = "Smile",
            label = "SMILE",
            type = FieldType.RADIO,
            required = true,
            options = listOf("1", "0"),
            helperText = "1 FOR SMILE & 0 FOR NON SMILE"
        ),
        FormField(
            key = "Collant Change (Litres)",
            label = "COLLANT CHANGE",
            type = FieldType.NUMBER,
            helperText = "Collant in litres"
        ),
        FormField(
            key = "Parts Sale",
            label = "PARTS SALE",
            type = FieldType.NUMBER
        ),
        FormField(
            key = "Lube Sale",
            label = "LUBE SALE",
            type = FieldType.NUMBER
        ),
        FormField(
            key = "Air Filter",
            label = "AIR FILTER",
            type = FieldType.RADIO_OPTIONAL,
            options = listOf("0", "1", "2"),
            helperText = "0 FOR NON FILTER & 1 FOR FILTER CHANGE"
        ),
        FormField(
            key = "Regular Work At Dealership",
            label = "Regular Work At Dealership",
            type = FieldType.RADIO_OPTIONAL,
            options = listOf("Yes", "No")
        ),
        FormField(
            key = "Outside Work Due To",
            label = "Outside Work Due To",
            type = FieldType.CHECKBOX,
            options = listOf("Price", "Mechanics Shortage", "Parts Shortage", "Distance"),
            hasOther = true
        ),
        FormField(
            key = "Major Job Work Suggested",
            label = "Major Job Work Suggested",
            type = FieldType.CHECKBOX,
            options = listOf("ENGINE", "CLUTCH", "BRAKES", "GEAR BOX", "TRANSMISSION OIL"),
            hasOther = true
        ),
        FormField(
            key = "Converted If Discount Given",
            label = "Converted If Discount Given",
            type = FieldType.RADIO_OPTIONAL,
            options = listOf("Yes", "No")
        ),
        FormField(
            key = "Implement Change Requires",
            label = "IMPLEMENT CHANGE REQUIRES",
            type = FieldType.CHECKBOX,
            options = listOf("ROTAVATOR", "SUPERSEEDER", "BALLER")
        ),
        FormField(
            key = "Enquiry",
            label = "ENQUIRY",
            type = FieldType.RADIO,
            required = true,
            options = listOf("YES", "NO")
        ),
        FormField(
            key = "Enquiry Name",
            label = "ENQUIRY NAME",
            type = FieldType.TEXT
        ),
        FormField(
            key = "Village Enquiry",
            label = "VILLAGE ENQUIRY",
            type = FieldType.TEXT
        ),
        FormField(
            key = "Model Require",
            label = "MODEL REQUIRE",
            type = FieldType.TEXT
        ),
        FormField(
            key = "Enquiry Status",
            label = "Enquiry Status",
            type = FieldType.RADIO_OPTIONAL,
            options = listOf("1", "2", "3", "4", "5")
        ),
        FormField(
            key = "Delivery Date Estimate",
            label = "DELIVERY DATE ESTIMATE",
            type = FieldType.DATE
        ),
        FormField(
            key = "Enquiry Phone No",
            label = "ENQUIRY PHONE NO",
            type = FieldType.PHONE
        ),
        FormField(
            key = "Remarks",
            label = "REMARKS",
            type = FieldType.TEXT
        )
    )
}
