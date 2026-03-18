package com.garitawatch.app.domain.analytics

object AnalyticsEvents {
    const val MANUAL_REFRESH = "manual_refresh"
    const val REORDER_PORTS = "reorder_ports"
    const val ADD_FAVORITE = "add_favorite"
    const val REMOVE_FAVORITE = "remove_favorite"
}

object AnalyticsParams {
    const val PORT_NAME = "port_name"
    const val PORT_NUMBER = "port_number"
    const val SOURCE = "source"
}

object AnalyticsScreens {
    const val DASHBOARD = "Dashboard"
    const val SEARCH = "Search"
    const val PORT_DETAIL = "PortDetail"
    const val ALERTS = "Alerts"
    const val PREMIUM = "Premium"
}
