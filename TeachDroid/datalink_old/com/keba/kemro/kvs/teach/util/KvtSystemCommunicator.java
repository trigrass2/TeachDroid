/*-------------------------------------------------------------------------
 *                   (c) 1999 by KEBA Ges.m.b.H & Co
 *                            Linz/AUSTRIA
 *                         All rights reserved
 *--------------------------------------------------------------------------
 *    Projekt   : KEMRO.teachview.4
 *    Auftragsnr: 5500395
 *    Erstautor : ede
 *    Datum     : 01.04.2003
 *--------------------------------------------------------------------------
 *      Revision:
 *        Author:
 *          Date:
 *------------------------------------------------------------------------*/
package com.keba.kemro.kvs.teach.util;

import java.util.Vector;

import com.keba.kemro.kvs.teach.data.project.KvtProjectAdministrator;
import com.keba.kemro.teach.dfl.KTcDfl;
import com.keba.kemro.teach.network.TcClient;
import com.keba.kemro.teach.network.TcConnectionListener;
import com.keba.kemro.teach.network.TcConnectionManager;

/**
 * Class KSystemCommunicator
 */
public class KvtSystemCommunicator {
	/** Field AS_CONTROLLER */
	private static boolean									WRITE_ACCESS_ALLOWED	= false;

	/** Field m_connectionListeners */
	private static Vector<KvtTeachviewConnectionListener>	m_connectionListeners	= new Vector<KvtTeachviewConnectionListener>(10);
	/** Field m_hostName */
	private static String									m_hostName;
	private static String									clientID;

	private static KTcDfl									dfl;

	public static KTcDfl getTcDfl() {
		return dfl;
	}

	/**
	 * Verbindungsaufbau zum Laufzeitsystem
	 * 
	 * @param hostName
	 *            Name des Hosts
	 * @param timeout
	 *            Timeout int ms
	 * @param _progressPublisher
	 * @param _progressPublisher
	 * @param automaticReconnect
	 *            Automatisches Reconnect bei Verbindungsabbruch
	 */
	public static void connect(String hostName, int timeout, String globalFilter) {
		m_hostName = hostName;

		TcClient client = null; // TcConnectionManager.getTcClient("Teachview",m_hostName);

		int i = 0;
		while ((client == null) && (i < 100)) { // 20 seconds connect timeout
			try {
				Thread.sleep(200);
			} catch (InterruptedException ie) {
			}
			i++;
			client = TcConnectionManager.getTcClient("TeachDroid", m_hostName);

		}
		if (client != null) {
			client.setTimeout(timeout);
			client.setUserMode(false);
			client.setWriteAccess(WRITE_ACCESS_ALLOWED);
			clientID = client.getClientID();
			dfl = new KTcDfl(client, globalFilter);
			client.addConnectionListener(new KTcConnectionListener());

			fireConnected();

		}
	}

	public static String getHostname() {
		return m_hostName;
	}

	public static boolean connectOnce(String _host, int _to, String _globalFilter) {
		m_hostName = _host;
		TcClient client = null;

		client = TcConnectionManager.getTcClient("Teachview", m_hostName);
		if (client != null) {
			client.setTimeout(_to);
			client.setUserMode(false);
			client.setWriteAccess(true);
			clientID = client.getClientID();
			dfl = new KTcDfl(client, _globalFilter);
			Log.i("TC connection", "Adding connection listener");
			client.addConnectionListener(new KTcConnectionListener());

			// new Thread(new Runnable() {
			//
			// public void run() {
			// Log.i("TC connection", "connection state update");
			// fireConnected();
			// KvtProjectAdministrator.reloadProjectList();
			// }
			// }, "Connection state notifier thread").start();
			Log.i("TC connection", "about to fireConnected()");
			fireConnected();
			KvtProjectAdministrator.reloadProjectList();
			return true;
		}

		return false;
	}

	public static String getClientID() {
		return clientID;
	}

