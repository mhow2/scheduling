/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s):
*
* ################################################################
*/
package org.objectweb.proactive.ext.locationserver;

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;


public class RequestWithLocationServer extends RequestImpl
    implements java.io.Serializable {
    private static final int MAX_TRIES = 30;
    static Logger logger = Logger.getLogger(RequestWithLocationServer.class.getName());

    /**
 * the number of time we try before reporting a failure
 */
    private int tries;
    private transient LocationServer server;

    public RequestWithLocationServer(MethodCall methodCall,
        UniversalBody sender, boolean isOneWay, long nextSequenceID,
        LocationServer server) {
        super(methodCall, sender, isOneWay, nextSequenceID);
        this.server = server;
    }

    public Reply serve(Body targetBody) throws ServeException {
        Reply r = super.serve(targetBody);
        return r;
    }

    protected void sendRequest(UniversalBody destinationBody)
        throws java.io.IOException {
        try {
            //   startTime = System.currentTimeMillis();
            destinationBody.receiveRequest(this);

            //    long endTime = System.currentTimeMillis();
        } catch (Exception e) {
            this.backupSolution(destinationBody);
        }
    }

    /**
 * Implements the backup solution
 */
    protected void backupSolution(UniversalBody destinationBody)
        throws java.io.IOException {
        boolean ok = false;
        tries = 0;
        //get the new location from the server
        UniqueID bodyID = destinationBody.getID();
        while (!ok && (tries < MAX_TRIES)) {
            UniversalBody remoteBody = null;
            UniversalBody mobile = queryServer(bodyID);

            //we want to bypass the stub/proxy
            remoteBody = (UniversalBody) ((FutureProxy) ((StubObject) mobile).getProxy()).getResult();
            try {
                remoteBody.receiveRequest(this);

                //everything went fine, we have to update the current location of the object
                //so that next requests don't go through the server
                if (sender != null) {
                    sender.updateLocation(bodyID, remoteBody);
                } else {
                    LocalBodyStore.getInstance().getLocalBody(getSourceBodyID())
                                  .updateLocation(bodyID, remoteBody);
                }
                ok = true;
            } catch (Exception e) {
                logger.debug(
                    "RequestWithLocationServer:  .............. FAILED = " +
                    " for method " + methodName);
                tries++;
            }
        }
    }

    protected UniversalBody queryServer(UniqueID bodyID) {
        if (server == null) {
            server = LocationServerFactory.getLocationServer();
        }
        UniversalBody mobile = (UniversalBody) server.searchObject(bodyID);

        logger.debug(
            "RequestWithLocationServer: backupSolution() server has sent an answer");

        ProActive.waitFor(mobile);
        return mobile;
    }
}
