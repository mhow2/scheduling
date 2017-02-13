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
package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.FileInputStream;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;


public class UploadFileCommand extends AbstractCommand implements Command {
    private String spaceName;

    private String filePath;

    private String fileName;

    private String localFile;

    public UploadFileCommand(String spaceName, String filePath, String fileName, String localFile) {
        this.spaceName = spaceName;
        this.filePath = filePath;
        this.fileName = fileName;
        this.localFile = localFile;
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(localFile);
            boolean uploaded = currentContext.getRestClient().pushFile(currentContext.getSessionId(),
                                                                       spaceName,
                                                                       filePath,
                                                                       fileName,
                                                                       fileStream);
            resultStack(currentContext).push(uploaded);
            if (uploaded) {
                writeLine(currentContext, "%s successfully uploaded.", localFile);
            } else {
                writeLine(currentContext, "Cannot upload the file: %s.", localFile);
            }
        } catch (Exception error) {
            if (fileStream != null) {
                closeQuietly(fileStream);
            }
            handleError(String.format("An error occurred when uploading the file %s. ", localFile),
                        error,
                        currentContext);
        }
    }

}
