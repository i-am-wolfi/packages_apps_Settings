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
import android.os.SELinux
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.contract.TAG_DEVICE_STATE_PREFERENCE
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding

class SelinuxStatusPreference :
    PreferenceMetadata, PreferenceSummaryProvider, PreferenceBinding,
    PreferenceAvailabilityProvider {

    override val key: String
        get() = "selinux_status"

    override val title: Int
        get() = R.string.selinux_status

    override val indexable
        get() = false

    override fun tags(context: Context) = arrayOf(TAG_DEVICE_STATE_PREFERENCE)

    override fun isAvailable(context: Context) =
        context.resources.getBoolean(R.bool.config_show_selinux_status)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isSelectable = false
        preference.isCopyingEnabled = true
    }

    override fun getSummary(context: Context): CharSequence {
        val stringId = if (!SELinux.isSELinuxEnabled()) {
            R.string.selinux_status_disabled
        } else if (SELinux.isSELinuxEnforced()) {
            R.string.selinux_status_enforcing
        } else {
            R.string.selinux_status_permissive
        }
        return context.getString(stringId)
    }
}
