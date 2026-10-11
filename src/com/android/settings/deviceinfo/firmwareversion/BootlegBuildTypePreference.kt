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
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.contract.TAG_DEVICE_STATE_PREFERENCE
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding

class BootlegBuildTypePreference :
    PreferenceMetadata, PreferenceSummaryProvider, PreferenceBinding {

    override val key: String
        get() = "release_type"

    override val title: Int
        get() = R.string.bootleg_ri_title_buildtype

    override val indexable
        get() = false

    override fun tags(context: Context) = arrayOf(TAG_DEVICE_STATE_PREFERENCE)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
    }

    override fun getSummary(context: Context): CharSequence {
        val buildType = SystemProperties.get(PROPERTY_RELEASETYPE)
        val kind = when (buildType.split("-").firstOrNull()) {
            "Shishufied" -> context.getString(R.string.bootleg_type_official)
            "Unshishufied" -> context.getString(R.string.bootleg_type_unofficial)
            else -> context.getString(R.string.bootleg_type_unknown)
        }
        return context.getString(R.string.bootleggers_releasetype_skeleton, buildType, kind)
    }

    companion object {
        const val PROPERTY_RELEASETYPE: String = "ro.bootleggers.releasetype"
    }
}
