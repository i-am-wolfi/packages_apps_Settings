/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.display

import android.app.settings.SettingsEnums.OPEN_BATTERY_PERCENTAGE
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.android.settings.R
import com.android.settings.Utils
import com.android.settings.contract.KEY_BATTERY_PERCENTAGE
import com.android.settings.metrics.PreferenceActionMetricsProvider
import com.android.settingslib.datastore.AbstractKeyedDataObservable
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.Permissions
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceChangeReason
import com.android.settingslib.metadata.ReadWritePermit
import com.android.settingslib.metadata.SensitivityLevel
import com.android.settingslib.metadata.SwitchPreference
import lineageos.providers.LineageSettings

// LINT.IfChange
class BatteryPercentageSwitchPreference :
    SwitchPreference(KEY, R.string.battery_percentage, R.string.battery_percentage_description),
    PreferenceActionMetricsProvider,
    PreferenceAvailabilityProvider {

    override val preferenceActionMetrics: Int
        get() = OPEN_BATTERY_PERCENTAGE

    override fun tags(context: Context) = arrayOf(KEY_BATTERY_PERCENTAGE)

    override fun storage(context: Context): KeyValueStore =
        BatteryPercentageStorage(context)

    override fun isAvailable(context: Context): Boolean =
        Utils.isBatteryPresent(context) &&
            context.resources.getBoolean(
                com.android.internal.R.bool.config_battery_percentage_setting_available
            )

    override fun getReadPermissions(context: Context) = Permissions.EMPTY

    override fun getReadPermit(context: Context, callingPid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    override fun getWritePermissions(context: Context) = Permissions.allOf(
        android.Manifest.permission.WRITE_SETTINGS,
    )

    override fun getWritePermit(
        context: Context,
        value: Boolean?,
        callingPid: Int,
        callingUid: Int,
    ) = ReadWritePermit.ALLOW

    override val sensitivityLevel
        get() = SensitivityLevel.NO_SENSITIVITY

    @Suppress("UNCHECKED_CAST")
    private class BatteryPercentageStorage(private val context: Context) :
        AbstractKeyedDataObservable<String>(), KeyValueStore {

        private val settingsObserver =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    notifyChange(KEY, PreferenceChangeReason.VALUE)
                }
            }

        override fun contains(key: String) = key == KEY

        override fun <T : Any> getValue(key: String, valueType: Class<T>): T? {
            // SystemUI (Lineage pipeline) reads the Lineage mode key, not the
            // AOSP boolean. A mode other than HIDDEN means "show".
            val mode = LineageSettings.System.getInt(
                context.contentResolver,
                LineageSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT,
                MODE_UNSET,
            )
            val shown = if (mode == MODE_UNSET) {
                // Never set via Lineage key: honor the legacy AOSP value.
                Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SHOW_BATTERY_PERCENT,
                    if (context.resources.getBoolean(
                            com.android.internal.R.bool.config_defaultBatteryPercentageSetting
                        )
                    ) 1 else 0,
                ) == 1
            } else {
                mode != SHOW_PERCENT_HIDDEN
            }
            return shown as T
        }

        override fun <T : Any> setValue(key: String, valueType: Class<T>, value: T?) {
            val show = value as? Boolean ?: false
            // Keep the AOSP key in sync for anything still reading it...
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SHOW_BATTERY_PERCENT,
                if (show) 1 else 0,
            )
            // ...but SystemUI actually consumes the Lineage mode key.
            LineageSettings.System.putInt(
                context.contentResolver,
                LineageSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT,
                if (show) SHOW_PERCENT_NEXT_TO else SHOW_PERCENT_HIDDEN,
            )
            notifyChange(key, PreferenceChangeReason.VALUE)
        }

        override fun onFirstObserverAdded() {
            context.contentResolver.registerContentObserver(
                LineageSettings.System.getUriFor(
                    LineageSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT
                ),
                false,
                settingsObserver,
            )
            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.SHOW_BATTERY_PERCENT),
                false,
                settingsObserver,
            )
        }

        override fun onLastObserverRemoved() {
            context.contentResolver.unregisterContentObserver(settingsObserver)
        }
    }

    companion object {
        const val KEY = Settings.System.SHOW_BATTERY_PERCENT

        // Mirrors BatteryRepository modes in SystemUI.
        const val SHOW_PERCENT_HIDDEN = 0
        const val SHOW_PERCENT_NEXT_TO = 2

        // Sentinel: Lineage key never written on this device yet.
        const val MODE_UNSET = -1
    }
}
// LINT.ThenChange(BatteryPercentagePreferenceController.java)
