/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.mytype.server.impl;

import org.jboss.tools.rsp.api.DefaultServerAttributes;

public interface IMyTypeServerAttributes extends DefaultServerAttributes {
	public static final String MYTYPE_PREFIX = "org.example.rsp.mytype.";
	public static final String MYTYPE_90_SERVER_TYPE_ID = MYTYPE_PREFIX + "90";
	

	/*
	 * Required attributes
	 */
	public static final String SERVER_HOME = DefaultServerAttributes.SERVER_HOME_DIR;

	/*
	 * Optional attributes
	 */
	public static final String SERVER_BASE_DIR = "server.base.dir";
}
