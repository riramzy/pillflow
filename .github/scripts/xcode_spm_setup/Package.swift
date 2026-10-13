import PackageDescription

let package = Package(
    name: "xcode_spm_setup",
    platforms: [.macOS(.v13)],
    dependencies: [
        .package(url: "https://github.com/tadija/AEXML.git", exact: "4.7.0"),
        .package(url: "https://github.com/tuist/XcodeProj.git", .upToNextMajor(from: "8.27.7")),
    ],
    targets: [
        .executableTarget(
            name: "xcode_spm_setup",
            dependencies: ["XcodeProj"],
            path: "Sources"
        )
    ]
)
