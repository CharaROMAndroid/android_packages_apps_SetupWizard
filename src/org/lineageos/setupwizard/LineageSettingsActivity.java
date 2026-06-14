/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard;

import static org.lineageos.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

import com.android.internal.util.crdroid.Utils;

public class LineageSettingsActivity extends BaseSetupWizardActivity {

    private SetupWizardApp mSetupWizardApp;

    private CheckBox mNavKeys;
    private TextView mNothingHereText; // Added for the empty state

    private boolean mSupportsKeyDisabler = false;

    private final View.OnClickListener mNavKeysClickListener = view -> {
        boolean checked = !mNavKeys.isChecked();
        mNavKeys.setChecked(checked);
        mSetupWizardApp.getSettingsBundle().putBoolean(DISABLE_NAV_KEYS, checked);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetupWizardApp = (SetupWizardApp) getApplication();
        setNextText(R.string.next);

        View navKeysRow = findViewById(R.id.nav_keys);
        navKeysRow.setOnClickListener(mNavKeysClickListener);
        mNavKeys = findViewById(R.id.nav_keys_checkbox);
        
        // Find the TextView for the empty state
        mNothingHereText = findViewById(R.id.nothing_here_text);

        mSupportsKeyDisabler = isKeyDisablerSupported(this);
        if (mSupportsKeyDisabler) {
            mNavKeys.setChecked(LineageSettings.System.getIntForUser(getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR,
                    Utils.hasNavbarByDefault(this) ? 1 : 0, UserHandle.USER_CURRENT) != 0);
                    
            // Ensure the text is hidden if features are present
            if (mNothingHereText != null) {
                mNothingHereText.setVisibility(View.GONE);
            }
        } else {
            navKeysRow.setVisibility(View.GONE);
            
            // Show the text and set the string when nav keys aren't supported
            if (mNothingHereText != null) {
                mNothingHereText.setText(R.string.nothing_here);
                mNothingHereText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_lineage_settings;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_services;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_features;
    }

    private void updateDisableNavkeysOption() {
        if (mSupportsKeyDisabler) {
            final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
            boolean enabled = LineageSettings.System.getIntForUser(getContentResolver(),
                    LineageSettings.System.FORCE_SHOW_NAVBAR,
                    Utils.hasNavbarByDefault(this) ? 1 : 0, UserHandle.USER_CURRENT) != 0;
            boolean checked = myPageBundle.containsKey(DISABLE_NAV_KEYS) ?
                    myPageBundle.getBoolean(DISABLE_NAV_KEYS) :
                    enabled;
            mNavKeys.setChecked(checked);
            myPageBundle.putBoolean(DISABLE_NAV_KEYS, checked);
        }
    }

    private static boolean isKeyDisablerSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE);
    }
}