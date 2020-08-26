# StratisSDK Example Apps

Sample iOS & Android apps to help you hit the ground running with our new SDK

## iOS Getting Started

1. Copy your **StratisSDK.framework** into the [Frameworks](ios/Frameworks) folder.
1. Inside the directory **StratisSDK.framework**, locate the files named **LegicMobileSdk.framework** and **SaltoJustINMobileSDK.xcframework** and move them to the same directory as the **StratisSDK.framework**
1. Navigate to your app's target and select the General tab, then add all 3 frameworks to the section labeled "Frameworks, Libraries, and embedded Content.
1. Mark all three as Embed & Sign.
1. In your targets Build Settings tab, find the setting labeled "Valid Architectures" and adjust it to only include arm64 and arm64e.
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
