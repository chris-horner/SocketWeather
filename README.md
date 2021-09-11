# Socket Weather

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" title="Launcher icon" align="left" width="72" height="72">

Inspired by [Shifty Jelly's](https://shiftyjelly.com/) retired
[Pocket Weather](https://blog.shiftyjelly.com/2018/08/31/everything-that-begins-must-also-end/), this app makes use of
a completely undocumented API from Australia's [Bureau of Meteorology](https://weather.bom.gov.au). It might break
at a moment's notice, but while it's up we can enjoy a simple Australian weather app. It's available on [Google Play](https://play.google.com/store/apps/details?id=codes.chrishorner.socketweather).

<img src="app/src/main/play/listings/en-AU/graphics/phone-screenshots/1.png" width="30%" align="right">

This app is a bit of a hobby project where I experiment with different ideas. The code, UI, and feature set are likely to change quite a bit. If there are any features or improvements you'd like to see, feel free to submit an issue to discuss potential PRs.

## What API are you hitting?

I stumbled upon https://api.weather.bom.gov.au/v1. All requests and responses have been modelled after poking and prodding that endpoint.

# License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
