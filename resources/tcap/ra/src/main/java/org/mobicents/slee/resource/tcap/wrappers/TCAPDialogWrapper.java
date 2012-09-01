package org.mobicents.slee.resource.tcap.wrappers;

import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.protocols.ss7.tcap.api.TCAPException;
import org.mobicents.protocols.ss7.tcap.api.TCAPSendException;
import org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog;
import org.mobicents.protocols.ss7.tcap.api.tc.dialog.TRPseudoState;
import org.mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCBeginRequest;
import org.mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCContinueRequest;
import org.mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCEndRequest;
import org.mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCUniRequest;
import org.mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCUserAbortRequest;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextName;
import org.mobicents.protocols.ss7.tcap.asn.UserInformation;
import org.mobicents.protocols.ss7.tcap.asn.comp.Component;
import org.mobicents.slee.resource.tcap.TCAPDialogActivityHandle;
import org.mobicents.slee.resource.tcap.TCAPResourceAdaptor;
import org.mobicents.slee.resource.tcap.events.TCAPEvent;

/**
 * 
 * @author amit bhayani
 * 
 */
public class TCAPDialogWrapper implements Dialog, TCAPEvent {

	private TCAPDialogActivityHandle activityHandle;
	private final TCAPResourceAdaptor ra;
	private Dialog wrappedDialog;
	
	private boolean keepedTimeout;

	/**
	 * @param activityHandle
	 * @param ra
	 * @param wrappedDialog
	 */
	public TCAPDialogWrapper(TCAPDialogActivityHandle activityHandle, TCAPResourceAdaptor ra, Dialog wrappedDialog) {
		super();
		this.activityHandle = activityHandle;
		this.ra = ra;
		this.wrappedDialog = wrappedDialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#cancelInvocation
	 * (java.lang.Long)
	 */
	@Override
	public boolean cancelInvocation(Long invokeId) throws TCAPException {
		return this.wrappedDialog.cancelInvocation(invokeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#
	 * getApplicationContextName()
	 */
	@Override
	public ApplicationContextName getApplicationContextName() {
		return this.wrappedDialog.getApplicationContextName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getDataLength(org
	 * .mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCBeginRequest)
	 */
	@Override
	public int getDataLength(TCBeginRequest event) throws TCAPSendException {
		return this.wrappedDialog.getDataLength(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getDataLength(org
	 * .mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCContinueRequest)
	 */
	@Override
	public int getDataLength(TCContinueRequest event) throws TCAPSendException {
		return this.wrappedDialog.getDataLength(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getDataLength(org
	 * .mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCEndRequest)
	 */
	@Override
	public int getDataLength(TCEndRequest event) throws TCAPSendException {
		return this.wrappedDialog.getDataLength(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getDataLength(org
	 * .mobicents.protocols.ss7.tcap.api.tc.dialog.events.TCUniRequest)
	 */
	@Override
	public int getDataLength(TCUniRequest event) throws TCAPSendException {
		return this.wrappedDialog.getDataLength(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getDialogId()
	 */
	@Override
	public Long getDialogId() {
		return this.wrappedDialog.getDialogId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getLocalAddress()
	 */
	@Override
	public SccpAddress getLocalAddress() {
		return this.wrappedDialog.getLocalAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getMaxUserDataLength
	 * ()
	 */
	@Override
	public int getMaxUserDataLength() {
		return this.wrappedDialog.getMaxUserDataLength();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getNewInvokeId()
	 */
	@Override
	public Long getNewInvokeId() throws TCAPException {
		return this.wrappedDialog.getNewInvokeId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getRemoteAddress()
	 */
	@Override
	public SccpAddress getRemoteAddress() {
		return this.wrappedDialog.getRemoteAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getState()
	 */
	@Override
	public TRPseudoState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getUserInformation
	 * ()
	 */
	@Override
	public UserInformation getUserInformation() {
		return this.wrappedDialog.getUserInformation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#getUserObject()
	 */
	@Override
	public Object getUserObject() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#isEstabilished()
	 */
	@Override
	public boolean isEstabilished() {
		return this.wrappedDialog.isEstabilished();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#isStructured()
	 */
	@Override
	public boolean isStructured() {
		return this.wrappedDialog.isStructured();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#keepAlive()
	 */
	@Override
	public void keepAlive() {
		this.keepedTimeout = true;
	}

	public void startDialogTimeoutProc() {
		this.keepedTimeout = false;
	}

	public boolean checkDialogTimeoutProcKeeped() {
		return this.keepedTimeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#release()
	 */
	@Override
	public void release() {
		this.wrappedDialog.release();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#resetTimer(java
	 * .lang.Long)
	 */
	@Override
	public void resetTimer(Long invokeId) throws TCAPException {
		this.wrappedDialog.resetTimer(invokeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#send(org.mobicents
	 * .protocols.ss7.tcap.api.tc.dialog.events.TCBeginRequest)
	 */
	@Override
	public void send(TCBeginRequest event) throws TCAPSendException {
		this.wrappedDialog.send(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#send(org.mobicents
	 * .protocols.ss7.tcap.api.tc.dialog.events.TCContinueRequest)
	 */
	@Override
	public void send(TCContinueRequest event) throws TCAPSendException {
		this.wrappedDialog.send(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#send(org.mobicents
	 * .protocols.ss7.tcap.api.tc.dialog.events.TCEndRequest)
	 */
	@Override
	public void send(TCEndRequest event) throws TCAPSendException {
		this.wrappedDialog.send(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#send(org.mobicents
	 * .protocols.ss7.tcap.api.tc.dialog.events.TCUserAbortRequest)
	 */
	@Override
	public void send(TCUserAbortRequest event) throws TCAPSendException {
		this.wrappedDialog.send(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#send(org.mobicents
	 * .protocols.ss7.tcap.api.tc.dialog.events.TCUniRequest)
	 */
	@Override
	public void send(TCUniRequest event) throws TCAPSendException {
		this.wrappedDialog.send(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#sendComponent(org
	 * .mobicents.protocols.ss7.tcap.asn.comp.Component)
	 */
	@Override
	public void sendComponent(Component componentRequest) throws TCAPSendException {
		this.wrappedDialog.sendComponent(componentRequest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.tcap.api.tc.dialog.Dialog#setUserObject(java
	 * .lang.Object)
	 */
	@Override
	public void setUserObject(Object arg0) {
		throw new UnsupportedOperationException();
	}
	
	public TCAPDialogActivityHandle getActivityHandle() {
		return this.activityHandle;
	}

	public void clear() {
		// TODO Any more cleaning here?
		if (this.activityHandle != null) {
			this.activityHandle.setActivity(null);
			this.activityHandle = null;
		}

		if (this.wrappedDialog != null) {
			this.wrappedDialog.setUserObject(null);
			this.wrappedDialog = null;
		}
	}
	
	public Dialog getWrappedDialog(){
		return this.wrappedDialog;
	}

	public TCAPResourceAdaptor getRa() {
		return ra;
	}

}
