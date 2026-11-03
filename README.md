# SMS API Lab (Android)

A small native Kotlin app for testing `SmsManager.createForSubscriptionId(subId)` with two SIMs.

## Download APK

**[Download SMS-API-Lab.apk](https://github.com/vivekkartha/the-kotlin-way/raw/refs/heads/android-sms-playground/SMS-API-Lab.apk)**

The signed debug APK is maintained at **`SMS-API-Lab.apk` in the root of the `android-sms-playground` branch**. No Gmail attachment, ZIP extraction, or GitHub Actions artifact page needed.

## Use

1. Download the APK above and install it on an Android 12+ physical phone.
2. Grant SMS and Phone permissions.
3. Enter a phone number you control and a short SMS message.
4. Tap **Send SMS via SIM 1**, then **Send SMS via SIM 2**.
5. Compare result callbacks here with actual thread behavior in Google Messages or your default Messaging app.

## Notes

- Debug-only sideloaded utility, not a Play Store app.
- Each send uses `createForSubscriptionId(subId)` to select its outbound SIM.
- Android SMS permission may be restricted by security controls or device policy.
- **Sending from a selected subscription does not guarantee how a messaging app groups conversations.** Check that on-device.
- No numbers, SMS contents, or logs are uploaded by this app.
- Carriers may charge for SMS. Use a recipient number you control.
- The `master` branch is unchanged; this app is on `android-sms-playground`.

## Rebuild

Edit code on `android-sms-playground` and push. GitHub Actions builds the APK and commits the resulting `SMS-API-Lab.apk` back to the branch root automatically. The Actions run also stores a downloadable build artifact for 7 days. APK-only commits do not retrigger the build.
