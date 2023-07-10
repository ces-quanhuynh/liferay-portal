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

package com.liferay.portal.service.impl;

import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.model.PermissionPropagationEntry;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.service.base.PermissionPropagationEntryLocalServiceBaseImpl;

/**
 * @author Brian Wing Shun Chan
 * @author Quan Huynh
 */
public class PermissionPropagationEntryLocalServiceImpl
	extends PermissionPropagationEntryLocalServiceBaseImpl {

	public void addPermissionPropagationEntry(
		long companyId, long groupId, String className, long classPK,
		boolean propagation) {

		long permissionPropagationEntryId = counterLocalService.increment();

		PermissionPropagationEntry permissionPropagationEntry =
			permissionPropagationEntryPersistence.create(
				permissionPropagationEntryId);

		permissionPropagationEntry.setGroupId(groupId);
		permissionPropagationEntry.setCompanyId(companyId);
		permissionPropagationEntry.setClassNameId(
			_classNameLocalService.getClassNameId(className));
		permissionPropagationEntry.setClassPK(classPK);
		permissionPropagationEntry.setPropagation(propagation);

		permissionPropagationEntryPersistence.update(
			permissionPropagationEntry);
	}

	@BeanReference(type = ClassNameLocalService.class)
	private ClassNameLocalService _classNameLocalService;

}