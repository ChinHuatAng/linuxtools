/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class IdentityRelationData {
	
	private String type;
	
	private String id;
	
	// for testing purposes only
	public IdentityRelationData (String id, String type) {
		this.id = id;
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}

}
