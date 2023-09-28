/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portlet.documentlibrary.util;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryConstants;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.portal.kernel.exception.NoSuchRoleException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.PermissionPropagationEntry;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.PermissionPropagationEntryLocalServiceUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.permission.ModelPermissions;
import com.liferay.portal.kernel.service.permission.ModelPermissionsFactory;
import com.liferay.portal.kernel.util.SetUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author To Trinh
 */
public class DLPermissionPropagationUtil {

	public static Map<Long, Set<String>>
		getDefaultInheritableDocumentPermissions(
			long companyId, long groupId, long inheritableParentFolderId,
			String folderResourceName) {

		Map<Long, Set<String>> defaultInheritableDocumentPermissions =
			new HashMap<>();

		Map<Long, Set<String>> dlFolderRoleIdsToActionIds =
			ResourcePermissionLocalServiceUtil.
				getAvailableResourcePermissionActionIds(
					companyId, folderResourceName,
					ResourceConstants.SCOPE_INDIVIDUAL,
					String.valueOf(inheritableParentFolderId),
					ResourceActionsUtil.getModelResourceActions(
						folderResourceName));

		Map<Long, Set<String>> dlFileEntryRoleIdsToActionIds =
			_getDefaultDocumentPermissions(companyId, groupId);

		Set<Long> dlFolderRoleIds = dlFolderRoleIdsToActionIds.keySet();

		for (Long dlFolderRoleId : dlFolderRoleIds) {
			Set<String> dlFolderRoleIdToActionIds =
				dlFolderRoleIdsToActionIds.get(dlFolderRoleId);

			dlFolderRoleIdToActionIds.retainAll(_commonPermissions);

			Set<String> dlFileEntryActionIds = new HashSet<>(
				dlFolderRoleIdToActionIds);

			Set<String> dlFileEntryRoleIdToActionIds =
				dlFileEntryRoleIdsToActionIds.get(dlFolderRoleId);

			if (dlFileEntryRoleIdToActionIds != null) {
				dlFileEntryRoleIdToActionIds.removeAll(_commonPermissions);

				dlFileEntryActionIds.addAll(dlFileEntryRoleIdToActionIds);
			}

			defaultInheritableDocumentPermissions.put(
				dlFolderRoleId, dlFileEntryActionIds);
		}

		return defaultInheritableDocumentPermissions;
	}

	public static long getInheritableParentFolderId(DLFileEntry dlFileEntry)
		throws PortalException {

		List<Long> ancestorFolderIds = new ArrayList<>();

		if (dlFileEntry.getFolderId() !=
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {

			DLFolder folder = dlFileEntry.getFolder();

			ancestorFolderIds.add(folder.getFolderId());
			ancestorFolderIds.addAll(folder.getAncestorFolderIds());
		}

		return getInheritableParentFolderId(
			dlFileEntry.getCompanyId(), dlFileEntry.getGroupId(),
			ancestorFolderIds);
	}

	public static long getInheritableParentFolderId(DLFolder dlFolder)
		throws PortalException {

		return getInheritableParentFolderId(
			dlFolder.getCompanyId(), dlFolder.getGroupId(),
			dlFolder.getAncestorFolderIds());
	}

	public static long getInheritableParentFolderId(
		long companyId, long groupId, List<Long> ancestorIds) {

		for (long ancestorId : ancestorIds) {
			PermissionPropagationEntry permissionPropagationEntry =
				PermissionPropagationEntryLocalServiceUtil.
					fetchPermissionPropagationEntry(
						companyId, groupId, DLFolderConstants.getClassName(),
						ancestorId);

			if ((permissionPropagationEntry != null) &&
				permissionPropagationEntry.isPropagation()) {

				return ancestorId;
			}
		}

		if (isHomeFolderPermissionPropagationEnabled(companyId, groupId)) {
			return DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
		}

		return -1;
	}

	public static boolean isHomeFolderPermissionPropagationEnabled(
		long companyId, long groupId) {

		PermissionPropagationEntry permissionPropagationEntry =
			PermissionPropagationEntryLocalServiceUtil.
				fetchPermissionPropagationEntry(
					companyId, groupId, DLFolderConstants.getClassName(),
					groupId);

		if (permissionPropagationEntry != null) {
			return permissionPropagationEntry.isPropagation();
		}

		return DLFolderConstants.DEFAULT_FOLDER_PERMISSION_PROPAGATION_ENABLED;
	}

	private static Map<Long, Set<String>> _getDefaultDocumentPermissions(
		long companyId, long groupId) {

		Map<Long, Set<String>> defaultDocumentPermissions = new HashMap<>();

		try {
			Role ownerRole = RoleLocalServiceUtil.getRole(
				companyId, RoleConstants.OWNER);

			List<String> ownerActionIds =
				ResourceActionsUtil.getModelResourceActions(
					DLFileEntryConstants.getClassName());

			List<String> defaultOwnerActions =
				ResourceActionsUtil.getModelResourceOwnerDefaultActions(
					DLFileEntryConstants.getClassName());

			if (!defaultOwnerActions.isEmpty()) {
				ownerActionIds.retainAll(defaultOwnerActions);
			}

			defaultDocumentPermissions.put(
				ownerRole.getRoleId(), SetUtil.fromCollection(ownerActionIds));

			ModelPermissions modelPermissions =
				ModelPermissionsFactory.createWithDefaultPermissions(
					DLFileEntryConstants.getClassName());

			for (String roleName : modelPermissions.getRoleNames()) {
				Role role = _getRole(companyId, groupId, roleName);

				defaultDocumentPermissions.put(
					role.getRoleId(),
					SetUtil.fromArray(modelPermissions.getActionIds(roleName)));
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		return defaultDocumentPermissions;
	}

	private static Role _getRole(long companyId, long groupId, String roleName)
		throws PortalException {

		if (roleName.equals(RoleConstants.PLACEHOLDER_DEFAULT_GROUP_ROLE)) {
			if (groupId == 0) {
				throw new NoSuchRoleException(
					"Specify a group ID other than 0 for role name " +
						RoleConstants.PLACEHOLDER_DEFAULT_GROUP_ROLE);
			}

			return RoleLocalServiceUtil.getDefaultGroupRole(groupId);
		}

		return RoleLocalServiceUtil.getRole(companyId, roleName);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DLPermissionPropagationUtil.class);

	private static final Set<String> _commonPermissions = new HashSet<>(
		Arrays.asList(
			ActionKeys.DELETE, ActionKeys.PERMISSIONS, ActionKeys.UPDATE,
			ActionKeys.VIEW));

}