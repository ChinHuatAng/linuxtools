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

public class GenericLinksForWorkItem {
	
	private String self;
	
	private String related;
	
	private String editCodebase;
	
	private Object meta;
	
	// for testing purposes only
	public GenericLinksForWorkItem (String self, String related, String editCodebase,
			Object meta) {
		this.self = self;
		this.related = related;
		this.editCodebase = editCodebase;
		this.meta = meta;
	}
	
	public String getSelf() {
		return self;
	}
	
	public String getRelated() {
		return related;
	}
	
	public String getEditCodebase() {
		return editCodebase;
	}
	
	public Object getMeta() {
		return meta;
	}

}
