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

import com.liferay.portal.kernel.model.PermissionPropagationEntry;
import com.liferay.portal.kernel.service.PermissionPropagationEntryLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

/**
 * @author To Trinh
 */
public abstract class BasePortletConfigurationPermissionPropagation
	implements PortletConfigurationPermissionPropagation {

	@Override
	public boolean getPermissionPropagation(PortletRequest portletRequest) {
		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionPropagationEntry permissionPropagationEntry =
			PermissionPropagationEntryLocalServiceUtil.
				fetchPermissionPropagationEntry(
					themeDisplay.getCompanyId(), getGroupId(portletRequest),
					getClassName(), getClassPK(portletRequest));

		if (permissionPropagationEntry != null) {
			return permissionPropagationEntry.isPropagation();
		}

		return getDefaultPermissionPropagation();
	}

	@Override
	public boolean isAvailable(PortletRequest portletRequest) {
		String modelResource = ParamUtil.getString(
			portletRequest, "modelResource");

		if (!ArrayUtil.contains(getModelResources(), modelResource) ||
			(getClassPK(portletRequest) == 0)) {

			return false;
		}

		return true;
	}

	@Override
	public void updatePermissionPropagation(
		ActionRequest actionRequest, ActionResponse actionResponse) {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PermissionPropagationEntryLocalServiceUtil.
			updatePermissionPropagationEntry(
				themeDisplay.getCompanyId(), getGroupId(actionRequest),
				getClassName(), getClassPK(actionRequest),
				ParamUtil.getBoolean(
					actionRequest, "permissionPropagationEnabled"));
	}

	protected abstract String getClassName();

	protected long getClassPK(PortletRequest portletRequest) {
		return ParamUtil.getLong(portletRequest, "resourcePrimKey");
	}

	protected abstract boolean getDefaultPermissionPropagation();

	protected long getGroupId(PortletRequest portletRequest) {
		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return ParamUtil.getLong(
			portletRequest, "resourceGroupId", themeDisplay.getScopeGroupId());
	}

	protected abstract String[] getModelResources();

}