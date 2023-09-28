/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.portlet.configuration.permission.propagation;

import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.portlet.configuration.permission.propagation.BasePortletConfigurationPermissionPropagation;
import com.liferay.portal.kernel.portlet.configuration.permission.propagation.PortletConfigurationPermissionPropagation;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portlet.documentlibrary.constants.DLConstants;
import com.liferay.portlet.documentlibrary.util.DLPermissionPropagationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author To Trinh
 */
@Component(
	property = {
		"javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY,
		"javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY_ADMIN,
		"javax.portlet.name=" + DLPortletKeys.MEDIA_GALLERY_DISPLAY
	},
	service = PortletConfigurationPermissionPropagation.class
)
public class DLFolderPortletConfigurationPermissionPropagation
	extends BasePortletConfigurationPermissionPropagation {

	@Override
	public String getAlertMessage() {
		return "changes-will-be-applied-to-new-documents-and-folder-uploaded-" +
			"into-it";
	}

	@Override
	public String getMessage() {
		return "set-permissions-for-documents-and-folders-uploaded-to-this-" +
			"folder";
	}

	@Override
	public Map<String, String> getPermissionsTabs(
		PortletRequest portletRequest) {

		if (!isAvailable(portletRequest)) {
			return new HashMap<>();
		}

		ThemeDisplay themeDisplay = (ThemeDisplay)portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		int count = _getFileEntriesCount(
			themeDisplay.getCompanyId(), getGroupId(portletRequest),
			_getClassPK(portletRequest));

		if (count == 0) {
			return new HashMap<>();
		}

		_initializeDocumentTabResources(
			themeDisplay.getCompanyId(), getGroupId(portletRequest),
			_getClassPK(portletRequest),
			_getFolderResourceName(portletRequest));

		return LinkedHashMapBuilder.put(
			"folder", _getFolderResourceName(portletRequest)
		).put(
			"document", _CHILD_DOCUMENTS_RESOURCE_NAME
		).build();
	}

	@Override
	public boolean isAvailable(PortletRequest portletRequest) {
		long[] classPKs = getClassPKs(portletRequest);

		if ((classPKs.length == 1) && super.isAvailable(portletRequest)) {
			if (getGroupId(portletRequest) == _getClassPK(portletRequest)) {
				return true;
			}

			DLFolder dlFolder = _dlFolderLocalService.fetchDLFolder(
				_getClassPK(portletRequest));

			if (dlFolder != null) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected String getClassName() {
		return DLFolderConstants.getClassName();
	}

	@Override
	protected boolean getDefaultPermissionPropagation(
		long groupId, long classPK) {

		if (classPK == groupId) {
			return DLFolderConstants.
				DEFAULT_FOLDER_PERMISSION_PROPAGATION_ENABLED;
		}

		return false;
	}

	@Override
	protected String[] getModelResources() {
		return new String[] {
			DLConstants.RESOURCE_NAME, DLFolderConstants.getClassName(),
			_CHILD_DOCUMENTS_RESOURCE_NAME
		};
	}

	private long _getClassPK(PortletRequest portletRequest) {
		long[] resourcePrimKeys = getClassPKs(portletRequest);

		return resourcePrimKeys[0];
	}

	private int _getFileEntriesCount(
		long companyId, long groupId, long folderId) {

		DynamicQuery dlFileEntryEntryDynamicQuery =
			_dlFileEntryLocalService.dynamicQuery();

		Property companyIdProperty = PropertyFactoryUtil.forName("companyId");

		dlFileEntryEntryDynamicQuery.add(companyIdProperty.eq(companyId));

		Property groupIdProperty = PropertyFactoryUtil.forName("groupId");

		dlFileEntryEntryDynamicQuery.add(groupIdProperty.eq(groupId));

		if (folderId != groupId) {
			Property treePathProperty = PropertyFactoryUtil.forName("treePath");

			dlFileEntryEntryDynamicQuery.add(
				treePathProperty.like(
					StringUtil.quote(
						String.valueOf(folderId), StringPool.PERCENT)));
		}

		return (int)_dlFileEntryLocalService.dynamicQueryCount(
			dlFileEntryEntryDynamicQuery);
	}

	private String _getFolderResourceName(PortletRequest portletRequest) {
		if (_getClassPK(portletRequest) == getGroupId(portletRequest)) {
			return DLConstants.RESOURCE_NAME;
		}

		return DLFolderConstants.getClassName();
	}

	private void _initializeDocumentTabResources(
		long companyId, long groupId, long folderId,
		String folderResourceName) {

		int count = _resourcePermissionLocalService.getResourcePermissionsCount(
			companyId, _CHILD_DOCUMENTS_RESOURCE_NAME,
			ResourceConstants.SCOPE_INDIVIDUAL, String.valueOf(folderId));

		if (count > 0) {
			return;
		}

		try {
			Map<Long, Set<String>> dlFileEntryRoleIdsToActionIds =
				DLPermissionPropagationUtil.
					getDefaultInheritableDocumentPermissions(
						companyId, groupId, folderId, folderResourceName);

			for (Map.Entry<Long, Set<String>> dlFileEntryRoleIdToActionIds :
					dlFileEntryRoleIdsToActionIds.entrySet()) {

				_resourcePermissionLocalService.setResourcePermissions(
					companyId, _CHILD_DOCUMENTS_RESOURCE_NAME,
					ResourceConstants.SCOPE_INDIVIDUAL,
					String.valueOf(folderId),
					dlFileEntryRoleIdToActionIds.getKey(),
					dlFileEntryRoleIdToActionIds.getValue(
					).toArray(
						new String[0]
					));
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}
	}

	private static final String _CHILD_DOCUMENTS_RESOURCE_NAME =
		ResourceActionsUtil.getCompositeModelName(
			DLFileEntryConstants.getClassName(),
			DLFolderConstants.getClassName());

	private static final Log _log = LogFactoryUtil.getLog(
		DLFolderPortletConfigurationPermissionPropagation.class);

	@Reference
	private DLFileEntryLocalService _dlFileEntryLocalService;

	@Reference
	private DLFolderLocalService _dlFolderLocalService;

	@Reference
	private ResourcePermissionLocalService _resourcePermissionLocalService;

}