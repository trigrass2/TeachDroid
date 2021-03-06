/**
 * 
 */
package com.keba.kemro.kvs.teach.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.Vector;

import com.keba.kemro.kvs.teach.model.DataModel;
import com.keba.kemro.teach.dfl.KTcDfl;
import com.keba.kemro.teach.dfl.value.KStructVarWrapper;
import com.keba.kemro.teach.dfl.value.KVariableGroup;
import com.keba.kemro.teach.dfl.value.KVariableGroupListener;

/**
 * @author ltz
 * 
 */
public class KvtPositionMonitor implements KVariableGroupListener, KvtTeachviewConnectionListener {

	private static KvtPositionMonitor mInstance;
	private KTcDfl mDfl;
	private KVariableGroup mVarGroup;
	private final String mAxisNameVarnameStub = "_system.gRcSelectedRobotData.axesName[{0}]";
	private final String mAxisPosValueVarnameStub = "_system.gRcSelectedRobotData.axisPosValue[{0}]";

	private final String mCartPosNameVarnameStub = "_system.gRcSelectedRobotData.cartCompName[{0}]";
	private final String mCartPosVarVarnameStub = "_system.gRcSelectedRobotData.worldPosValue[{0}]";
	private final String mRefsysVarnameStub = "_system.gRcRefSysInstPaths[{0}]";
	private final String mRefsysReadyVarnameStub = "_system.gRcRefSysReadyForCalculation[{0}]";
	private final String mToolsVarnameStub = "_system.gRcTools[{0}].name";
	private final String mCartVelVarname = "_system.gRcSelectedRobotData.cartPathVel";
	private final String mSelToolName = "_system.gRcSelectedRobotData.selectedToolName";
	private final String mSelRefsysVarname = "_system.gRcSelectedRobotData.selectedRefSysName";
	private final String mChosenRefsysVarname = "_system.gRcSelectedRobotData.chosenRefSys.sInstanceName";
	private final String mChosenToolVarname = "_system.gRcSelectedRobotData.chosenTool.toolName";

	private final String mOverrideVarname = "_system.gRcData.override";

	private List<KStructVarWrapper> mAxisPositionVars = new Vector<KStructVarWrapper>();
	private List<KStructVarWrapper> mNameVars = new Vector<KStructVarWrapper>();

	private List<KStructVarWrapper> mCartPosVars = new Vector<KStructVarWrapper>();
	private List<KStructVarWrapper> mCartNameVars = new Vector<KStructVarWrapper>();
	private List<KStructVarWrapper> mRefsysVars = new Vector<KStructVarWrapper>();
	private List<KStructVarWrapper> mToolVars = new Vector<KStructVarWrapper>();
	private KStructVarWrapper mOverrideVar;
	private KStructVarWrapper mCartVelVar;
	private KStructVarWrapper mSelectedRefSysVar;
	private KStructVarWrapper mChosenRefSysVar;
	private KStructVarWrapper mChosenToolVar;
	private KStructVarWrapper mSelectedToolVar;

	private List<Float> mCartPos = new Vector<Float>();
	private List<Float> mAxisPos = new Vector<Float>();
	private List<String> mTools = new Vector<String>();
	private List<String> mRefsys = new Vector<String>();
	private String mChosenRefsys;
	private String mChosenTool;
	protected DataModel mRefsysmodel;
	protected DataModel mToolmodel;
	private int mOvr;
	private float mCartVel;
	private String mSelectedTool;
	private String mSelectedRefsys;

	private static List<KvtPositionMonitorListener> mListeners = new Vector<KvtPositionMonitor.KvtPositionMonitorListener>();
	private static List<KvtOverrideChangedListener> mOverrideListeners = new Vector<KvtPositionMonitor.KvtOverrideChangedListener>();
	private static Object mInstancelock = new Object();

	public static void init() {
		mInstance = new KvtPositionMonitor();
		KvtSystemCommunicator.addConnectionListener(mInstance);
	}

