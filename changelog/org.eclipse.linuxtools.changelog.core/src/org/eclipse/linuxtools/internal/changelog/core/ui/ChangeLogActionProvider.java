/*******************************************************************************
 * Copyright (c) 2006, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.changelog.core.actions.PrepareChangeLogAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class ChangeLogActionProvider extends CommonActionProvider {
    private Action exampleAction;

    @Override
    public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);
        exampleAction = new PrepareChangeLogAction() {
            @Override
            public void run() {
                setSelection((IStructuredSelection) getContext().getSelection());
                doRun();
            }

        };

    }

    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        menu.add(exampleAction);
    }

    @Override
    public void fillActionBars(IActionBars actionBars) {

        actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
                exampleAction);
    }
}
