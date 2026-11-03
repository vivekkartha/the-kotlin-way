# SMS API Lab (Android)

A small native Kotlin app for testing Android's `SmsManager.createForSubscriptionId(subId)` with two SIMs.

## Use
1. Open the [GitHub Actions page](https://github.com/vivekkartha/the-kotlin-way/actions/workflows/build-apk.yml) and select the latest **SMS API Lab APK** run.
2. Download the `sms-api-lab-debug-apk` artifact ZIP, unzip it, and install `app-debug.apk` on an Android 12+ physical phone.
3. Grant **SMS** and **Phone** permissions.
4. Enter a phone number you control and a short SMS message. Tap **Send SMS via SIM 1** and then **Send SMS via SIM 2**.
5. Compare the result callbacks in the app and actual thread behavior in Google Messages or your default Messaging app.

## Notes
- This APK is a debug-only sideloaded utility, not a Play Store app.
- Each send uses the Android 12+ `context.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)` API.
- Android SMS permission may be restricted by device policy or security controls.
- **A successful send does not prove whether the Messages app reuses a conversation thread.** That UI behavior depends on the default Messaging app and device.
- The app does not collect or upload numbers, messages, or logs.
- SMS sends can incur carrier fees. Only send to numbers you control.
- The `master` branch is unchanged; this test app lives only on `android-sms-playground`.

## Build
The `.github/workflows/build-apk.yml` workflow installs Android SDK + Gradle in GitHub Actions, builds `app-debug.apk`, and publishes it as a downloadable artifact for 7 days.

To rebuild: edit Kotlin code and push to `android-sms-playground`. The workflow starts automatically.
