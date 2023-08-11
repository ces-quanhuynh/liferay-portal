/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.captcha.rest.internal.resource.v1_0;

import com.liferay.captcha.configuration.CaptchaConfiguration;
import com.liferay.captcha.recaptcha.ReCaptchaImpl;
import com.liferay.captcha.rest.dto.v1_0.FormCaptcha;
import com.liferay.captcha.rest.internal.util.CaptchaTokenUtil;
import com.liferay.captcha.rest.resource.v1_0.CaptchaResource;
import com.liferay.captcha.util.CaptchaUtil;
import com.liferay.portal.kernel.captcha.Captcha;
import com.liferay.portal.kernel.captcha.CaptchaTextException;
import com.liferay.portal.kernel.encryptor.EncryptorUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
	public com.liferay.captcha.rest.dto.v1_0.Captcha getSimpleCaptcha()
		throws Exception {

		_checkSimpleCaptchaConfiguration();

		Captcha captcha = CaptchaUtil.getCaptcha();

		ByteArrayOutputStream imageByteArrayOutputStream =
			new ByteArrayOutputStream();

		String captchaAnswer = captcha.serveImageOutputStream(
			_httpServletRequest, imageByteArrayOutputStream);

		String base64CaptchaImage =
			"data:image/png;base64," +
				Base64.encode(imageByteArrayOutputStream.toByteArray());

		imageByteArrayOutputStream.close();

		return new com.liferay.captcha.rest.dto.v1_0.Captcha() {
			{
				captchaToken = CaptchaTokenUtil.generateCaptchaToken(
					_contextCompany, captchaAnswer);

				image = base64CaptchaImage;
			}
		};
	}

	@Override
	public FormCaptcha validateSimpleCaptcha(FormCaptcha formCaptcha)
		throws Exception {

		_checkSimpleCaptchaConfiguration();

		try {
			CaptchaTokenUtil.checkAnswer(
				_contextCompany, formCaptcha.getCaptchaToken(),
				formCaptcha.getAnswer());
		}
		catch (CaptchaTextException captchaTextException) {
			ByteArrayOutputStream imageByteArrayOutputStream =
				new ByteArrayOutputStream();

			Captcha simpleCaptcha = CaptchaUtil.getCaptcha();

			String captchaAnswer = simpleCaptcha.serveImageOutputStream(
				_httpServletRequest, imageByteArrayOutputStream);

			String base64CaptchaImage =
				"data:image/png;base64," +
					Base64.encode(imageByteArrayOutputStream.toByteArray());

			imageByteArrayOutputStream.close();

			com.liferay.captcha.rest.dto.v1_0.Captcha captcha =
				new com.liferay.captcha.rest.dto.v1_0.Captcha();

			captcha.setCaptchaToken(
				EncryptorUtil.encrypt(
					_contextCompany.getKeyObj(),
					CaptchaTokenUtil.generateCaptchaToken(
						_contextCompany, captchaAnswer)));

			captcha.setImage(base64CaptchaImage);

			throw new NotAcceptableException(
				captchaTextException.getMessage(),
				Response.status(
					Response.Status.NOT_ACCEPTABLE
				).entity(
					captcha
				).build());
		}
		catch (Exception exception) {
			throw new BadRequestException(exception.getMessage());
		}

		return formCaptcha;
	}

	private void _checkSimpleCaptchaConfiguration() throws Exception {
		if (!FeatureFlagManagerUtil.isEnabled("LPS-185213")) {
			throw new ForbiddenException();
		}

		CaptchaConfiguration captchaConfiguration =
			_configurationProvider.getSystemConfiguration(
				CaptchaConfiguration.class);

		if (!captchaConfiguration.enableSimpleCaptchaHeadlessAPI() ||
			StringUtil.equalsIgnoreCase(
				captchaConfiguration.captchaEngine(),
				ReCaptchaImpl.class.getName())) {

			throw new ForbiddenException(
				"Not support for Simple Captcha HeadlessAPI");
		}
	}

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Context
	private Company _contextCompany;

	@Context
	private HttpServletRequest _httpServletRequest;

}