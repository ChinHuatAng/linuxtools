/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Keith Seitz <keiths@redhat.com> - setup code in launch the method, initially
 *        written in the now-defunct OprofileSession class
 *    QNX Software Systems and others - the section of code marked in the launch
 *        method, and the exec method
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.launch.launching;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile.OprofileProject;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchMessages;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.OprofileCounter;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.internal.oprofile.ui.view.OcountView;
import org.eclipse.linuxtools.internal.oprofile.ui.view.OprofileView;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractOprofileLaunchConfigurationDelegate extends AbstractCLaunchDelegate {
    protected ILaunchConfiguration config;
    private static final String OPROFILE_DATA = "oprofile_data"; //$NON-NLS-1$
    private static final String OCOUNT_DATA = "ocount_data"; //$NON-NLS-1$
    private static final String SESSION_DIR = "--session-dir="; //$NON-NLS-1$
    private static final String OUTPUT_FILE = "--output-file="; //$NON-NLS-1$
    private static final String EVENTS = "--events="; //$NON-NLS-1$
    private static final String APPEND = "--append"; //$NON-NLS-1$
    private static final String OPD_SETUP_EVENT_SEPARATOR = ":"; //$NON-NLS-1$
    private static final String OPD_SETUP_EVENT_TRUE = "1"; //$NON-NLS-1$
    private static final String OPD_SETUP_EVENT_FALSE = "0"; //$NON-NLS-1$

    @Override
    public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        this.config = config;
        Oprofile.OprofileProject.setProject(getProject());
        LaunchOptions options = new LaunchOptions();        //default options created in the constructor
        options.loadConfiguration(config);
        IPath exePath = getExePath(config);
        options.setBinaryImage(exePath.toOSString());
        Oprofile.OprofileProject.setProfilingBinary(options.getOprofileComboText());

        /*
         * Parameters needed for the application under profiling
         */
        String appArgs[] = getProgramArgumentsArray(config);
        String appEnv[] = getEnvironment(config);

        //if daemonEvents null or zero size, the default event will be used
        OprofileDaemonEvent[] daemonEvents = null;
        ArrayList<OprofileDaemonEvent> events = new ArrayList<>();
        if (!config.getAttribute(OprofileLaunchPlugin.ATTR_USE_DEFAULT_EVENT, false)) {
            //get the events to profile from the counters
            OprofileCounter[] counters = oprofileCounters(config);

            for (int i = 0; i < counters.length; ++i) {
                if (counters[i].getEnabled()) {
                    OprofileDaemonEvent[] counterEvents  = counters[i].getDaemonEvents();
                    events.addAll(Arrays.asList(counterEvents));
                }
            }

            daemonEvents = new OprofileDaemonEvent[events.size()];
            events.toArray(daemonEvents);
        }

        /*
         * this code written by QNX Software Systems and others and was
         * originally in the CDT under LocalCDILaunchDelegate::RunLocalApplication
         */

        if (!preExec(options, daemonEvents, launch)) {
            return;
        }
        Process process = null;
        // Executing operf with the default or specified events,
        // outputing the profiling data to the project dir/OPROFILE_DATA
        if (OprofileProject.getProfilingBinary().equals(OprofileProject.OPERF_BINARY)) {

            String eventsString=null;

            // Event spec: "EVENT:count:mask:profileKernel:profileUser"
            StringBuilder spec = new StringBuilder();
            spec.append(EVENTS);
            boolean isCommaAllowed = false;
            for (int i=0;i<events.size();i++) {
                OprofileDaemonEvent event = events.get(i);
                if(isCommaAllowed) {
                    spec.append(',');
                }
                spec.append(event.getEvent().getText());
                spec.append(OPD_SETUP_EVENT_SEPARATOR);
                spec.append(event.getResetCount());
                spec.append(OPD_SETUP_EVENT_SEPARATOR);
                spec.append(event.getEvent().getUnitMask().getMaskValue());
                // Some platforms allow EXCLUSIVE masks with same bit value, but
                // different names and thus, the name is needed when specifying.
                if (event.getEvent().getUnitMask().getMaskName() != null) {
                	spec.append(OPD_SETUP_EVENT_SEPARATOR);
                	spec.append(event.getEvent().getUnitMask().getMaskName());
                }
                spec.append(OPD_SETUP_EVENT_SEPARATOR);
                spec.append((event.getProfileKernel() ? OPD_SETUP_EVENT_TRUE : OPD_SETUP_EVENT_FALSE));
                spec.append(OPD_SETUP_EVENT_SEPARATOR);
                spec.append((event.getProfileUser() ? OPD_SETUP_EVENT_TRUE : OPD_SETUP_EVENT_FALSE));
                isCommaAllowed = true;
            }
            eventsString = spec.toString();

            ArrayList<String> argArray = new ArrayList<>(Arrays.asList(appArgs));
            // Use remote file proxy to determine data folder since the project may be either local or remote
            IRemoteFileProxy proxy = RemoteProxyManager.getInstance().getFileProxy(OprofileProject.getProject());
            IFileStore dataFolder = proxy.getResource(oprofileWorkingDirURI(config).getPath() + IPath.SEPARATOR + OPROFILE_DATA);
            if(! dataFolder.fetchInfo().exists()) {
                dataFolder.mkdir(EFS.SHALLOW, null);
            }
            argArray.add(0, exePath.toOSString());
            if (events.size()>0) {
                argArray.add(0,eventsString);
            }
            argArray.add(0, SESSION_DIR + oprofileWorkingDirURI(config).getPath() + IPath.SEPARATOR + OPROFILE_DATA);
            argArray.add(0, OprofileProject.OPERF_BINARY);

            boolean appended = false;
            for(int i = 0; i < options.getExecutionsNumber(); i++){
                /*
                 * If profiling multiple times,
                 * append oprofile results from 2nd execution on.
                 */
                if (!appended && i!=0) {
                    argArray.add(1, APPEND);
                    appended = true;
                }
                String[] arguments = new String[argArray.size()];
                arguments = argArray.toArray(arguments);
                try {
                    process = RuntimeProcessFactory.getFactory().exec(arguments, appEnv , OprofileProject.getProject());
                } catch (IOException e1) {
                	if (process != null)
                		process.destroy();
                	Status status = new Status(IStatus.ERROR, OprofileLaunchPlugin.PLUGIN_ID, OprofileLaunchMessages.getString("oprofilelaunch.error.interrupted_error.status_message")); //$NON-NLS-1$
                    throw new CoreException(status);
                }
                DebugPlugin.newProcess( launch, process, renderProcessLabel( exePath.toOSString() ) );
                try{
                    process.waitFor();
                } catch (InterruptedException e){
                	if (process != null)
                		process.destroy();
                    Status status = new Status(IStatus.ERROR, OprofileLaunchPlugin.PLUGIN_ID, OprofileLaunchMessages.getString("oprofilelaunch.error.interrupted_error.status_message")); //$NON-NLS-1$
                    throw new CoreException(status);
                }
            }
        }

        // Executing ocount with the default or specified events,
        // outputing the profiling data to the project dir/OPROFILE_DATA
        if (OprofileProject.getProfilingBinary().equals(OprofileProject.OCOUNT_BINARY)) {
            String eventsString=null;
            // Event spec: "EVENT:count:mask:profileKernel:profileUser"
            StringBuilder spec = new StringBuilder();
            spec.append(EVENTS);
            boolean isCommaAllowed = false;
            for (int i=0;i<events.size();i++) {
                OprofileDaemonEvent event = events.get(i);
                if(isCommaAllowed) {
                    spec.append(',');
                }
                spec.append(event.getEvent().getText());
                spec.append(OPD_SETUP_EVENT_SEPARATOR);
                spec.append(event.getResetCount());
                spec.append(OPD_SETUP_EVENT_SEPARATOR);
                spec.append(event.getEvent().getUnitMask().getMaskValue());
                spec.append(OPD_SETUP_EVENT_SEPARATOR);
                spec.append((event.getProfileKernel() ? OPD_SETUP_EVENT_TRUE : OPD_SETUP_EVENT_FALSE));
                spec.append(OPD_SETUP_EVENT_SEPARATOR);
                spec.append((event.getProfileUser() ? OPD_SETUP_EVENT_TRUE : OPD_SETUP_EVENT_FALSE));
                isCommaAllowed = true;
            }
            eventsString = spec.toString();

            ArrayList<String> argArray = new ArrayList<>(Arrays.asList(appArgs));
            argArray.add(0, exePath.toOSString());
            if (events.size()>0) {
                argArray.add(0,eventsString);
            }
            argArray.add(0, OUTPUT_FILE + oprofileWorkingDirURI(config).getPath() + IPath.SEPARATOR + OCOUNT_DATA);
            argArray.add(0, OprofileProject.OCOUNT_BINARY);

            String[] arguments = new String[argArray.size()];
            arguments = argArray.toArray(arguments);
            try {
                process = RuntimeProcessFactory.getFactory().exec(arguments, OprofileProject.getProject());
            } catch (IOException e1) {
                process.destroy();
                Status status = new Status(IStatus.ERROR, OprofileLaunchPlugin.PLUGIN_ID, OprofileLaunchMessages.getString("oprofilelaunch.error.interrupted_error.status_message")); //$NON-NLS-1$
                throw new CoreException(status);
            }
            DebugPlugin.newProcess( launch, process, renderProcessLabel( exePath.toOSString() ) );
            try{
                process.waitFor();

                // Put the OCount data in a separate view
                StringBuffer buffer = new StringBuffer();
                // Use remote file proxy to operate resources since the project may be either local or remote
                IRemoteFileProxy proxy = RemoteProxyManager.getInstance().getFileProxy(OprofileProject.getProject());

                IFileStore ocountDataStore = proxy.getResource(oprofileWorkingDirURI(config).getPath() +
                        IPath.SEPARATOR + OCOUNT_DATA);
                try (
                    InputStream is = ocountDataStore.openInputStream(EFS.NONE, monitor);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))
                ) {
                    String s = reader.readLine();
                    String sep_char = ""; //$NON-NLS-1$
                    while (s != null) {
                        buffer.append(s);
                        buffer.append(sep_char);
                        sep_char = "\n"; //$NON-NLS-1$
                        s = reader.readLine();
                    }

                    // Open the OCount View and display output from ocount
                    final String text = buffer.toString();
                    Display.getDefault().syncExec(() -> refreshOcountView(text));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (InterruptedException e){
                process.destroy();
                Status status = new Status(IStatus.ERROR, OprofileLaunchPlugin.PLUGIN_ID, OprofileLaunchMessages.getString("oprofilelaunch.error.interrupted_error.status_message")); //$NON-NLS-1$
                throw new CoreException(status);
            }
            return;
        }

        postExec(options, daemonEvents, process);

    }

    protected abstract boolean preExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, ILaunch launch);

    protected abstract void postExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, Process process);

    @Override
    protected String getPluginID() {
        return OprofileLaunchPlugin.PLUGIN_ID;
    }

    //Helper function to refresh the oprofile view. Opens and focuses the view
    // if it isn't already.
    protected void refreshOprofileView() {
        OprofileView view = OprofileUiPlugin.getDefault().getOprofileView();
        if (view != null) {
            view.refreshView();
        } else {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(OprofileUiPlugin.ID_OPROFILE_VIEW);
            } catch (PartInitException e2) {
                e2.printStackTrace();
            }
            OprofileUiPlugin.getDefault().getOprofileView().refreshView();
        }
    }
    
    //Helper function to refresh the ocount view. Opens and focuses the view
    // if it isn't already.
    protected void refreshOcountView(String text) {
        OcountView view = OprofileUiPlugin.getDefault().getOcountView();
        if (view == null) {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(OprofileUiPlugin.ID_OCOUNT_VIEW);
            } catch (PartInitException e2) {
                e2.printStackTrace();
                return;
            }
            view = OprofileUiPlugin.getDefault().getOcountView();
        }
        view.setText(text);
        view.refreshView();
    }

    /* all these functions exist to be overridden by the test class in order to allow launch testing */

    protected IProject oprofileProject(){
        return Oprofile.OprofileProject.getProject();
    }


    /**
     * Return the URI of the current working directory from the current
     * project's file proxy.
     *
     * @return URI URI of the working directory.
     * @throws CoreException
     */
    protected URI oprofileWorkingDirURI(ILaunchConfiguration config) throws CoreException{
        File workingDirectory = this.getWorkingDirectory(config);
        if(workingDirectory == null){
            return getProject().getLocationURI();
        } else {
            URI uri = null;
            try {
                uri = new URI(workingDirectory.getAbsolutePath());
            } catch (URISyntaxException e) {
                //Since working directory paths are verified by the launch tab, this exception should never be thrown
                Status status = new Status(IStatus.ERROR, OprofileCorePlugin.getId(),
                        OprofileLaunchMessages.getString("oprofilelaunch.error.invalidworkingdir.status_message")); //$NON-NLS-1$
                throw new CoreException(status);
            }
            return uri;
        }
    }

    protected OprofileCounter[] oprofileCounters(ILaunchConfiguration config){
        return OprofileCounter.getCounters(config);
    }

    /**
     * Runs opcontrol --help. Returns true if there was any output, false
     * otherwise. Return value can be used to tell if the user successfully
     * entered a password.
     * @return true if opcontrol --help was run correctly. False otherwise
     */
    protected boolean oprofileStatus() {
		if (OprofileProject.getProfilingBinary().equals(OprofileProject.OPERF_BINARY)) {
			try {
				Process p = RuntimeProcessFactory.getFactory().exec(new String[] { "operf", "--version" }, //$NON-NLS-1$ //$NON-NLS-2$
						OprofileProject.getProject());
				return (p != null);
			} catch (IOException e) {
				return false;
			}
		} else {
			try {
				Process p = RuntimeProcessFactory.getFactory().exec(new String[] { "ocount", "--version" }, //$NON-NLS-1$ //$NON-NLS-2$
						OprofileProject.getProject());
				return (p != null);
			} catch (IOException e) {
				return false;
			}
		}
    }

    protected IProject getProject(){
        try{
            return CDebugUtils.verifyCProject(config).getProject();
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param config
     * @return
     * @throws CoreException
     * @since 1.1
     */
    protected IPath getExePath(ILaunchConfiguration config) throws CoreException{
        return CDebugUtils.verifyProgramPath( config );
    }
}
