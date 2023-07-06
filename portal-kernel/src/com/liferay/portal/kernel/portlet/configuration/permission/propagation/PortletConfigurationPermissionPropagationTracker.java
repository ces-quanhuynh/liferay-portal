/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.kernel.portlet.configuration.permission.propagation;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.model.ClassedModel;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.portlet.PortletIdCodec;

import org.osgi.framework.BundleContext;

/**
 * @author To Trinh
 */
public class PortletConfigurationPermissionPropagationTracker {

	public static <T extends ClassedModel>
		PortletConfigurationPermissionPropagation
			getPortletConfigurationPermissionPropagation(
				String portletResource) {

		String rootPortletId = PortletIdCodec.decodePortletName(
			portletResource);

		return (PortletConfigurationPermissionPropagation)
			_serviceTrackerMap.getService(rootPortletId);
	}

	private static final ServiceTrackerMap
		<String, PortletConfigurationPermissionPropagation> _serviceTrackerMap;

	static {
		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, PortletConfigurationPermissionPropagation.class,
			"javax.portlet.name");
	}

}