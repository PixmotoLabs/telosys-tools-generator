/**
 *  Copyright (C) 2008-2017  Telosys project org. ( http://www.telosys.org/ )
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.telosys.tools.generator.task;

/**
 * Default ITaskMonitor implementation
 * 
 * @author Laurent Guerin
 *
 */
public class DefaultTaskMonitor implements ITaskMonitor {

	private boolean canceled = false ;
	
	public DefaultTaskMonitor() {
		super();
	}

	@Override
	public void beginTask(String arg0, int arg1) {
		// Nothing to do 
	}

	@Override
	public void done() {
		// Nothing to do 
	}

	@Override
	public boolean isCanceled() {
		return canceled ;
	}

	@Override
	public void setCanceled(boolean v) {
		canceled = v ;
	}

	@Override
	public void setTaskName(String arg0) {
		// Nothing to do 
	}

	@Override
	public void subTask(String arg0) {
		// Nothing to do 
	}

	@Override
	public void worked(int arg0) {
		// Nothing to do 
	}

}
