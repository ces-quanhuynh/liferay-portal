/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.captcha.rest.internal.resource.v1_0;

import com.liferay.captcha.configuration.CaptchaConfiguration;
import com.liferay.captcha.recaptcha.ReCaptchaImpl;
import com.liferay.captcha.rest.resource.v1_0.CaptchaResource;
import com.liferay.captcha.util.CaptchaUtil;
import com.liferay.portal.kernel.captcha.Captcha;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Loc Pham
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/captcha.properties",
	scope = ServiceScope.PROTOTYPE, service = CaptchaResource.class
)
public class CaptchaResourceImpl extends BaseCaptchaResourceImpl {

	@Override
	public Response getSimpleCaptcha() throws Exception {
		CaptchaConfiguration captchaConfiguration =
			_configurationProvider.getCompanyConfiguration(
				CaptchaConfiguration.class, _contextCompany.getCompanyId());

		if (!captchaConfiguration.enabledHeadlessApiSimpleCaptcha() ||
			StringUtil.equalsIgnoreCase(
				captchaConfiguration.captchaEngine(),
				ReCaptchaImpl.class.getName())) {

			return Response.status(
				Response.Status.NOT_FOUND
			).build();
		}

		Captcha captcha = CaptchaUtil.getCaptcha();

		StreamingOutput streamingOutput =
			outputStream -> captcha.serveImageOutputStream(
				_httpServletRequest, outputStream);

		return Response.ok(
			streamingOutput
		).header(
			"content-disposition",
			"attachment; filename=" + StringUtil.randomString() + ".png"
		).build();
	}

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Context
	private Company _contextCompany;

	@Context
	private HttpServletRequest _httpServletRequest;

}