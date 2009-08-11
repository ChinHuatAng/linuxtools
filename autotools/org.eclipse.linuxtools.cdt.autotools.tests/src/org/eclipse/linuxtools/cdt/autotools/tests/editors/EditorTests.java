/*******************************************************************************
 * Copyright (c) 2008 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.tests.editors;

import junit.framework.Test;
import junit.framework.TestSuite;

public class EditorTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.linuxtools.cdt.autotools.tests.editors");
		//$JUnit-BEGIN$
		suite.addTestSuite(AutomakeColourizationTests.class);
		suite.addTestSuite(AutomakeTextHoverTest.class);
		suite.addTestSuite(AutomakeEditorTests.class);
		suite.addTestSuite(AutoconfTextHoverTest.class);
		//$JUnit-END$
		return suite;
	}

}
