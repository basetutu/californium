/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 ******************************************************************************/
package org.eclipse.californium.core.test.lockstep;

import java.util.Arrays;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.observe.NotificationListener;

public class SynchronousNotificationListener implements NotificationListener {

	private Request request; // request to listen
	private Response currentResponse;
	private Object lock = new Object();
	int i = 0;

	public SynchronousNotificationListener() {
	}

	public SynchronousNotificationListener(Request req) {
		request = req;
	}

	/**
	 * Wait until a response is received or the request was cancelled/rejected.
	 * 
	 * @return the response or null if waiting time elapses or if request is
	 *         cancelled/rejected.
	 */
	public Response waitForResponse(long timeoutInMs) throws InterruptedException {
		Response r;
		synchronized (lock) {
			if (currentResponse != null)
				r = currentResponse;
			else {
				lock.wait(timeoutInMs);
				r = currentResponse;
			}
			currentResponse = null;
		}
		System.out.println("waitForResponse " + i + " " + r);
		return r;
	}

	@Override
	public void onResponse(Request req, Response resp) {
		if (request == null || Arrays.equals(request.getToken(), req.getToken())) {
			synchronized (lock) {
				i++;
				System.out.println("notif " + i + " " + resp);
				currentResponse = resp;
				lock.notifyAll();
			}
		}
	}

	@Override
	public void onReject(Request req) {
		if (request == null || Arrays.equals(request.getToken(), req.getToken())) {
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}

	@Override
	public void onTimeout(Request req) {
		if (request == null || Arrays.equals(request.getToken(), req.getToken())) {
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}

	@Override
	public void onCancel(Request req) {
		if (request == null || Arrays.equals(request.getToken(), req.getToken())) {
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}

	@Override
	public void onAcknowledgement(Request request) {
	}

	@Override
	public void onRetransmission(Request request) {
	}
}