	protected KvtPositionMonitor() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.keba.kemro.teach.dfl.value.KVariableGroupListener#changed(com.keba
	 * .kemro.teach.dfl.value.KStructVarWrapper)
	 */
	public void changed(KStructVarWrapper _variable) {
		int index = mAxisPositionVars.indexOf(_variable);
		if (index >= 0) {
			String name = mNameVars.get(index).readActualValue(null).toString();
			mAxisPos.remove(index);
			mAxisPos.add(index, (Float) mAxisPositionVars.get(index).readActualValue(null));
			for (KvtPositionMonitorListener l : mListeners)
				l.axisPositionChanged(index, (Number) _variable.readActualValue(null), name);

			// Log.d("KvtPositionMonitor", "Axis " + (index + 1) + " [" + name +
			// "] has position " + _variable.getActualValue());
			return;
		}

		index = mCartPosVars.indexOf(_variable);
		if (index >= 0) {
			String name = mCartNameVars.get(index).readActualValue(null).toString();
			mCartPos.remove(index);
			mCartPos.add(index, (Float) mCartPosVars.get(index).readActualValue(null));
			for (KvtPositionMonitorListener l : mListeners)
				l.cartesianPositionChanged(index, name, (Number) _variable.readActualValue(null));

			// Log.d("KvtPositionMonitor", "Component " + name + ": " +
			// _variable.getActualValue());
			return;
		}

		if (_variable.equals(mOverrideVar)) {
			int ovt = ((Number) _variable.readActualValue(null)).intValue();
			mOvr = ovt / 10;
			for (KvtOverrideChangedListener l : mOverrideListeners)
				l.overrideChanged(mOvr);

		} else if (_variable.equals(mCartVelVar)) {
			mCartVel = (Float) _variable.readActualValue(null);
			for (KvtPositionMonitorListener l : mListeners)
				l.pathVelocityChanged(mCartVel);

		} else if (_variable.equals(mSelectedRefSysVar)) {
			Object v = _variable.readActualValue(null);
			if (mSelectedRefsys == null)
				mSelectedRefsys = new String();
			if (v != null && v instanceof String) {
				mSelectedRefsys = (String) v;
				for (KvtPositionMonitorListener l : mListeners)
					l.selectedRefSysChanged(mSelectedRefsys);

			}
		} else if (_variable.equals(mSelectedToolVar)) {
			Object v = _variable.getActualValue();
			if (v != null && v instanceof String) {
				mSelectedTool = (String) v;
				for (KvtPositionMonitorListener l : mListeners)
					l.selectedToolChanged(mSelectedTool);
			}
		} else if (_variable.equals(mChosenRefSysVar)) {
			Object val = _variable.getActualValue();
			if (mChosenRefsys == null)
				mChosenRefsys = new String();

			if (val != null && !mChosenRefsys.equals(val)) {
				mChosenRefsys = val.toString();
			}
			for (KvtPositionMonitorListener l : mListeners)
				l.jogRefsysChanged(mChosenRefsys);

		} else if (_variable.equals(mChosenToolVar)) {
			Object v = _variable.getActualValue();
			if (mChosenTool == null)
				mChosenTool = new String();
			if (v != null && v instanceof String) {
				mChosenTool = (String) v;
				for (KvtPositionMonitorListener l : mListeners)
					l.jogToolChanged(mChosenTool);
			}
		} else {
			System.out.println(_variable.getRootPathString() + ": " + _variable.readActualValue(null));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.keba.kemro.teach.dfl.value.KVariableGroupListener#allActualValuesUpdated
	 * ()
	 */
	public void allActualValuesUpdated() {
		// for (KvtPositionMonitorListener l : mListeners) {
		// for (int i = 0; i < mAxisPositionVars.size(); i++) {
		// changed(mAxisPositionVars.get(i));
		// }
		// for (int i = 0; i < mCartPosVars.size(); i++) {
		// changed(mCartPosVars.get(i));
		// }
		// changed(mCartVelVar);
		// changed(mSelectedRefSysVar);
		// changed(mSelectedToolVar);
		// changed(mOverrideVar);
		// }
	}

	protected int getNumAxes() {
		return 6;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keba.kemro.kvs.teach.util.KvtTeachviewConnectionListener#
	 * teachviewConnected()
	 */
	public void teachviewConnected() {
		mDfl = KvtSystemCommunicator.getTcDfl();
		if (mDfl != null) {

			if (mVarGroup != null)
				mVarGroup.release();

			// create variable group
			mVarGroup = mDfl.variable.createVariableGroup("KvtPositionMonitor");
			mVarGroup.addListener(this);

			// create variable wrappers
			createAxisPosVariables();
			createCartVariables();
			createOverrideVariables();

			// activate
			mVarGroup.setPollInterval(100);
			mVarGroup.activate();

		}
	}

	/**
	 * 
	 */
	public static void buildModels() {

		new Thread(new Runnable() {
			public void run() {
				synchronized (mInstancelock) {

					// TODO: Move this to a setter method
					// create data models for the refsys and the tool
					if (mInstance.mRefsysmodel == null)
						mInstance.mRefsysmodel = DataModel.createMapToModel(mInstance.mChosenRefSysVar, null, null);
					if (mInstance.mToolmodel == null)
						mInstance.mToolmodel = DataModel.createMapToModel(mInstance.mChosenToolVar, null, null);
					System.out.println("notifyAll()!");
					mInstancelock.notifyAll();
				}
				System.out.println("Models for tool and refsys built!");
			}
		}, "ModelBuilderThread").start();
	}

	/**
	 * 
	 */
	private void createCartVariables() {
		for (int i = 0; i < 6; i++) {
			// cartesian component name vars
			String compVar = MessageFormat.format(mCartPosNameVarnameStub, i);
			KStructVarWrapper w1 = mDfl.variable.createKStructVarWrapper(compVar);
			if (w1 != null) {
				mCartNameVars.add(w1);
				mVarGroup.add(w1);
			} else
				System.err.println("Variable " + compVar + " not created!");

			// cartesian position vars
			String posVar = MessageFormat.format(mCartPosVarVarnameStub, i);
			KStructVarWrapper w2 = mDfl.variable.createKStructVarWrapper(posVar);
			if (w2 != null) {
				mCartPosVars.add(w2);
				mCartPos.add((Float) w2.readActualValue(null));
				mVarGroup.add(w2);
			} else {
				System.err.println("Variable " + posVar + " not created!");
			}

		}

		mCartVelVar = mDfl.variable.createKStructVarWrapper(mCartVelVarname);
		if (mCartVelVar != null) {
			mVarGroup.add(mCartVelVar);
			mCartVel = (Float) mCartVelVar.readActualValue(null);
		}

		mChosenRefSysVar = mDfl.variable.createKStructVarWrapper(mChosenRefsysVarname);
		if (mChosenRefSysVar != null) {
			mVarGroup.add(mChosenRefSysVar);
			mChosenRefsys = (String) mChosenRefSysVar.readActualValue(null);
		}

		mChosenToolVar = mDfl.variable.createKStructVarWrapper(mChosenToolVarname);
		if (mChosenToolVar != null) {
			mVarGroup.add(mChosenToolVar);
			mChosenTool = (String) mChosenToolVar.readActualValue(null);
		}

		mSelectedRefSysVar = mDfl.variable.createKStructVarWrapper(mSelRefsysVarname);
		if (mSelectedRefSysVar != null) {
			mVarGroup.add(mSelectedRefSysVar);
			mSelectedRefsys = (String) mSelectedRefSysVar.readActualValue(null);
		}

		mSelectedToolVar = mDfl.variable.createKStructVarWrapper(mSelToolName);
		if (mSelectedToolVar != null) {
			mVarGroup.add(mSelectedToolVar);
			mSelectedTool = (String) mSelectedToolVar.readActualValue(null);
		}
	}

	/**
	 * 
	 */
	private void createAxisPosVariables() {
		// create variables
		int numAxes = getNumAxes();
		for (int i = 0; i < numAxes; i++) {

			// position variable
			String posVar = MessageFormat.format(mAxisPosValueVarnameStub, i);
			KStructVarWrapper wrpP = mDfl.variable.createKStructVarWrapper(posVar);
			if (wrpP != null) {
				mAxisPositionVars.add(wrpP);
				mAxisPos.add((Float) wrpP.readActualValue(null));
				mVarGroup.add(wrpP);
			}

			// name variable
			String nameVar = MessageFormat.format(mAxisNameVarnameStub, i);
			KStructVarWrapper wrpN = mDfl.variable.createKStructVarWrapper(nameVar);
			if (wrpN != null) {
				mNameVars.add(wrpN);
				mVarGroup.add(wrpN);
			}
		}
	}

	private void createOverrideVariables() {

		KStructVarWrapper wrp = mDfl.variable.createKStructVarWrapper(mOverrideVarname);
		if (wrp != null) {
			mOverrideVar = wrp;
			mOvr = (Integer) wrp.readActualValue(null) / 10;
			mVarGroup.add(wrp);
		}

		int i = 0;
		String toolVar = MessageFormat.format(mToolsVarnameStub, i);
		KStructVarWrapper wrapperTool = mDfl.variable.createKStructVarWrapper(toolVar);
		while ((String) wrapperTool.readActualValue(null) != "") {
			mToolVars.add(wrapperTool);
			mVarGroup.add(wrapperTool);
			mTools.add((String) wrapperTool.readActualValue(null));
			i++;
			toolVar = MessageFormat.format(mToolsVarnameStub, i);
			wrapperTool = mDfl.variable.createKStructVarWrapper(toolVar);
		}

		i = 0;
		String refsysVar = MessageFormat.format(mRefsysVarnameStub, i);
		KStructVarWrapper wrapperRefsys = mDfl.variable.createKStructVarWrapper(refsysVar);
		String readyVar = MessageFormat.format(mRefsysReadyVarnameStub, i);
		KStructVarWrapper wrapperReady = mDfl.variable.createKStructVarWrapper(readyVar);
		while ((String) wrapperRefsys.readActualValue(null) != "") {
			if ((Boolean) wrapperReady.readActualValue(null)) {
				mRefsysVars.add(wrapperRefsys);
				mVarGroup.add(wrapperRefsys);
				String actRefsys = ((String) wrapperRefsys.readActualValue(null));
				int indexOfPoint = actRefsys.indexOf('.') + 1;
				mRefsys.add(actRefsys.substring(indexOfPoint));
			}
			i++;
			refsysVar = MessageFormat.format(mRefsysVarnameStub, i);
			wrapperRefsys = mDfl.variable.createKStructVarWrapper(refsysVar);
			readyVar = MessageFormat.format(mRefsysReadyVarnameStub, i);
			wrapperReady = mDfl.variable.createKStructVarWrapper(readyVar);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keba.kemro.kvs.teach.util.KvtTeachviewConnectionListener#
	 * teachviewDisconnected()
	 */
	public void teachviewDisconnected() {

		mAxisPositionVars.clear();
		mNameVars.clear();

		mVarGroup.release();
		mVarGroup.reset();

	}

	public synchronized static void addListener(KvtPositionMonitorListener _listener) {
		if (mListeners == null)
			mListeners = new Vector<KvtPositionMonitor.KvtPositionMonitorListener>();
		if (!mListeners.contains(_listener))
			mListeners.add(_listener);
	}

	public synchronized static void addListener(KvtOverrideChangedListener _listener) {
		if (mOverrideListeners == null)
			mOverrideListeners = new Vector<KvtPositionMonitor.KvtOverrideChangedListener>();
		if (!mOverrideListeners.contains(_listener))
			mOverrideListeners.add(_listener);
	}

	public synchronized static void removeListener(KvtPositionMonitorListener _listener) {
		mListeners.remove(_listener);
	}

	public synchronized static void removeListener(KvtOverrideChangedListener _listener) {
		mOverrideListeners.remove(_listener);
	}

	public static interface KvtOverrideChangedListener {
		public void overrideChanged(Number _override);
	}

	public static interface KvtPositionMonitorListener {

		public void cartesianPositionChanged(int _compNo, String _compName, Number _value);

		/**
		 * Called when the tool setting used for manual jogging has changed.
		 * This does not affect automatic programs!
		 * 
		 * @param _jogTool
		 *            The name of the now selected jog tool.
		 */
		public void jogToolChanged(String _jogTool);

		/**
		 * Called then the cartesian system of reference used for manual jogging
		 * has been changed. Note that this does not affect automatic programs!
		 * This method may also be invoked when an automatic program starts.
		 * 
		 * @param _jogRefsys
		 *            The name of the now selected frame of reference
		 */
		public void jogRefsysChanged(String _jogRefsys);

		/**
		 * @param _velocityMms
		 */
		public void pathVelocityChanged(float _velocityMms);

		public void axisPositionChanged(int axisNo, Number _value, String _axisName);

		/**
		 * Called when the robot's geometric frame of reference has changed from
		 * within an automatic program.
		 * 
		 * @param _mChosenRefSys
		 *            The name of the new reference system
		 */
		void selectedRefSysChanged(String _refsysName);

		/**
		 * Called when the chosen tool of the robot has changed from within an
		 * automatic program
		 * 
		 * @param _mChosenTool
		 *            The name of the new tool
		 */
		void selectedToolChanged(String _toolName);
	}

	public static List<Float> getCartesianPositions() {
		return mInstance.mCartPos;
	}

	public static List<Float> getAxisPositions() {
		return mInstance.mAxisPos;
	}

	public static List<String> getTools() {
		return mInstance.mTools;
	}

	public static List<String> getRefSysList() {
		return mInstance.mRefsys;
	}

	public static float getPathVelocity() {
		return mInstance.mCartVel;
	}

	public static int getOverride() {
		return mInstance.mOvr;
	}

	public static String getChosenRefSys() {
		return mInstance.mChosenRefsys;
	}

	public static String getChosenTool() {
		return mInstance.mChosenTool;
	}

	public static String getJogRefSys() {
		return mInstance.mSelectedRefsys;
	}

	public static String getJogTool() {
		return mInstance.mSelectedTool;
	}

	/**
	 * @param _value
	 */
	public static void setOverride(int _value) {
		if (mInstance.mOverrideVar != null)
			mInstance.mOverrideVar.setActualValue(_value * 10);
	}

	/**
	 * @return
	 * 
	 */
	public static List<?> getAvailableRefsys() {
		synchronized (mInstancelock) {
			try {
				if (mInstance.mRefsysmodel == null) {
					System.out.println("refsys wait()");
					mInstancelock.wait();
				}
				return mInstance.mRefsysmodel.getData();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	public synchronized static List<?> getAvailableTools() {
		synchronized (mInstancelock) {
			try {
				if (mInstance.mToolmodel == null) {
					System.out.println("tool wait()");
					mInstancelock.wait();
				}
				return mInstance.mToolmodel.getData();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
