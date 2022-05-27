# Barstore

(name is still open for suggestions)

An app to scan and store barcodes, for use at a later time. You can find the latest stable release on
the [play store](https://play.google.com/store/apps/details?id=com.msiejak.barstore), or you can build it yourself.


## Building

After you clone the repository, add the following lines in the `barstore/local.properties` file

`admob.ad_id = "ca-app-pub-3940256099942544/2247696110"`

`admob.app_id = "ca-app-pub-3940256099942544~3347511713"`

After that, you can then build the project with `barstore/gradlew` or `barstore/gradlew.bat`

`./gradlew assembleDebug --parallel --configure-on-demand`

or on Windows

`gradlew.bat assembleDebug --parallel --configure-on-demand`


## Contributing

Feel free to fork and open a pull request!
