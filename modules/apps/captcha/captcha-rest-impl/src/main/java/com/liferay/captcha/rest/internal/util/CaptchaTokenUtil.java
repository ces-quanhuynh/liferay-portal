/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.captcha.rest.internal.util;

import com.liferay.portal.kernel.captcha.CaptchaTextException;
import com.liferay.portal.kernel.encryptor.EncryptorException;
import com.liferay.portal.kernel.encryptor.EncryptorUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Date;

/**
 * @author Loc Pham
 */
public class CaptchaTokenUtil {

	public static void checkAnswer(
			Company company, String captchaToken, String answer)
		throws Exception {

		String captchaJSONString = EncryptorUtil.decrypt(
			company.getKeyObj(), captchaToken);

		JSONObject captchaJSONObject = JSONFactoryUtil.createJSONObject(
			captchaJSONString);

		if (!isValidCaptchaToken(captchaJSONObject)) {
			throw new Exception("Invalid captcha");
		}

		Date now = new Date();

		if (!StringUtil.equalsIgnoreCase(
				captchaJSONObject.getString("answer"), answer) ||
			!now.before(new Date(captchaJSONObject.getLong("tokenExpired")))) {

			throw new CaptchaTextException("Invalid answer");
		}
	}

	public static String generateCaptchaToken(Company company, String answer)
		throws EncryptorException {

		Date now = new Date();

		return EncryptorUtil.encrypt(
			company.getKeyObj(),
			JSONUtil.put(
				"answer", answer
			).put(
				"tokenExpired", now.getTime() + _TIME_EXPIRED_CAPTCHA_TOKEN
			).toString());
	}

	public static boolean isValidCaptchaToken(JSONObject captchaJSONObject) {
		if ((captchaJSONObject == null) ||
			((captchaJSONObject.getString("answer") == null) &&
			 (captchaJSONObject.get("tokenExpired") == null))) {

			return false;
		}

		return true;
	}

	private static final long _TIME_EXPIRED_CAPTCHA_TOKEN = 5 * 60000;

}