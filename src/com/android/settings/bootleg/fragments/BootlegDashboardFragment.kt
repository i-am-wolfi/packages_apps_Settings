package com.android.settings.bootleg.fragments

import android.app.settings.SettingsEnums
import com.android.settings.dashboard.DashboardFragment

abstract class BootlegDashboardFragment: DashboardFragment() {
    override fun getMetricsCategory(): Int = SettingsEnums.DASHBOARD_SUMMARY
}