	public static void shutdown() {
		KTcDfl tcDfl = dfl;
		if (tcDfl != null) {
			tcDfl.disconnect();
			dfl = null;
		}
	}

	/**
	 * Verbindung wird abbgebaut
	 */
	public static void disconnect() {
		KTcDfl tcDfl = dfl;
		if (tcDfl != null) {
			tcDfl.client.disconnect();
			dfl = null;
			fireDisconnected();
		}
	}

	public static void close() {
		KTcDfl tcDfl = dfl;
		if (tcDfl != null) {
			tcDfl.client.close();
			fireDisconnected();
			dfl = null;
		}
	}

	/**
	 * Liefert true wenn eine Verbindung besteht
	 * 
	 * @return true wenn eine Verbindung besteht
	 */
	public static boolean isConnected() {
		return dfl != null;
	}

	/**
	 * sets o removes write access
	 * 
	 * @param writeAccessAllowed
	 *            true if write access is granted
	 */
	public static void setAccessMode(boolean writeAccessAllowed) {
		WRITE_ACCESS_ALLOWED = writeAccessAllowed;
		KTcDfl tcDfl = dfl;
		if (tcDfl != null) {
			tcDfl.client.setWriteAccess(writeAccessAllowed);
		}
	}

	/**
	 * F�gt einen Verbindungslistener hinzu.
	 * 
	 * @param listener
	 *            Verbindungslistener
	 */
	public static void addConnectionListener(KvtTeachviewConnectionListener listener) {
		if (m_connectionListeners == null)
			m_connectionListeners = new Vector<KvtTeachviewConnectionListener>();

		if (!m_connectionListeners.contains(listener)) {
			m_connectionListeners.addElement(listener);
		}
	}

	/**
	 * Entfernt einen Verbindungslistener
	 * 
	 * @param listener
	 *            Verbindungslistener
	 */
	public static void removeConnectionListener(KvtTeachviewConnectionListener listener) {
		m_connectionListeners.removeElement(listener);
	}

	private static void fireConnected() {
		for (int i = 0; i < m_connectionListeners.size(); i++) {
			try {
				long start = System.currentTimeMillis();

				KvtTeachviewConnectionListener listener = (KvtTeachviewConnectionListener) (m_connectionListeners.elementAt(i));
				String classname = listener.getClass().toString().substring(listener.getClass().toString().lastIndexOf("."));
				listener.teachviewConnected();
				long dur = System.currentTimeMillis() - start;

				Log.i("KvtSystemCommunicator", "notifying listener " + classname + " took "
						+ dur
						+ " ms");

			} catch (Exception ex) {
				Log.e(KvtSystemCommunicator.class.toString(),
						"Error in Call of KTeachviewConnectionListener.connected " + m_connectionListeners.elementAt(i) + " Excp: " + ex);
			}

		}
		Log.i("KvtSystemCommunicator", "notifying listeners done");
	}

	private static void fireDisconnected() {
		for (int i = 0; i < m_connectionListeners.size(); i++) {
			try {
				((KvtTeachviewConnectionListener) (m_connectionListeners.elementAt(i))).teachviewDisconnected();
			} catch (Exception ex) {
				Log.e(KvtSystemCommunicator.class.toString(), "Error in Call of KTeachviewConnectionListener.disconnected "
						+ m_connectionListeners.elementAt(i) + " Excp: " + ex);
			}
		}
	}

	private static class KTcConnectionListener implements TcConnectionListener {
		/**
		 * @see com.keba.kemro.teach.network.TcConnectionListener#connectionStateChanged(boolean)
		 */
		public void connectionStateChanged(boolean isConnected) {
			if (isConnected) {
			} else {
				if (dfl != null) {
					Log.i(getClass().toString(), "Disconnected to TeachControl - Client ID: " + clientID);
					dfl = null;
					fireDisconnected();
				}
			}
		}
	}
}
