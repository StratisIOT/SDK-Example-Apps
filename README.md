# StratisSDK Example Apps

Sample iOS & Android apps to help you hit the ground running with our new SDK

## iOS Getting Started

1. Copy your **StratisSDK.framework** into the [Frameworks](ios/Frameworks) folder.
1. Add the **LegicMobileSdk.framework** and **StratisSDK.framework** to your build Frameworks.
1. Manually update the following in the [MainViewController.swift](ios/StratisSDKExampleApp/MainViewController.swift) (currently the settings form is not working.)
    * public var accessToken: String = ""
    * public var propertyID: String = ""
    * serverEnvironment: .DEV
1. Build it!

## Android Getting Started

1. Copy your **stratissdk-release.aar** into [libs](android/sdkexamplev2/libs)
1. (Optional) Manually update the following in the [MainActivity.kt](android/sdkexamplev2/src/main/java/com/example/sdkexample/MainActivity.kt)
    * private var accessToken: String = ""
    * private var propertyID: String = ""
    * private var serverEnvironment: ServerEnvironment? = null
1. Build it!
