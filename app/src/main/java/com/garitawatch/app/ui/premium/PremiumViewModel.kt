package com.garitawatch.app.ui.premium

import androidx.lifecycle.ViewModel
import com.garitawatch.app.domain.analytics.AnalyticsProvider
import com.garitawatch.app.domain.analytics.AnalyticsScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val analytics: AnalyticsProvider
) : ViewModel() {
    init {
        analytics.trackScreenView(AnalyticsScreens.PREMIUM)
    }
}
