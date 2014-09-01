# Clusters are Lists

This sample code depends on Google Maps Android API v2. You will need to follow the steps in [Getting Started](https://developers.google.com/maps/documentation/android/start) to generate an API key and configure your copy of the source.

To summarize,

* You will need a fingerprint of your app signing keystore to generate the API key:

```sh
# for debug certificate fingerprint
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# for release cerficate fingerprint
keytool -list -v -keystore $your_keystore_name -alias $your_alias_name
```

* You will need to go to the [Google APIs Console](https://code.google.com/apis/console/), enable "Google Maps Android API v2" (*NOT* Google Maps API v2, that's for web development).

* Still on the [Google APIs Console](https://code.google.com/apis/console/), you will need to create a new Android key using your certificate fingerprint and the application's package name (`com.example.mapsv2`)

* Update the application's `AndroidManifest.xml` with the new API key:
```xml
        <!--
         ** You need to replace the key below with your own key. **
         The example key below will not be accepted because it is not linked to the
         certificate which you will use to sign this application.
         See: https://developers.google.com/maps/documentation/android/start
         for instructions on how to get your own key.
        -->
        <meta-data
            android:name="com.google.android.maps.v2.API_KEY"
            android:value="AIzaSyDNqkMxf-9LIgsjs91xC6-KygnMo8T4Amg" />
```

* You should also [Setup Google Play Services SDK](http://developer.android.com/google/play-services/setup.html).

## Eclipse

* Import Google Play Services library project into your workspace.
* Import this project into your workspace and set the Google Play Service library project as a dependency.
