/**
 * SETUP:
 * 1. Open the Google Sheet where your Form responses currently go
 *    (Form > Responses tab > green Sheets icon, if you don't already
 *    have it open).
 * 2. Extensions > Apps Script.
 * 3. Delete any starter code and paste this whole file in.
 * 4. Update SHEET_NAME below if your response tab is not named "App Responses".
 * 5. Click Deploy > New deployment > select type "Web app".
 *      - Execute as: Me
 *      - Who has access: Anyone
 * 6. Copy the Web App URL it gives you (ends in /exec) and paste it into
 *    Constants.APPS_SCRIPT_URL in the Android app.
 * 7. (Optional) Set SHARED_SECRET below to a random string and put the
 *    same value in Constants.SHARED_SECRET in the app, so random people
 *    who find your URL can't write junk rows into your sheet.
 */

var SHEET_NAME = "App Responses"; // name of the sheet/tab to write into
var SHARED_SECRET = "";           // leave blank to disable the check

// Column order written to the sheet. Keys must match the JSON keys
// sent by the Android app (see FormModel.kt "key" values).
var COLUMNS = [
  "Date",
  "Salesman",
  "Van No",
  "Chassis No",
  "Customer Name",
  "Village",
  "HMR",
  "Phone No",
  "Warranty Type",
  "Service",
  "Smile",
  "Collant Change (Litres)",
  "Parts Sale",
  "Lube Sale",
  "Air Filter",
  "Regular Work At Dealership",
  "Outside Work Due To",
  "Major Job Work Suggested",
  "Converted If Discount Given",
  "Implement Change Requires",
  "Enquiry",
  "Enquiry Name",
  "Village Enquiry",
  "Model Require",
  "Enquiry Status",
  "Delivery Date Estimate",
  "Enquiry Phone No",
  "Remarks"
];

function doPost(e) {
  try {
    var data = JSON.parse(e.postData.contents);

    if (SHARED_SECRET && data.secret !== SHARED_SECRET) {
      return jsonResponse({ ok: false, error: "Unauthorized" });
    }

    var sheet = getOrCreateSheet();

    var row = COLUMNS.map(function (col) {
      return data[col] !== undefined ? data[col] : "";
    });
    // Add a server-side timestamp as the first column for reference.
    row.unshift(new Date());

    sheet.appendRow(row);

    return jsonResponse({ ok: true });
  } catch (err) {
    return jsonResponse({ ok: false, error: err.toString() });
  }
}

function getOrCreateSheet() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) {
    sheet = ss.insertSheet(SHEET_NAME);
    sheet.appendRow(["Timestamp"].concat(COLUMNS));
  }
  return sheet;
}

function jsonResponse(obj) {
  return ContentService
    .createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}
