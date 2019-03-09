package na.komi.kodesh.ui.internal

/**
 * Fix indentation in preferences
 * https://is.gd/zoKZaZ
 */

import android.annotation.SuppressLint
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {
    private fun setAllPreferencesToAvoidHavingExtraSpace(preference: Preference) {
        preference.isIconSpaceReserved = false
        if (preference is PreferenceGroup)
            for (i in 0 until preference.preferenceCount)
                setAllPreferencesToAvoidHavingExtraSpace(preference.getPreference(i))
    }

    override fun setPreferenceScreen(preferenceScreen: PreferenceScreen?) {
        if (preferenceScreen != null)
            setAllPreferencesToAvoidHavingExtraSpace(preferenceScreen)
        super.setPreferenceScreen(preferenceScreen)

    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen?): RecyclerView.Adapter<*> =
            object : PreferenceGroupAdapter(preferenceScreen) {
                @SuppressLint("RestrictedApi")
                override fun onPreferenceHierarchyChange(preference: Preference?) {
                    if (preference != null)
                        setAllPreferencesToAvoidHavingExtraSpace(preference)
                    super.onPreferenceHierarchyChange(preference)
                }
            }
}