/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012.
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.resource.cap;

import javax.naming.InitialContext;
import javax.slee.Address;
import javax.slee.AddressPlan;
import javax.slee.SLEEException;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityAlreadyExistsException;
import javax.slee.resource.ActivityFlags;
import javax.slee.resource.ActivityHandle;
import javax.slee.resource.ActivityIsEndingException;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.EventFlags;
import javax.slee.resource.FailureReason;
import javax.slee.resource.FireEventException;
import javax.slee.resource.FireableEventType;
import javax.slee.resource.IllegalEventException;
import javax.slee.resource.InvalidConfigurationException;
import javax.slee.resource.Marshaler;
import javax.slee.resource.ReceivableService;
import javax.slee.resource.ResourceAdaptor;
import javax.slee.resource.ResourceAdaptorContext;
import javax.slee.resource.SleeEndpoint;
import javax.slee.resource.StartActivityException;
import javax.slee.resource.UnrecognizedActivityHandleException;

import org.mobicents.protocols.ss7.cap.api.CAPDialog;
import org.mobicents.protocols.ss7.cap.api.CAPDialogListener;
import org.mobicents.protocols.ss7.cap.api.CAPMessage;
import org.mobicents.protocols.ss7.cap.api.CAPProvider;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPComponentErrorReason;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPDialogState;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPGeneralAbortReason;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPGprsReferenceNumber;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPNoticeProblemDiagnostic;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPUserAbortReason;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessage;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ActivityTestRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ActivityTestResponse;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ApplyChargingReportRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ApplyChargingRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.AssistRequestInstructionsRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPDialogCircuitSwitchedCall;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPServiceCircuitSwitchedCallListener;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CallInformationReportRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CallInformationRequestRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CancelRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ConnectRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ConnectToResourceRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ContinueRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.DisconnectForwardConnectionRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.EstablishTemporaryConnectionRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.EventReportBCSMRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.FurnishChargingInformationRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.InitialDPRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.PlayAnnouncementRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.PromptAndCollectUserInformationRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.PromptAndCollectUserInformationResponse;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ReleaseCallRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.RequestReportBCSMEventRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ResetTimerRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.SendChargingInformationRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.SpecializedResourceReportRequest;
import org.mobicents.protocols.ss7.cap.api.service.gprs.CAPDialogGprs;
import org.mobicents.protocols.ss7.cap.api.service.gprs.CAPServiceGprsListener;
import org.mobicents.protocols.ss7.cap.api.service.sms.CAPDialogSms;
import org.mobicents.protocols.ss7.cap.api.service.sms.CAPServiceSmsListener;
import org.mobicents.protocols.ss7.tcap.asn.comp.PAbortCauseType;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;
import org.mobicents.slee.resource.cap.events.CAPEvent;
import org.mobicents.slee.resource.cap.events.DialogAccept;
import org.mobicents.slee.resource.cap.events.DialogClose;
import org.mobicents.slee.resource.cap.events.DialogDelimiter;
import org.mobicents.slee.resource.cap.events.DialogNotice;
import org.mobicents.slee.resource.cap.events.DialogProviderAbort;
import org.mobicents.slee.resource.cap.events.DialogRelease;
import org.mobicents.slee.resource.cap.events.DialogRequest;
import org.mobicents.slee.resource.cap.events.DialogTimeout;
import org.mobicents.slee.resource.cap.events.DialogUserAbort;
import org.mobicents.slee.resource.cap.events.ErrorComponent;
import org.mobicents.slee.resource.cap.events.InvokeTimeout;
import org.mobicents.slee.resource.cap.events.ProviderErrorComponent;
import org.mobicents.slee.resource.cap.events.RejectComponent;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.ActivityTestRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.ActivityTestResponseWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.ApplyChargingReportRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.ApplyChargingRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.AssistRequestInstructionsRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.CAPDialogCircuitSwitchedCallWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.CallInformationReportRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.CallInformationRequestRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.CancelRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.ConnectRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.ConnectToResourceRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.ContinueRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.DisconnectForwardConnectionRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.EstablishTemporaryConnectionRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.EventReportBCSMRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.FurnishChargingInformationRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.InitialDPRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.PlayAnnouncementRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.PromptAndCollectUserInformationRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.PromptAndCollectUserInformationResponseWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.ReleaseCallRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.RequestReportBCSMEventRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.ResetTimerRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.SendChargingInformationRequestWrapper;
import org.mobicents.slee.resource.cap.service.circuitSwitchedCall.wrappers.SpecializedResourceReportRequestWrapper;
import org.mobicents.slee.resource.cap.service.gprs.wrappers.CAPDialogGprsWrapper;
import org.mobicents.slee.resource.cap.service.sms.wrappers.CAPDialogSmsWrapper;
import org.mobicents.slee.resource.cap.wrappers.CAPDialogWrapper;
import org.mobicents.slee.resource.cap.wrappers.CAPProviderWrapper;


