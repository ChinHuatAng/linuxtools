/*******************************************************************************
 * Copyright (c) 2010, 2018 SAP AG and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Stefan Seelmann - initial implementation posted to
 *    http://www.eclipse.org/forums/index.php?t=msg&th=11863&start=2
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.ui.tests.utils;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.hamcrest.Matcher;

public class ContextMenuHelper {

    /**
     * Clicks the context menu matching the text.
     *
     * @param bot
     *
     * @param texts
     *            the text on the context menu.
     * @throws WidgetNotFoundException
     *             if the widget is not found.
     * @throws SWTException
     *             if the menu item is disabled (the root cause being an
     *             {@link IllegalStateException})
     */
    public static void clickContextMenu(final AbstractSWTBot<?> bot,
            final String... texts) {

        // show
        final MenuItem menuItem = UIThreadRunnable
				.syncExec((WidgetResult<MenuItem>) () -> {
					MenuItem theItem = getMenuItem(bot, texts);
					if (theItem != null && !theItem.isEnabled())
						throw new IllegalStateException("Menu item is diabled");

					return theItem;
				});
        if (menuItem == null) {
            throw new WidgetNotFoundException("Could not find menu: "
                    + Arrays.asList(texts));
        }

        // click
        click(menuItem);

        // hide
		UIThreadRunnable.syncExec(() -> {
			if (menuItem.isDisposed())
				return; // menu already gone
			hide(menuItem.getParent());
		});
    }

    private static MenuItem getMenuItem(final AbstractSWTBot<?> bot,
            final String... texts) {
        MenuItem theItem = null;
        Control control = (Control) bot.widget;
        // for dynamic menus, we need to issue this event
        control.notifyListeners(SWT.MenuDetect, new Event());
        Menu menu = control.getMenu();
        for (String text : texts) {
            Matcher<MenuItem> matcher = allOf(instanceOf(MenuItem.class),
                    withMnemonic(text));
            theItem = show(menu, matcher);
            if (theItem != null) {
                menu = theItem.getMenu();
            } else {
                hide(menu);
                break;
            }
        }
        return theItem;
    }

    private static MenuItem show(final Menu menu, final Matcher<MenuItem> matcher) {
        if (menu != null) {
            menu.notifyListeners(SWT.Show, new Event());
            MenuItem[] items = menu.getItems();
            for (final MenuItem menuItem : items) {
                if (matcher.matches(menuItem)) {
                    return menuItem;
                }
            }
            menu.notifyListeners(SWT.Hide, new Event());
        }
        return null;
    }

    private static void click(final MenuItem menuItem) {
        final Event event = new Event();
        event.time = (int) System.currentTimeMillis();
        event.widget = menuItem;
        event.display = menuItem.getDisplay();
        event.type = SWT.Selection;

		UIThreadRunnable.asyncExec(menuItem.getDisplay(), () -> menuItem.notifyListeners(SWT.Selection, event));
    }

    private static void hide(final Menu menu) {
        menu.notifyListeners(SWT.Hide, new Event());
        if (menu.getParentMenu() != null) {
            hide(menu.getParentMenu());
        }
    }
}
