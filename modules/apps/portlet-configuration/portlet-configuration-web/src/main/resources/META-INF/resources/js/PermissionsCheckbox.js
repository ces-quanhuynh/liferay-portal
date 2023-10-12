/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayCheckbox} from '@clayui/form';
import PropTypes from 'prop-types';
import React, {useState} from 'react';

export default function PermissionsCheckbox({
	checked: initialChecked,
	componentId: _componentId,
	indeterminate: initialIndeterminate,
	locale: _locale,
	portletId: _portletId,
	portletNamespace: _portletNamespace,
	...otherProps
}) {
	const [checked, setChecked] = useState(
		Boolean(initialChecked || initialIndeterminate)
	);
	const [indeterminate, setIndeterminate] = useState(
		Boolean(initialIndeterminate)
	);

	const [value, setValue] = useState(
		initialIndeterminate ? 'indeterminate' : ''
	);

	const permissionPropagationEnabledCheckbox = document.getElementById(
		_portletNamespace + 'permissionPropagationEnabled'
	);

	const initialPermissionPropagationEnabled = Liferay.Util.SessionStorage.getItem(
		`${_portletNamespace}initialPermissionPropagationEnabled`,
		Liferay.Util.SessionStorage.TYPES.FUNCTIONAL
	);

	return (
		<ClayCheckbox
			checked={checked}
			indeterminate={indeterminate}
			inline
			onChange={() => {
				setChecked((prevCheckedState) => !prevCheckedState);

				const permissionCheckboxChangeCount = Liferay.Util.SessionStorage.getItem(
					`${_portletNamespace}permissionCheckboxChangeCount`,
					Liferay.Util.SessionStorage.TYPES.FUNCTIONAL
				);

				const changeValue =
					checked === initialChecked
						? Number(permissionCheckboxChangeCount) - 1
						: Number(permissionCheckboxChangeCount) + 1;

				Liferay.Util.SessionStorage.setItem(
					`${_portletNamespace}permissionCheckboxChangeCount`,
					changeValue,
					Liferay.Util.SessionStorage.TYPES.FUNCTIONAL
				);

				if (indeterminate) {
					setIndeterminate(false);
					setValue('');
				}

				const alertMessage = document.getElementById(
					_portletNamespace + 'alertMessage'
				);

				if (permissionPropagationEnabledCheckbox.checked) {
					if (changeValue !== 0) {
						alertMessage.classList.remove('hide');
					}
					else if (initialPermissionPropagationEnabled === 'true') {
						alertMessage.classList.add('hide');
					}
				}
			}}
			value={value}
			{...otherProps}
		/>
	);
}

PermissionsCheckbox.propTypes = {
	checked: PropTypes.bool,
	indeterminate: PropTypes.bool,
};
