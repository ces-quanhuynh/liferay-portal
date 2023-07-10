/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.service;

/**
 * Provides a wrapper for {@link PermissionPropagationEntryService}.
 *
 * @author Brian Wing Shun Chan
 * @see PermissionPropagationEntryService
 * @generated
 */
public class PermissionPropagationEntryServiceWrapper
	implements PermissionPropagationEntryService,
			   ServiceWrapper<PermissionPropagationEntryService> {

	public PermissionPropagationEntryServiceWrapper() {
		this(null);
	}

	public PermissionPropagationEntryServiceWrapper(
		PermissionPropagationEntryService permissionPropagationEntryService) {

		_permissionPropagationEntryService = permissionPropagationEntryService;
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public java.lang.String getOSGiServiceIdentifier() {
		return _permissionPropagationEntryService.getOSGiServiceIdentifier();
	}

	@Override
	public PermissionPropagationEntryService getWrappedService() {
		return _permissionPropagationEntryService;
	}

	@Override
	public void setWrappedService(
		PermissionPropagationEntryService permissionPropagationEntryService) {

		_permissionPropagationEntryService = permissionPropagationEntryService;
	}

	private PermissionPropagationEntryService
		_permissionPropagationEntryService;

}