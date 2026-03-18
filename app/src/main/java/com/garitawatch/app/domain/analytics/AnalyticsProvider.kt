package com.garitawatch.app.domain.analytics

interface AnalyticsProvider {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun trackScreenView(screenName: String, screenClass: String? = null)
}
