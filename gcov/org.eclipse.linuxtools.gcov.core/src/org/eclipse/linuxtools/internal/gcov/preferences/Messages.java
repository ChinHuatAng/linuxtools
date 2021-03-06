/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov <akurtako@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gcov.preferences.messages"; //$NON-NLS-1$
    public static String ColorPreferencePage_BackColorHighest;
    public static String ColorPreferencePage_BackColorLowest;
    public static String ColorPreferencePage_BackColorNotCovered;
    public static String ColorPreferencePage_ColorizeCode;
    public static String ColorPreferencePage_Description;
    public static String ColorPreferencePage_Title;
    public static String ColorPreferencePage_UseGradient;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
