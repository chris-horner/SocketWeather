# Socket Weather

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" title="Launcher icon" align="left" width="72" height="72">

Inspired by [Shifty Jelly's](https://shiftyjelly.com/) retired
[Pocket Weather](https://blog.shiftyjelly.com/2018/08/31/everything-that-begins-must-also-end/), this app makes use of
a completely undocumented API from Australia's [Bureau of Meteorology](https://weather.bom.gov.au). It might break
at a moment's notice, but while it's up we can enjoy a simple Australian weather app. It's available on [Google Play](https://play.google.com/store/apps/details?id=codes.chrishorner.socketweather).

<img src="app/src/main/play/listings/en-AU/graphics/phone-screenshots/1.png" width="30%" align="right">

This app is a bit of a hobby project where I experiment with different ideas. The code, UI, and feature set are likely to change quite a bit. If there are any features or improvements you'd like to see, feel free to submit an issue to discuss potential PRs.

## FAQs

### What API are you hitting?

I stumbled upon https://api.weather.bom.gov.au/v1. All requests and responses have been modelled after poking and prodding that endpoint.

## Google Maps

If you're building this project and want the `MapView` to work, generate an API key as per [the documentation](https://developers.google.com/maps/documentation/android-sdk/get-api-key). When restricting the key to Android apps, use `codes.chrishorner.socketweather.debug` as the package name, and `31:A7:4A:C7:5E:DD:A5:7C:8E:01:F5:9E:83:4A:EE:C6:DE:BC:73:EB` as the SHA-1 fingerprint.

Once you have an API key, add it to `local.properties` like so
```
google.maps.key={insert_your_api_key_here}
```
