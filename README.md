# SquickPic

A fun Android photo booth: pay a dollar 💳, choose a filter, smile, Snap! 📸 The photo booth prints the picture and tweets it 🐦.

## Building

Update `gradle.properties`:

* Follow the Square [Reader SDK Setup](https://docs.connect.squareup.com/payments/readersdk/setup-android#step-1-request-reader-sdk-credentials) and set `SQUARE_READER_SDK_APPLICATION_ID` and `SQUARE_READER_SDK_REPOSITORY_PASSWORD`
* Follow the Twitter [Authentication guide](https://developer.twitter.com/en/docs/basics/authentication/guides/access-tokens.html) and set `TWITTER_CONSUMER_KEY` and `TWITTER_CONSUMER_SECRET`

Install with `./gradlew :app:installDebug`

## License

    Copyright 2018 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
