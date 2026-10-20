import SwiftUI
import Shared
import FirebaseCore
import UserNotifications

class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        if #available(iOS 14.0, *) {
            completionHandler([.banner, .list, .sound, .badge])
        } else {
            completionHandler([.alert, .sound, .badge])
        }
    }
}

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase
    private let notificationDelegate = NotificationDelegate()
    private let refreshTaskId = "com.riramzy.pillflow.refresh"

    init() {
        FirebaseApp.configure()
        InitKoinIosKt.doInitKoinForIos()

        let center = UNUserNotificationCenter.current()
        center.delegate = notificationDelegate
        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if granted {
                print("PillFlow iOS: Notification permission granted")
            } else if let error = error {
                print("PillFlow iOS: Notification permission error: \(error.localizedDescription)")
            }
        }

        BGTaskScheduler.shared.register(forTaskWithIdentifier: refreshTaskId, using: nil) { task in guard let refreshTask = task as? BGAppRefreshTask else { return }
            scheduleBackgroundRefresh()

            InitKoinIosKt.triggerAppSyncAndSchedule {
                refreshTask.setTaskCompleted(success: true)
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }.onChange(of: scenePhase) { newPhase in
            if newPhase == .active {
                InitKoinIosKt.triggerAppSyncAndSchedule(onComplete: nil)
            } else if newPhase == .background {
                scheduleBackgroundRefresh()
            }
        }
    }

    private static func scheduleBackgroundRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: refreshTaskId)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 12 * 3600)
        try? BGTaskScheduler.shared.submit(request)
    }
}