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
import android.content.Intent
import android.net.Uri
import android.os.SystemProperties
import android.util.Log
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.Utils
import com.android.settings.contract.TAG_DEVICE_STATE_PREFERENCE
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding

class BootlegMusicCodenamePreference :
    PreferenceMetadata, PreferenceSummaryProvider, PreferenceBinding,
    Preference.OnPreferenceClickListener {

    override val key: String
        get() = "music_codename"

    override val title: Int
        get() = R.string.bootleg_ri_title_musicode

    override val indexable
        get() = false

    override fun tags(context: Context) = arrayOf(TAG_DEVICE_STATE_PREFERENCE)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
        preference.onPreferenceClickListener = this
    }

    override fun getSummary(context: Context): CharSequence {
        // Musical codename uses niceparse.Song_Name encoding because raw
        // system properties can't hold spaces.
        val prop = SystemProperties.get(PROPERTY_MUSICODENAME)
        return if (prop.contains("niceparse")) {
            prop.substringAfterLast(".").replace('_', ' ')
        } else {
            prop.ifEmpty { context.getString(R.string.unknown) }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (Utils.isMonkeyRunning()) return true
        val context = preference.context
        val url = SystemProperties.get(PROPERTY_MUSICODENAME_URL)
        if (url.isEmpty()) return true
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (context.packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            // Don't send out the intent to stop crash
            Log.w(TAG, "queryIntentActivities() returns empty")
            return true
        }
        context.startActivity(intent)
        return true
    }

    companion object {
        const val TAG = "BootlegMusicCodenamePreference"
        const val PROPERTY_MUSICODENAME: String = "ro.bootleggers.songcodename"
        const val PROPERTY_MUSICODENAME_URL: String = "ro.bootleggers.songcodeurl"
    }
}
