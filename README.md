# MSPS Android application

This is the complete GitHub repository package for building the **MSPS** Android app.

- `index.html` at the repository root is included so it is visible in GitHub.
- `app/src/main/assets/index.html` is the identical copy packaged inside the Android app. It is unchanged from the supplied website file.
- The Android WebView wrapper includes a native file picker for the existing JSON backup restore control, plus the existing print and backup-save JavaScript bridges.
- The launcher name shown on the Android home screen is **MSPS**.

## Upload this project to a new GitHub repository

1. Download and extract this ZIP.
2. Open your new, empty GitHub repository.
3. Upload **all files and folders inside** the extracted `MSPS-Android-GitHub` folder. Upload the contents, not an extra enclosing folder. Keep `.github/workflows/build-android.yml` in that exact path.
4. Commit the upload to the repository's `main` branch (or `master`).

## Build and download the APK

1. Open the repository's **Actions** tab.
2. Select **Build MSPS Android APK** in the workflow list.
3. If it has not started automatically after your upload, press **Run workflow** and confirm.
4. Open the latest run. Wait until it shows a green check mark.
5. On the successful run page, scroll to **Artifacts** and download **MSPS-Android-APK**.
6. Extract the downloaded artifact ZIP. It contains `app-debug.apk`.
7. Transfer `app-debug.apk` to your Android phone and install it. Android may ask you to allow installation from the app or browser used to open it.

## Important notes

- The GitHub Actions workflow builds a debug APK for testing and direct installation. It is not a Play Store-signed release.
- No Supabase keys or secrets are added by this Android wrapper. The included website uses the configuration already present in its HTML.
- If you later update the website HTML, replace **both** `index.html` and `app/src/main/assets/index.html` with the same updated file before building again.
