/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class WorkItemTypeData implements IdNamed {
	
	private String type;
	
	private String id;
	
	private WorkItemTypeAttributes attributes;
	
	private WorkItemTypeRelationships relationships;
	
	// for testing purposes only
	public WorkItemTypeData (String id, String type, WorkItemTypeAttributes attributes,
			WorkItemTypeRelationships relationships) {
		this.id = id;
		this.type = type;
		this.attributes = attributes;
		this.relationships = relationships;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return attributes.getName();
	}
	
	public String getType() {
		return type;
	}
	
	public WorkItemTypeAttributes getWorkItemTypeAttributes() {
		return attributes;
	}
	
	public WorkItemTypeRelationships getWorkItemTypeRelationships() {
		return relationships;
	}
	
}
