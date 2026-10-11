/*
 * Copyright (C) 2025 The BootleggersROM Project
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

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties
import android.text.format.DateFormat
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.contract.TAG_DEVICE_STATE_PREFERENCE
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding
import java.text.SimpleDateFormat
import java.util.Locale

class BootlegBuildDatePreference :
    PreferenceMetadata, PreferenceSummaryProvider, PreferenceBinding {

    override val key: String
        get() = "build_date"

    override val title: Int
        get() = R.string.bootleggers_build_date

    override val indexable
        get() = false

    override fun tags(context: Context) = arrayOf(TAG_DEVICE_STATE_PREFERENCE)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
    }

    override fun getSummary(context: Context): CharSequence {
        // Date is embedded as yyyyMMdd in the tail of ro.bootleggers.version
        // (e.g. ...-Vanilla-20261011-123604). Guard the length so eng builds
        // with a short version string can't crash with StringIndexOutOfBounds.
        val version = SystemProperties.get(PROPERTY_ROM_VERSION)
        if (version.length >= 15) {
            try {
                val raw = version.substring(version.length - 15)
                val buildDate = SimpleDateFormat("yyyyMMdd", Locale.US).parse(raw)
                if (buildDate != null) {
                    val format =
                        DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy")
                    return DateFormat.format(format, buildDate).toString()
                }
            } catch (e: Exception) {
                // fall through to unknown
            }
        }
        return context.getString(R.string.unknown)
    }

    companion object {
        const val PROPERTY_ROM_VERSION: String = "ro.bootleggers.version"
    }
}
