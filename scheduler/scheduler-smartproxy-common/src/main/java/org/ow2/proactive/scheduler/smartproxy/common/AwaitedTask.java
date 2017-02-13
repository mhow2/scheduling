/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.smartproxy.common;

import java.io.Serializable;
import java.util.List;

import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;


/**
 * AwaitedTask
 *
 * @author The ProActive Team
 */
public class AwaitedTask implements Serializable {

    private String taskName;

    private List<OutputSelector> outputSelectors;

    private String taskId;

    private boolean transferring = false;

    public AwaitedTask(String taskName, List<OutputSelector> outputSelectors) {

        this.taskName = taskName;
        this.outputSelectors = outputSelectors;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public List<OutputSelector> getOutputSelectors() {
        return outputSelectors;
    }

    public void setOutputSelectors(List<OutputSelector> outputSelectors) {
        this.outputSelectors = outputSelectors;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public boolean isTransferring() {
        return transferring;
    }

    public void setTransferring(boolean transferring) {
        this.transferring = transferring;
    }

}
