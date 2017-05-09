# acro-android

An android app made mainly as a learning exersice, Acro acts as an additional safety measure for cyclists ho are doing solo activities.
It uses a phone's GPS and accelerometer to determine if a crash has ocurred, and will send out an alert via SMS to a predefined list of emergency contacts.

The app works with [acro-backend](https://github.com/jcrumb/acro-backend), and has the following functionality:

* Set up emergency contacts with phone numbers to receive alert messages via SMS.
* Live location tracking when the system is active. Each user will have a randomly generated tracking code and PIN they can provide to contacts,
who can then log into the webapp and view their location on a map.
* Accelerometer based crash detection. The algorithm used for detecting crashes is quite naive and would need a fair bit of tuning to be production ready.

When a crash is detected, the user is given 30s to respond saying whether or not they're alright. If they don't respond, a tracking code and access
pin will be sent out to all emergency contacts letting them see where the user is on a map.
