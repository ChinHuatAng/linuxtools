/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch.provider.launch;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.profiling.launch.provider.launch.messages"; //$NON-NLS-1$
    public static String ProviderLaunchShortcut_0;
    public static String ProviderPreferencesPage_0;
    public static String ProviderPreferencesPage_1;
    public static String ProviderPreferencesPage_2;
    public static String ProviderOptionsTab_0;
    public static String UseProjectSetting_0;
    public static String ProjectSettings_0;
    public static String PreferenceLink_0;
    public static String ProviderLaunchConfigurationPrompt_0;
    public static String ProviderNoProfilers_title_0;
    public static String ProviderNoProfilers_msg_0;
    public static String ProviderProfilerMissing_title_0;
    public static String ProviderProfilerMissing_msg_0;
    public static String ProviderProfilerMissing_msg_1;
    public static String ProviderLaunchError_msg_0;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
