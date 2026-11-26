import UIKit
import FirebaseCore
import GoogleSignIn
import Shared

final class IosSocialAuthDelegateImpl: NSObject, IosSocialAuthDelegate {
    var supportsApple: Bool { false }

    func startGoogleSignIn(
        onSuccess: @escaping (String, String?, String?, String?, String?) -> Void,
        onCancel: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        DispatchQueue.main.async {
            guard let clientID = FirebaseApp.app()?.options.clientID else {
                onError("Missing default_web_client_id")
                return
            }

            guard let topVC = Self.topViewController() else {
                onError("No active screen available")
                return
            }

            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
            GIDSignIn.sharedInstance.signIn(withPresenting: topVC) { result, error in
                if let nsError = error as NSError?,
                    nsError.domain == "com.google.GIDSignIn",
                    nsError.code == -5 {
                        onCancel()
                        return
                    }

                if let error = error {
                    onError(error.localizedDescription)
                    return
                }

                guard let user = result?.user,

                let idToken = user.idToken?.tokenString else {
                    onError("Missing Google ID token")
                    return
                }

                onSuccess(
                    idToken,
                    user.accessToken.tokenString,
                    user.profile?.givenName,
                    user.profile?.familyName,
                    user.profile?.email
                )
            }
        }
    }

    func startAppleSignIn(
        onSuccess: @escaping (String, String, String?, String?, String?) -> Void,
        onCancel: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        onError("Apple Sign-In is not configured")
    }

    func startGoogleSignOut() {
        DispatchQueue.main.async {
            GIDSignIn.sharedInstance.signOut()
        }
    }

    private static func topViewController() -> UIViewController? {
        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        let scene = scenes.first { $0.activationState == .foregroundActive } ?? scenes.first
        var vc = scene?.windows.first { $0.isKeyWindow }?.rootViewController
            ?? scene?.windows.first?.rootViewController

        while let presented = vc?.presentedViewController {
            vc = presented
        }

        return vc
    }
}