# Service Entitlement

Service entitlement library exports the APIs for querying the service status, based on GSMA TS.43
spec.

## How to debug

This lib produces logcat with log tag `ServiceEntitlement`.

###  Enable logging the raw HTTP request / response / headers

Such log is not enabled by default since it contains sensitive device identifiers.

To enable, set the system property below, with **ROOT**:

NOTE This is only supported on devices of userdebug builds.

```shell
adb root
adb shell setprop dbg.se.pii_loggable true
```

### EAP-AKA auth test

For testing purpose, it may be helpful to make the device under test return a specified
response to EAP-AKA challenge.

To do so, set the system property below, with **ROOT**:

NOTE This is only supported on devices of userdebug builds.

```shell
adb root
adb shell setprop persist.entitlement.fake_eap_aka_response <response>
```
