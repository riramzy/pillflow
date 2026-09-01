import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        InitKoinIosKt.initKoinForIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}