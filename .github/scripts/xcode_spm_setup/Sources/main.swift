import Foundation
import XcodeProj
import PathKit

func main() {
    let args = CommandLine.arguments
    guard args.count >= 5 else { exit(1) }
    var arguments = args
    _ = arguments.removeFirst()
    let projectPath = Path(arguments.removeFirst())
    let repoURL = arguments.removeFirst()
    let versionRequirementString = arguments.removeFirst()
    
    var plistPath: Path? = nil
    if let plistIndex = arguments.firstIndex(of: "--plist"), plistIndex + 1 < arguments.count {
        plistPath = Path(arguments[plistIndex + 1])
        arguments.remove(at: plistIndex + 1)
        arguments.remove(at: plistIndex)
    }

    let products = arguments

    do {
        let xcodeproj = try XcodeProj(path: projectPath)
        let pbxproj = xcodeproj.pbxproj
        guard let rootObject = try pbxproj.rootProject(), let target = pbxproj.nativeTargets.first else { exit(1) }
        
        let packageRef: XCRemoteSwiftPackageReference
        if let existingPkg = rootObject.remotePackages.first(where: { $0.repositoryURL == repoURL }) {
            packageRef = existingPkg
        } else {
            packageRef = try rootObject.addSwiftPackage(
                repositoryURL: repoURL, 
                productName: products.first!, 
                versionRequirement: .upToNextMajorVersion(versionRequirementString), 
                targetName: target.name
            )
        }
        
        var frameworksBuildPhase = target.buildPhases.compactMap { $0 as? PBXFrameworksBuildPhase }.first
        if frameworksBuildPhase == nil {
            let newPhase = PBXFrameworksBuildPhase()
            pbxproj.add(object: newPhase)
            target.buildPhases.append(newPhase)
            frameworksBuildPhase = newPhase
        }
        
        for product in products {
            if target.packageProductDependencies?.contains(where: { $0.productName == product }) == true { continue }
            let dependency = XCSwiftPackageProductDependency(productName: product, package: packageRef)
            pbxproj.add(object: dependency)
            if target.packageProductDependencies == nil { target.packageProductDependencies = [] }
            target.packageProductDependencies?.append(dependency)
            
            let buildFile = PBXBuildFile(product: dependency)
            pbxproj.add(object: buildFile)
            if frameworksBuildPhase?.files == nil { frameworksBuildPhase?.files = [] }
            frameworksBuildPhase?.files?.append(buildFile)
        }

        for configuration in target.buildConfigurationList?.buildConfigurations ?? [] {
            configuration.buildSettings["PRODUCT_NAME"] = "PillFlow"

            var otherLdFlags: [String] = []
            if let current = configuration.buildSettings["OTHER_LDFLAGS"] as? [String] {
                otherLdFlags = current
            } else if let currentString = configuration.buildSettings["OTHER_LDFLAGS"] as? String {
                otherLdFlags = [currentString]
            }
            if !otherLdFlags.contains("-ObjC") {
                otherLdFlags.append("-ObjC")
                configuration.buildSettings["OTHER_LDFLAGS"] = otherLdFlags
            }
        }
        
        try xcodeproj.write(path: projectPath)
        print("Firebase added to Xcode successfully with PRODUCT_NAME=PillFlow!")
    } catch {
        print("Error: \(error)")
        exit(1)
    }
}
main()
