# Sales Report Form — Android App

A native Android app that reproduces your Google Form and writes each submission
as a new row directly into your Google Sheet (no internet form-filling required —
this uses a small Google Apps Script "Web App" as the bridge).

This app covers the full form, top to bottom (Date through Remarks). No file
upload question exists on this form, so the app is purely text/choice-based —
no Google Sign-In needed.

## How it works

```
Android App  --(HTTPS POST JSON)-->  Apps Script Web App  --(appendRow)-->  Google Sheet
```

This is the standard, supported way to write into a Sheet from a mobile app.
It avoids embedding Google service-account credentials in the APK and avoids
relying on Google Forms' undocumented submission endpoint (which is fragile
and can break without notice).

## Step 1 — Attach a response sheet to your form (if not already)

In the Google Form editor: **Responses tab → green Sheets icon → Create/Select
spreadsheet**. This is the spreadsheet the app will write into.

## Step 2 — Deploy the Apps Script Web App

1. Open that Google Sheet.
2. **Extensions → Apps Script**.
3. Delete the placeholder code, then paste in the contents of
   `AppsScript/Code.gs` (included in this project).
4. Edit `SHEET_NAME` at the top if you want the app to write into a tab
   other than "App Responses" (it will create the tab automatically if it
   doesn't exist, so the default is fine to leave as-is).
5. Optionally set `SHARED_SECRET` to a random string, e.g. `"a1b2c3"`, so
   nobody else can submit to your sheet even if they find the URL.
6. **Deploy → New deployment**:
   - Type: **Web app**
   - Execute as: **Me**
   - Who has access: **Anyone**
7. Click **Deploy**, authorize the permissions it asks for, then copy the
   URL shown (it ends in `/exec`).

## Step 3 — Configure the Android app

Open `app/src/main/java/com/example/salesform/Constants.kt` and paste your
URL:

```kotlin
const val APPS_SCRIPT_URL = "https://script.google.com/macros/s/AKfycb..../exec"
const val SHARED_SECRET = "a1b2c3" // only if you set one in Step 2.5
```

## Building it entirely in the browser (no Android Studio install)

Firebase Studio currently has new signups/workspaces disabled (it's being
sunset in 2027), so the most reliable no-install option right now is
**GitHub Actions** — GitHub's own servers build the APK for you.

1. Create a new repository on [github.com](https://github.com) (any name,
   e.g. `sales-form-app`) and upload this whole `SalesFormApp` folder into
   it (drag-and-drop works fine via the "Add file → Upload files" button,
   or `git push` if you're comfortable with git). Make sure
   `Constants.kt` already has your Apps Script URL pasted in before
   uploading (Step 3 above).
2. A workflow file is already included at
   `.github/workflows/build-apk.yml` — GitHub detects it automatically and
   starts a build as soon as you push/upload to the `main` branch. You can
   also trigger it manually from the **Actions** tab → **Build APK** →
   **Run workflow**.
3. Wait for the run to finish (usually 2–4 minutes) — you'll see a green
   checkmark next to the commit.
4. Click into that run, scroll to **Artifacts**, and download
   `SalesFormApp-debug-apk`. It's a zip containing `app-debug.apk` — unzip
   it, transfer the APK to an Android phone, and tap it to install (you'll
   need to allow "install from unknown sources" once).

This produces a debug-signed APK, which is fine for internal use on your
team's own devices but not for the Play Store — that's a separate signing
step if you ever need it.

## Step 4 — Build and run

1. Open the `SalesFormApp` folder in **Android Studio** (Giraffe or newer).
2. Let Gradle sync (it will download dependencies — needs internet access
   to `google()`/`mavenCentral()`, which this sandbox doesn't have, so the
   first build must happen on your machine, not here).
3. Connect a device or start an emulator, then press **Run**.

## What's included

- `app/` — the Android Studio project (Kotlin, no Compose, minSdk 21).
  - `FormModel.kt` — declarative list of every question, its type, and
    whether it's required. Add/edit fields here if the form changes.
  - `MainActivity.kt` — builds the UI from that list, validates required
    fields, and POSTs a JSON object of the answers to the Apps Script URL.
  - `Constants.kt` — where you paste your deployment URL.
- `AppsScript/Code.gs` — paste into the Sheet's Apps Script editor. Receives
  the JSON POST and appends a row.

## Notes & possible follow-ups

- **Offline handling**: right now, if there's no signal, submission just
  fails with a toast and the user can retry. If your reps often work
  without signal, I can add local queuing (save to the device, auto-submit
  when back online) — let me know.
- **Launcher icon**: the project doesn't include a custom app icon yet;
  Android Studio's default is used. Easy to add via
  *File → New → Image Asset* if you want your own logo.
- **Editing dropdown lists** (e.g. adding a new salesman): just edit the
  `options` list for that field in `FormModel.kt`.
