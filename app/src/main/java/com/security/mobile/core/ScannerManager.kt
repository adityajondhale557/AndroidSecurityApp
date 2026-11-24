package com.security.mobile.core

import android.app.Application
import android.content.pm.PackageInfo
import com.security.mobile.core.models.ScanMode
import com.security.mobile.core.models.ThreatInfo
import com.security.mobile.core.models.ThreatInfo.Severity
import com.security.mobile.core.models.ThreatInfo.ThreatType

class ScannerManager(private val app: Application) {

    fun scan(mode: ScanMode): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        val packages = app.packageManager.getInstalledPackages(0)

        // Quick scan - just apps
        // Deep scan - apps + certificates + network
        // Security audit - everything
        
        for (packageInfo in packages) {
            val threat = analyzeAppPermissions(packageInfo)
            if (threat != null) threats.add(threat)
        }

        return threats
    }

    private fun analyzeAppPermissions(packageInfo: PackageInfo): ThreatInfo? {
        val permissions = packageInfo.requestedPermissions?.toList() ?: return null

        val hasSMS = permissions.contains("android.permission.SMS_RECEIVE")
        val hasMic = permissions.contains("android.permission.RECORD_AUDIO")
        val hasCamera = permissions.contains("android.permission.CAMERA")
        val hasLocation = permissions.contains("android.permission.ACCESS_FINE_LOCATION")
        val hasContacts = permissions.contains("android.permission.READ_CONTACTS")
        val hasPhone = permissions.contains("android.permission.READ_PHONE_STATE")

        // RAT/Spyware Detection
        if (hasSMS && hasMic && hasCamera && hasLocation) {
            return ThreatInfo(
                packageName = packageInfo.packageName,
                appName = packageInfo.applicationInfo.loadLabel(app.packageManager).toString(),
                severity = Severity.CRITICAL,
                description = "This app has excessive permissions that could indicate spyware or a RAT",
                threatType = ThreatType.RAT_SUSPECTED,
                permissions = permissions,
                action = "uninstall"
            )
        }

        // Surveillance tool detection
        if (hasSMS && hasContacts && hasPhone) {
            return ThreatInfo(
                packageName = packageInfo.packageName,
                appName = packageInfo.applicationInfo.loadLabel(app.packageManager).toString(),
                severity = Severity.HIGH,
                description = "Telecom surveillance permissions detected",
                threatType = ThreatType.SPYWARE,
                permissions = permissions,
                action = "uninstall"
            )
        }

        // Background location tracking
        if (hasLocation && permissions.contains("android.permission.ACCESS_BACKGROUND_LOCATION")) {
            return ThreatInfo(
                packageName = packageInfo.packageName,
                appName = packageInfo.applicationInfo.loadLabel(app.packageManager).toString(),
                severity = Severity.MEDIUM,
                description = "Background location tracking detected",
                threatType = ThreatType.PRIVACY_VIOLATION,
                permissions = permissions,
                action = "restrict_permissions"
            )
        }

        return null
    }
}
