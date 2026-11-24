package com.security.mobile.core.models

data class ThreatInfo(
    val packageName: String,
    val appName: String,
    val severity: Severity,
    val description: String,
    val threatType: ThreatType,
    val permissions: List<String> = emptyList(),
    val action: String? = null
) {
    enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
    enum class ThreatType { MALWARE, SPYWARE, RAT_SUSPECTED, PRIVACY_VIOLATION, CERTIFICATE_ISSUE }
}