/**
 * 
 * @author amit bhayani
 * @author baranowb
 * @author sergey vetyutnev
 * 
 */
public class CAPResourceAdaptor implements ResourceAdaptor, CAPDialogListener, CAPServiceCircuitSwitchedCallListener, CAPServiceGprsListener,
		CAPServiceSmsListener {
	/**
	 * for all events we are interested in knowing when the event failed to be
	 * processed
	 */
	public static final int DEFAULT_EVENT_FLAGS = EventFlags.REQUEST_PROCESSING_FAILED_CALLBACK;

	private static final int ACTIVITY_FLAGS = ActivityFlags.REQUEST_ENDED_CALLBACK;// .NO_FLAGS;

	/**
	 * This is local proxy of provider.
	 */
	protected CAPProviderWrapper capProvider = null;
	protected CAPProvider realProvider = null; // so we dont have to "get"
	private Tracer tracer;
	private transient SleeEndpoint sleeEndpoint = null;

	private ResourceAdaptorContext resourceAdaptorContext;

	private EventIDCache eventIdCache = null;

	/**
	 * tells the RA if an event with a specified ID should be filtered or not
	 */
	private final EventIDFilter eventIDFilter = new EventIDFilter();

	// ////////////////////////////
	// Configuration parameters //
	// ////////////////////////////
	private static final String CONF_CAP_JNDI = "capJndi";

	private String capJndi = null;
	private transient static final Address address = new Address(AddressPlan.IP, "localhost");

	public CAPResourceAdaptor() {
		this.capProvider = new CAPProviderWrapper(this);
	}

	// ////////////////
	// RA callbacks //
	// ////////////////
	public void activityEnded(ActivityHandle activityHandle) {
		if (this.tracer.isFineEnabled()) {
			this.tracer.fine("Activity with handle " + activityHandle + " ended");
		}
		CAPDialogActivityHandle mdah = (CAPDialogActivityHandle) activityHandle;
		final CAPDialogWrapper dw = mdah.getActivity();
		mdah.setActivity(null);

		if (dw != null) {
			dw.clear();
		}
	}

	public void activityUnreferenced(ActivityHandle arg0) {
		// TODO Auto-generated method stub

	}

	public void administrativeRemove(ActivityHandle arg0) {
		// TODO Auto-generated method stub

	}

	public void eventProcessingFailed(ActivityHandle arg0, FireableEventType arg1, Object arg2, Address arg3,
			ReceivableService arg4, int arg5, FailureReason arg6) {
		// TODO Auto-generated method stub

	}

	public void eventProcessingSuccessful(ActivityHandle arg0, FireableEventType arg1, Object arg2, Address arg3,
			ReceivableService arg4, int arg5) {
		// TODO Auto-generated method stub

	}

	public void eventUnreferenced(ActivityHandle arg0, FireableEventType arg1, Object arg2, Address arg3,
			ReceivableService arg4, int arg5) {
		// TODO Auto-generated method stub

	}

	public Object getActivity(ActivityHandle handle) {
		return ((CAPDialogActivityHandle) handle).getActivity();
	}

	public ActivityHandle getActivityHandle(Object activity) {
		if (activity instanceof CAPDialogWrapper) {
			final CAPDialogWrapper wrapper = ((CAPDialogWrapper) activity);
			if (wrapper.getRa() == this) {
				return wrapper.getActivityHandle();
			}
		}

		return null;
	}

	public Marshaler getMarshaler() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getResourceAdaptorInterface(String arg0) {
		return this.capProvider;
	}

	public void queryLiveness(ActivityHandle activityHandle) {
		final CAPDialogActivityHandle handle = ((CAPDialogActivityHandle) activityHandle);
		final CAPDialogWrapper capDialog = handle.getActivity();
		if (capDialog == null || capDialog.getWrappedDialog() == null
				|| capDialog.getState() == CAPDialogState.Expunged) {
			sleeEndpoint.endActivity(handle);
		}
	}

	public void raActive() {

		try {
			InitialContext ic = new InitialContext();
			this.realProvider = (CAPProvider) ic.lookup(this.capJndi);
			tracer.info("Successfully connected to CAP service[" + this.capJndi + "]");

			this.realProvider.addCAPDialogListener(this);

			this.realProvider.getCAPServiceCircuitSwitchedCall().addCAPServiceListener(this);
			this.realProvider.getCAPServiceGprs().addCAPServiceListener(this);
			this.realProvider.getCAPServiceSms().addCAPServiceListener(this);

			this.sleeEndpoint = resourceAdaptorContext.getSleeEndpoint();

			this.realProvider.getCAPServiceCircuitSwitchedCall().acivate();
			this.realProvider.getCAPServiceGprs().acivate();
			this.realProvider.getCAPServiceSms().acivate();

			this.capProvider.setWrappedProvider(this.realProvider);
		} catch (Exception e) {
			this.tracer.severe("Failed to activate CAP RA ", e);
		}
	}

	public void raConfigurationUpdate(ConfigProperties properties) {
		raConfigure(properties);
	}

	public void raConfigure(ConfigProperties properties) {
		try {
			if (tracer.isInfoEnabled()) {
				tracer.info("Configuring CAP RA: " + this.resourceAdaptorContext.getEntityName());
			}
			this.capJndi = (String) properties.getProperty(CONF_CAP_JNDI).getValue();
		} catch (Exception e) {
			tracer.severe("Configuring of CAP RA failed ", e);
		}
	}

	public void raInactive() {
		this.realProvider.getCAPServiceCircuitSwitchedCall().deactivate();
		this.realProvider.getCAPServiceGprs().deactivate();
		this.realProvider.getCAPServiceSms().deactivate();

		this.realProvider.getCAPServiceCircuitSwitchedCall().removeCAPServiceListener(this);
		this.realProvider.getCAPServiceGprs().removeCAPServiceListener(this);
		this.realProvider.getCAPServiceSms().removeCAPServiceListener(this);

		this.realProvider.removeCAPDialogListener(this);
	}

	public void raStopping() {
		// TODO Auto-generated method stub

	}

	public void raUnconfigure() {
		this.capJndi = null;
	}

	public void raVerifyConfiguration(ConfigProperties properties) throws InvalidConfigurationException {
		try {

			if (tracer.isInfoEnabled()) {
				tracer.info("Verifying configuring CAP RA: " + this.resourceAdaptorContext.getEntityName());
			}

			this.capJndi = (String) properties.getProperty(CONF_CAP_JNDI).getValue();
			if (this.capJndi == null) {
				throw new InvalidConfigurationException("CAP JNDI lookup name cannot be null");
			}

		} catch (Exception e) {
			throw new InvalidConfigurationException("Failed to test configuration options!", e);
		}
	}

	public void serviceActive(ReceivableService receivableService) {
		eventIDFilter.serviceActive(receivableService);
	}

	public void serviceInactive(ReceivableService receivableService) {
		eventIDFilter.serviceInactive(receivableService);
	}

	public void serviceStopping(ReceivableService receivableService) {
		eventIDFilter.serviceStopping(receivableService);
	}

	public void setResourceAdaptorContext(ResourceAdaptorContext raContext) {
		this.resourceAdaptorContext = raContext;
		this.tracer = resourceAdaptorContext.getTracer(CAPResourceAdaptor.class.getSimpleName());

		this.eventIdCache = new EventIDCache(this.tracer);
	}

	public void unsetResourceAdaptorContext() {
		this.resourceAdaptorContext = null;
	}

	// //////////////////
	// Helper methods //
	// //////////////////
	public void startActivity(CAPDialogWrapper capDialogWrapper) throws ActivityAlreadyExistsException,
			NullPointerException, IllegalStateException, SLEEException, StartActivityException {
		this.sleeEndpoint.startActivity(capDialogWrapper.getActivityHandle(), capDialogWrapper, ACTIVITY_FLAGS);
	}

	public void startSuspendedActivity(CAPDialogWrapper capDialogWrapper) throws ActivityAlreadyExistsException,
			NullPointerException, IllegalStateException, SLEEException, StartActivityException {
		this.sleeEndpoint.startActivitySuspended(capDialogWrapper.getActivityHandle(), capDialogWrapper,
				ActivityFlags.REQUEST_ENDED_CALLBACK);
	}

	/**
	 * Private methods
	 */
	private void fireEvent(String eventName, ActivityHandle handle, Object event) {

		FireableEventType eventID = eventIdCache.getEventId(this.resourceAdaptorContext.getEventLookupFacility(),
				eventName);

		if (eventIDFilter.filterEvent(eventID)) {
			if (tracer.isFineEnabled()) {
				tracer.fine("Event " + (eventID == null ? "null" : eventID.getEventType()) + " filtered");
			}
		} else {

			try {
				sleeEndpoint.fireEvent(handle, eventID, event, address, null);
			} catch (UnrecognizedActivityHandleException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (IllegalEventException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (ActivityIsEndingException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (NullPointerException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (SLEEException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (FireEventException e) {
				this.tracer.severe("Error while firing event", e);
			}
		}
	}

	// /////////////////
	// Event helpers //
	// /////////////////

	private CAPDialogActivityHandle onEvent(String eventName, CAPDialogWrapper dw, CAPEvent event) {
		if (dw == null) {
			this.tracer.severe(String.format("Firing %s but CAPDialogWrapper userObject is null", eventName));
			return null;
		}

		if (this.tracer.isFineEnabled()) {
			this.tracer
					.fine(String.format("Firing %s for DialogId=%d", eventName, dw.getWrappedDialog().getDialogId()));
		}

		this.fireEvent(eventName, dw.getActivityHandle(), event);
		return dw.getActivityHandle();
	}

	public void onDialogAccept(CAPDialog capDialog, CAPGprsReferenceNumber capGprsReferenceNumber) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		DialogAccept dialogAccept = new DialogAccept(capDialogWrapper, capGprsReferenceNumber);
		onEvent(dialogAccept.getEventTypeName(), capDialogWrapper, dialogAccept);
	}

	public void onDialogClose(CAPDialog capDialog) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		DialogClose dialogClose = new DialogClose(capDialogWrapper);
		CAPDialogActivityHandle handle = onEvent(dialogClose.getEventTypeName(), capDialogWrapper, dialogClose);

		// End Activity
		// if (handle != null)
		// this.sleeEndpoint.endActivity(handle);
	}

	public void onDialogDelimiter(CAPDialog capDialog) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		DialogDelimiter dialogDelimiter = new DialogDelimiter(capDialogWrapper);
		onEvent(dialogDelimiter.getEventTypeName(), capDialogWrapper, dialogDelimiter);
	}

	public void onDialogNotice(CAPDialog capDialog, CAPNoticeProblemDiagnostic noticeProblemDiagnostic) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		DialogNotice dialogNotice = new DialogNotice(capDialogWrapper, noticeProblemDiagnostic);
		onEvent(dialogNotice.getEventTypeName(), capDialogWrapper, dialogNotice);
	}

	public void onDialogProviderAbort(CAPDialog capDialog, PAbortCauseType abortCause) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		DialogProviderAbort dialogProviderAbort = new DialogProviderAbort(capDialogWrapper, abortCause);
		CAPDialogActivityHandle handle = onEvent(dialogProviderAbort.getEventTypeName(), capDialogWrapper, dialogProviderAbort);

		// End Activity
		// if (handle != null)
		// this.sleeEndpoint.endActivity(handle);
	}

	public void onDialogUserAbort(CAPDialog capDialog, CAPGeneralAbortReason generalReason, CAPUserAbortReason userReason) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		DialogUserAbort dialogUserAbort = new DialogUserAbort(capDialogWrapper, generalReason, userReason);
		CAPDialogActivityHandle handle = onEvent(dialogUserAbort.getEventTypeName(), capDialogWrapper, dialogUserAbort);

		// End Activity
		// if (handle != null)
		// this.sleeEndpoint.endActivity(handle);

	}

	private void handleDialogRequest(CAPDialog capDialog, CAPGprsReferenceNumber capGprsReferenceNumber) {
		try {

			if (this.tracer.isFineEnabled()) {
				this.tracer.fine(String.format("Received onDialogRequest id=%d ", capDialog.getDialogId()));
			}

			CAPDialogActivityHandle activityHandle = new CAPDialogActivityHandle(capDialog.getDialogId());
			CAPDialogWrapper capDialogWrapper = null;

			if (capDialog instanceof CAPDialogCircuitSwitchedCall) {
				capDialogWrapper = new CAPDialogCircuitSwitchedCallWrapper((CAPDialogCircuitSwitchedCall) capDialog, activityHandle, this);
			} else if (capDialog instanceof CAPDialogGprs) {
				capDialogWrapper = new CAPDialogGprsWrapper((CAPDialogGprs) capDialog, activityHandle, this);
			} else if (capDialog instanceof CAPDialogSms) {
				capDialogWrapper = new CAPDialogSmsWrapper((CAPDialogSms) capDialog, activityHandle, this);
			} else {
				this.tracer.severe(String.format("Received onDialogRequest id=%d for unknown CAPDialog class=%s", capDialog.getDialogId(),
						capDialog.getClass().getName()));
				return;
			}

			DialogRequest event = new DialogRequest(capDialogWrapper, capGprsReferenceNumber);
			capDialog.setUserObject(capDialogWrapper);
			this.startActivity(capDialogWrapper);
			this.fireEvent(event.getEventTypeName(), capDialogWrapper.getActivityHandle(), event);

		} catch (Exception e) {
			this.tracer.severe(String.format(
					"Exception when trying to fire event DIALOG_REQUEST for received DialogRequest=%s ", capDialog), e);
		}
	}

	public void onDialogRequest(CAPDialog capDialog, CAPGprsReferenceNumber capGprsReferenceNumber) {
		this.handleDialogRequest(capDialog, capGprsReferenceNumber);
	}

	public void onDialogRelease(CAPDialog capDialog) {
		try {

			CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
			DialogRelease dialogRelease = new DialogRelease(capDialogWrapper);
			CAPDialogActivityHandle handle = onEvent(dialogRelease.getEventTypeName(), capDialogWrapper,
					dialogRelease);

			// End Activity
			this.sleeEndpoint.endActivity(handle);
		} catch (Exception e) {
			this.tracer.severe(String.format(
					"onDialogRelease : Exception while trying to end activity for CAPDialog=%s", capDialog), e);
		}
	}

	public void onDialogTimeout(CAPDialog capDialog) {

		// TODO: done like that, since we want to process dialog callbacks
		// before we fire event.
		if (this.tracer.isFineEnabled()) {
			this.tracer.fine(String.format("Rx : onDialogTimeout for DialogId=%d", capDialog.getDialogId()));
		}

		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		DialogTimeout dt = new DialogTimeout(capDialogWrapper);
		onEvent(dt.getEventTypeName(), capDialogWrapper, dt);
	}


	// ///////////////////////
	// Component callbacks //
	// ///////////////////////

	public void onErrorComponent(CAPDialog capDialog, Long invokeId, CAPErrorMessage capErrorMessage) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		ErrorComponent errorComponent = new ErrorComponent(capDialogWrapper, invokeId, capErrorMessage);
		onEvent(errorComponent.getEventTypeName(), capDialogWrapper, errorComponent);
	}

	public void onRejectComponent(CAPDialog capDialog, Long invokeId, Problem problem) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		RejectComponent rejectComponent = new RejectComponent(capDialogWrapper, invokeId, problem);
		onEvent(rejectComponent.getEventTypeName(), capDialogWrapper, rejectComponent);
	}

	public void onInvokeTimeout(CAPDialog capDialog, Long invokeId) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		InvokeTimeout invokeTimeout = new InvokeTimeout(capDialogWrapper, invokeId);
		onEvent(invokeTimeout.getEventTypeName(), capDialogWrapper, invokeTimeout);
	}

	public void onProviderErrorComponent(CAPDialog capDialog, Long invokeId, CAPComponentErrorReason providerError) {
		CAPDialogWrapper capDialogWrapper = (CAPDialogWrapper) capDialog.getUserObject();
		ProviderErrorComponent providerErrorComponent = new ProviderErrorComponent(capDialogWrapper, invokeId,
				providerError);
		onEvent(providerErrorComponent.getEventTypeName(), capDialogWrapper, providerErrorComponent);
	}

	public void onCAPMessage(CAPMessage capMessage) {
		// TODO Auto-generated method stub
	}


	// ///////////////////////
	// Service: CircuitSwitchedCall
	// ///////////////////////

	@Override
	public void onInitialDPRequest(InitialDPRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		InitialDPRequestWrapper event = new InitialDPRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onRequestReportBCSMEventRequest(RequestReportBCSMEventRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		RequestReportBCSMEventRequestWrapper event = new RequestReportBCSMEventRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onApplyChargingRequest(ApplyChargingRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		ApplyChargingRequestWrapper event = new ApplyChargingRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onEventReportBCSMRequest(EventReportBCSMRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		EventReportBCSMRequestWrapper event = new EventReportBCSMRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onContinueRequest(ContinueRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		ContinueRequestWrapper event = new ContinueRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onApplyChargingReportRequest(ApplyChargingReportRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		ApplyChargingReportRequestWrapper event = new ApplyChargingReportRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onReleaseCallRequest(ReleaseCallRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		ReleaseCallRequestWrapper event = new ReleaseCallRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onConnectRequest(ConnectRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		ConnectRequestWrapper event = new ConnectRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onCallInformationRequestRequest(CallInformationRequestRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		CallInformationRequestRequestWrapper event = new CallInformationRequestRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onCallInformationReportRequest(CallInformationReportRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		CallInformationReportRequestWrapper event = new CallInformationReportRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onActivityTestRequest(ActivityTestRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		ActivityTestRequestWrapper event = new ActivityTestRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onActivityTestResponse(ActivityTestResponse ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		ActivityTestResponseWrapper event = new ActivityTestResponseWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onAssistRequestInstructionsRequest(AssistRequestInstructionsRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		AssistRequestInstructionsRequestWrapper event = new AssistRequestInstructionsRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onEstablishTemporaryConnectionRequest(EstablishTemporaryConnectionRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		EstablishTemporaryConnectionRequestWrapper event = new EstablishTemporaryConnectionRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onDisconnectForwardConnectionRequest(DisconnectForwardConnectionRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		DisconnectForwardConnectionRequestWrapper event = new DisconnectForwardConnectionRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onConnectToResourceRequest(ConnectToResourceRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		ConnectToResourceRequestWrapper event = new ConnectToResourceRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onResetTimerRequest(ResetTimerRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		ResetTimerRequestWrapper event = new ResetTimerRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onFurnishChargingInformationRequest(FurnishChargingInformationRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		FurnishChargingInformationRequestWrapper event = new FurnishChargingInformationRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onSendChargingInformationRequest(SendChargingInformationRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		SendChargingInformationRequestWrapper event = new SendChargingInformationRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onSpecializedResourceReportRequest(SpecializedResourceReportRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		SpecializedResourceReportRequestWrapper event = new SpecializedResourceReportRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onPlayAnnouncementRequest(PlayAnnouncementRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		PlayAnnouncementRequestWrapper event = new PlayAnnouncementRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onPromptAndCollectUserInformationRequest(PromptAndCollectUserInformationRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		PromptAndCollectUserInformationRequestWrapper event = new PromptAndCollectUserInformationRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onPromptAndCollectUserInformationResponse(PromptAndCollectUserInformationResponse ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		PromptAndCollectUserInformationResponseWrapper event = new PromptAndCollectUserInformationResponseWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

	@Override
	public void onCancelRequest(CancelRequest ind) {
		CAPDialogCircuitSwitchedCallWrapper capDialogCircuitSwitchedCallWrapper = (CAPDialogCircuitSwitchedCallWrapper) ind.getCAPDialog().getUserObject();
		CancelRequestWrapper event = new CancelRequestWrapper(capDialogCircuitSwitchedCallWrapper, ind);
		onEvent(event.getEventTypeName(), capDialogCircuitSwitchedCallWrapper, event);
	}

}