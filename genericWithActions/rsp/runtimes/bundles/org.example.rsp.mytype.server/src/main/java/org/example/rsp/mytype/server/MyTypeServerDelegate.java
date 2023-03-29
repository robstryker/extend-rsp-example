/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.mytype.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.example.rsp.mytype.server.impl.IMyTypeServerAttributes;
import org.example.rsp.mytype.server.impl.MyTypeFiveStepAction;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.IPublishControllerWithOptions;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerSuffixPublishController;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerType;
import org.jboss.tools.rsp.server.spi.launchers.AbstractJavaLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;

public class MyTypeServerDelegate extends GenericServerBehavior {

	public MyTypeServerDelegate(IServer server, JSONMemento behaviorMemento) {
		super(server, behaviorMemento);
	}
	@Override
	public void setDependentDefaults(IServerWorkingCopy server) {
		// This basically takes optional parameters that the user may have filled in
		// and ensures they have default values so they can be used when generating the 
		// launch arguments. 
		// This also makes sure the default launch arguments show up and are visible 
		// when a user tries to edit the server
		try {
			String baseDir = server.getAttribute(IMyTypeServerAttributes.SERVER_BASE_DIR, (String)null); 
			if( baseDir == null || baseDir.trim().length() == 0) {
				String currentHome = server.getAttribute(IMyTypeServerAttributes.SERVER_HOME, (String)null);
				server.setAttribute(IMyTypeServerAttributes.SERVER_BASE_DIR, currentHome);
			}

			// Once we've got some defaults set, we allow the launcher to tell us 
			// what the start arguments and vm-args and environment should look like
			// so we can persist that in the server object, so the user can see it and make
			// changes if they want. 
			CommandLineDetails det = getStartLauncher().getLaunchCommand("run");
			String progArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_PROGRAM_ARGS);
			String vmArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_VM_ARGS);
			if(progArgs == null || progArgs.isEmpty()) {
				progArgs = "";
			}
			if(vmArgs == null || vmArgs.isEmpty()) {
				vmArgs = "";
			}
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_BOOLEAN, false);
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_PROGRAM_ARGS, progArgs);
			server.setAttribute(GenericServerType.JAVA_LAUNCH_OVERRIDE_VM_ARGS, vmArgs);
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
	}
	
	@Override
	public String[] getDeploymentUrls(String strat, String baseUrl, String deployableOutputName, DeployableState ds) {
		// Return a url for a given deployment 
		if( deployableOutputName.equalsIgnoreCase("root.war") || deployableOutputName.equalsIgnoreCase("root")) {
			return new String[] {baseUrl};
		}
		String withSlashes = deployableOutputName.replaceAll("#", "/");
		String ret = baseUrl;
		if( !baseUrl.endsWith("/"))
			ret += "/";
		ret += removeWarSuffix(withSlashes);
		return new String[] { ret };
	}
	protected String removeWarSuffix(String name) {
		String noSuffix = name;
		if( name.toLowerCase().endsWith(".war")) {
			noSuffix = noSuffix.substring(0, noSuffix.length()-4);
		}
		return noSuffix;
	}

	// If tomcat is stopped nad user removes blah.war, We need to delete the exploded folder as well

	protected IPublishControllerWithOptions createPublishController() {
		JSONMemento publishMemento = this.getBehaviorMemento().getChild("publish");
		String deployPath = publishMemento.getString("deployPath");
		String approvedSuffixes = publishMemento.getString("approvedSuffixes");
		String[] suffixes = approvedSuffixes == null ? null : approvedSuffixes.split(",", -1);
		String supportsExploded = publishMemento.getString("supportsExploded");
		boolean exploded = (supportsExploded == null ? false : Boolean.parseBoolean(supportsExploded));
		return new TomcatServerSuffixPublishController(
				getServer(), this, 
				suffixes, deployPath, exploded);
	}
	
	private static class TomcatServerSuffixPublishController extends GenericServerSuffixPublishController {
		public TomcatServerSuffixPublishController(IServer server, IServerDelegate delegate, String[] approvedSuffixes,
				String deploymentPath, boolean supportsExploded) {
			super(server, delegate, approvedSuffixes, deploymentPath, supportsExploded);
		}


		protected int removeModule(DeployableReference reference, 
				int publishRequestType, int modulePublishState) throws CoreException {
			int runState = getServer().getDelegate().getServerState().getState();
			if( runState == IServerDelegate.STATE_STOPPED) {
				// clean up exploded folder from zipped deployment
				Path destPath = getDestinationPath(reference);
				if( destPath.toFile().isFile()) {
					// we are deleting a zip. Need to also delete the exploded folder
					String fname = destPath.getFileName().toString();
					if( fname.contains(".")) {
						int lastDot = fname.lastIndexOf(".");
						String prefix = lastDot == -1 ? fname : fname.substring(0, lastDot);
						Path explodedDirPossible = new File(destPath.getParent().toFile(), prefix).toPath();
						if( explodedDirPossible.toFile().exists()) {
							try {
								super.completeDelete(explodedDirPossible);
							} catch(IOException ioe) {
								// ignore
							}
						}
					}
				}
			}
			return super.removeModule(reference, publishRequestType, modulePublishState);
		}
	}
	
	
	@Override
	public ListServerActionResponse listServerActions() {
		// First get the generic actions we support from servers.json
		ListServerActionResponse resp = getServerActionSupport().listServerActions();
		
		// But lets add a custom 5-step action too
		resp.getWorkflows().add(MyTypeFiveStepAction.getInitialWorkflow(this));
		return resp;
	}
	public WorkflowResponse executeServerAction(ServerActionRequest req) {
		if( req.getActionId().equals(MyTypeFiveStepAction.ACTION_ID)) {
			return new MyTypeFiveStepAction(this).handle(req);
		} else {
			return getServerActionSupport().executeServerAction(req);
		}
	}

}
