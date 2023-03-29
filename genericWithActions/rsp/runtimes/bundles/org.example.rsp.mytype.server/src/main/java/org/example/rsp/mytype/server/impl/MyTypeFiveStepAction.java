/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.mytype.server.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.rsp.mytype.server.Activator;
import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowPromptDetails;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyTypeFiveStepAction extends ServerActionWorkflow {
	private static final Logger LOG = LoggerFactory.getLogger(MyTypeFiveStepAction.class);

	public static final String ACTION_LABEL = "MyType 5-step action";
	public static final String ACTION_ID = "mytype5Step";
	private static final String USER_KEY = "user";
	private static final String AGE_KEY = "age";
	private static final String FAV_COLOR_KEY = "favoriteColor";
	
	
	private IServerDelegate myDelegate;

	public static ServerActionWorkflow getInitialWorkflow(IServerDelegate d) {
		WorkflowResponse workflow = new WorkflowResponse();
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_LABEL)));
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_ID, ACTION_LABEL, workflow);
		return action;
	}

	public MyTypeFiveStepAction(IServerDelegate d) {
		this.myDelegate = d;
	}

	public WorkflowResponse handle(ServerActionRequest req) {
		String serverId = req.getServerId();
		if( serverId == null ) {
			return AbstractServerDelegate.cancelWorkflowResponse();
		}
		IServer server = this.myDelegate.getServer().getServerModel().getServer(serverId);
		if( server == null ) {
			return AbstractServerDelegate.cancelWorkflowResponse();
		}
		String user = (String)req.getData().get(USER_KEY);
		if( user == null) {
			long requestId = System.currentTimeMillis();
			WorkflowResponse ret = singlePromptWorkflowResponse(USER_KEY, 
					ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL, 
					ServerManagementAPIConstants.ATTR_TYPE_STRING,
					"What is your name?");
			ret.setRequestId(requestId);
			return ret;
		}
		if( user.equalsIgnoreCase("susan")) {
			WorkflowResponse stat = AbstractServerDelegate.cancelWorkflowResponse();
			Status s = new Status(IStatus.ERROR, Activator.BUNDLE_ID, 0, "No Susans Allowed", null);
			stat.setStatus(StatusConverter.convert(s));
			return stat;
		}
		Object age1 = req.getData().get(AGE_KEY);
		String age = age1 == null ? null : age1.toString();
		if( age == null) {
			return singlePromptWorkflowResponse(AGE_KEY, 
					ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL, 
					ServerManagementAPIConstants.ATTR_TYPE_INT,
					"What is your age?");
		}
		Double d = Double.parseDouble(age);
		if( d < 18) {
			WorkflowResponse stat = AbstractServerDelegate.cancelWorkflowResponse();
			Status s = new Status(IStatus.ERROR, Activator.BUNDLE_ID, 0, "To Young, Go Away", null);
			stat.setStatus(StatusConverter.convert(s));
			return stat;
		}
		
		/*
		 *  The user has passed all prompts. Now you can do one of a few things.
		 */
		if( d < 21 ) {
			/*
			 * Option 1: kick off a thread that does some work in the background, like
			 *  for example edit a configuration file on the server
			 */
			new Thread("FiveActionJob") {
				public void run() {
					System.out.println("I am doing a background job now! No further user input required");
				}
			}.start();
			// Then return 
			return AbstractServerDelegate.okWorkflowResponse();
		}
		
		if( d < 30 ) {
			/*
			 * Option 2: ask the UI to open an editor
			 */
			String home = myDelegate.getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR, (String)null);
			String fileToEdit = home == null ? null : home + "/" + "BUILDING.txt";
			IPath tmpPath = fileToEdit == null ? null : new Path(fileToEdit);
			if(tmpPath == null || !tmpPath.toFile().isFile()) {
				// File doesn't exist, so return a cancel workflow instead
				WorkflowResponse stat = AbstractServerDelegate.cancelWorkflowResponse();
				Status s = new Status(IStatus.ERROR, Activator.BUNDLE_ID, 0, "The config file doesn't exist", null);
				stat.setStatus(StatusConverter.convert(s));
				return stat;
			}
			
			// File exists, so ask UI to open it in an editor
			WorkflowResponseItem item1 = new WorkflowResponseItem();
			item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_EDITOR);
			Map<String,String> propMap = new HashMap<>();
			propMap.put(ServerManagementAPIConstants.WORKFLOW_EDITOR_PROPERTY_PATH, tmpPath.toOSString());
			item1.setProperties(propMap);
			item1.setId(ACTION_ID);
			item1.setLabel(ACTION_LABEL);

			WorkflowResponse workflow = new WorkflowResponse();
			List<WorkflowResponseItem> items = new ArrayList<>();
			workflow.setItems(items);
			items.add(item1);
			workflow.setStatus(StatusConverter.convert(
					new Status(IStatus.OK, Activator.BUNDLE_ID, ACTION_LABEL)));
			return workflow;
		}
		
		if( d < 40 ) {
			/*
			 * Option 3: ask the UI to open a web browser
			 */
			String url = "http://www.example.com";
			WorkflowResponseItem item = new WorkflowResponseItem();
			item.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_BROWSER);
			item.setLabel("Open the following url: " + url);
			item.setContent(url);
			WorkflowResponse resp = new WorkflowResponse();
			resp.setItems(Arrays.asList(item));
			resp.setStatus(StatusConverter.convert(Status.OK_STATUS));
			return resp;
		}
		
		if( d < 50 ) {
			/*
			 * Option 4: Ask the UI to open a terminal with a command
			 */
			WorkflowResponseItem item1 = new WorkflowResponseItem();
			item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_TERMINAL);
			Map<String,String> propMap = new HashMap<>();
			propMap.put(ServerManagementAPIConstants.WORKFLOW_TERMINAL_CMD, "ls /home");
			item1.setProperties(propMap);
			item1.setId("5steps-open-terminal");
			item1.setLabel("Run this command");
			WorkflowResponse resp = new WorkflowResponse();
			resp.setItems(Arrays.asList(item1));
			resp.setStatus(StatusConverter.convert(
					new Status(IStatus.OK, Activator.BUNDLE_ID, "Run this command")));
			return resp;

		}
		return AbstractServerDelegate.okWorkflowResponse();
	}
	
	private WorkflowResponse singlePromptWorkflowResponse(String id, String itemType, 
			String promptType, String label) {
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setId(id);
		item1.setItemType(itemType);
		item1.setLabel(label);
		List<WorkflowResponseItem> items = new ArrayList<>();
		items.add(item1);
		WorkflowPromptDetails prompt = new WorkflowPromptDetails();
		prompt.setResponseType(promptType);
		item1.setPrompt(prompt);
		WorkflowResponse workflow = new WorkflowResponse();
		workflow.setItems(items);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_LABEL)));
		return workflow;
	}
	
}
