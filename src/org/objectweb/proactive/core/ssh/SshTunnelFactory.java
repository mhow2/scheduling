package org.objectweb.proactive.core.ssh;

import org.objectweb.proactive.core.ssh.SshTunnel;
import org.objectweb.proactive.core.ssh.UnusedTunnel;

import org.apache.log4j.Logger;

/**
 * @author mlacage
 *
 * This factory class performs tunnel caching: if a tunnel is not
 * used anymore, it is put in the unused list and whenever a client 
 * requests a similar tunnel, this tunnel is reused. unused tunnels
 * are closed after a timeout determined by proactive.tunneling.gc_timeout
 * (the default value is 10000ms)
 * Caching is performed only if proactive.tunneling.use_gc is set to "yes".
 * Otherwise, tunnels are created and destroyed purely on a need-to basis.
 */
public class SshTunnelFactory {
	private static Logger logger = Logger.getLogger(SshTunnelFactory.class.getName());
	private java.util.Hashtable _unused;
		
	static private SshTunnelFactory _factory = null;
	static private SshTunnelFactory getFactory () {
		if (_factory == null) {
			_factory = new SshTunnelFactory ();
		}
		return _factory;
	}
	static public SshTunnel createTunnel (String host, int port) throws java.io.IOException {
		return getFactory ().create (host, port);
	}
	static public void reportUnusedTunnel (SshTunnel tunnel) throws Exception {
		getFactory ().reportUnused (tunnel);
	}
	
	
	
	private SshTunnelFactory () {
		_unused = new java.util.Hashtable ();
		if (SshParameters.getUseTunnelGC ()) {
			Thread gcThread = new Thread () {
				public void run() {
					while (true) {
						try {
							sleep (1000);
						} catch (InterruptedException e) {}
						getFactory ().GC ();
					}
				}
			};
			gcThread.start ();
		}
	}

	private String getKey (String host, int port) {
		return host + port;
	}
	
	private synchronized SshTunnel create (String host, int port) throws java.io.IOException {
		if (SshParameters.getUseTunnelGC ()) {
			UnusedTunnel unused = (UnusedTunnel)_unused.get (getKey (host, port));
			SshTunnel tunnel;
			if (unused == null) {
				logger.debug ("create tunnel " + host + ":" + port);
				tunnel = new SshTunnel (host, port);
			} else {
				logger.debug ("reuse tunnel " + host + ":" + port);
				_unused.remove (getKey (host, port));
				tunnel = unused.getTunnel ();
			}
			return tunnel;
		} else {
			return new SshTunnel (host, port);
		}
	}
	
	private synchronized void reportUnused (SshTunnel tunnel) throws Exception {
		String host = tunnel.getDistantHost ();
		int port = tunnel.getDistantPort ();
		if (SshParameters.getUseTunnelGC ()) {
			UnusedTunnel prev = (UnusedTunnel)_unused.get (getKey (host, port));
			if (prev != null) {
				prev.getTunnel ().realClose ();
				_unused.remove (getKey (host, port));
			}
			logger.debug ("return unused tunnel " + host + ":" + port);
			_unused.put (getKey (host, port), new UnusedTunnel (tunnel));
		} else {
			logger.debug ("kill unused tunnel " + host + ":" + port);
			tunnel.realClose ();
		}
	}
	
	private synchronized void GC () {
		java.util.Enumeration keys = _unused.keys ();
		for (; keys.hasMoreElements (); ) {
			String key = (String) keys.nextElement ();
			UnusedTunnel tunnel = (UnusedTunnel) _unused.get (key);
			if (tunnel.isOldEnough ()) {
				try {
					SshTunnel sshTunnel = tunnel.getTunnel ();
					logger.debug ("gc kill unused tunnel " + sshTunnel.getDistantHost () + ":" + sshTunnel.getDistantPort ());
					sshTunnel.realClose ();
				} catch (Exception e) {}
				_unused.remove (key);
			}
		}
	}
}
