/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator.model.project

import org.eclipse.xtext.xtext.generator.model.IXtextGeneratorFileSystemAccess

/**
 * Configuration of the runtime project, i.e. the main subproject containing the language definition.
 * 
 * @noimplement This interface should not be implemented by clients.
 */
interface IRuntimeProjectConfig extends IBundleProjectConfig {
	
	def IXtextGeneratorFileSystemAccess getEcoreModel()
	
	def String getEcoreModelFolder()
	
}