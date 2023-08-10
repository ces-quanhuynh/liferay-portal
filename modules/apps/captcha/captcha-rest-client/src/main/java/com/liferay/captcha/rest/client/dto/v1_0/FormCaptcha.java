/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.captcha.rest.client.dto.v1_0;

import com.liferay.captcha.rest.client.function.UnsafeSupplier;
import com.liferay.captcha.rest.client.serdes.v1_0.FormCaptchaSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author Loc Pham
 * @generated
 */
@Generated("")
public class FormCaptcha implements Cloneable, Serializable {

	public static FormCaptcha toDTO(String json) {
		return FormCaptchaSerDes.toDTO(json);
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public void setAnswer(
		UnsafeSupplier<String, Exception> answerUnsafeSupplier) {

		try {
			answer = answerUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String answer;

	public String getCaptchaToken() {
		return captchaToken;
	}

	public void setCaptchaToken(String captchaToken) {
		this.captchaToken = captchaToken;
	}

	public void setCaptchaToken(
		UnsafeSupplier<String, Exception> captchaTokenUnsafeSupplier) {

		try {
			captchaToken = captchaTokenUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String captchaToken;

	@Override
	public FormCaptcha clone() throws CloneNotSupportedException {
		return (FormCaptcha)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof FormCaptcha)) {
			return false;
		}

		FormCaptcha formCaptcha = (FormCaptcha)object;

		return Objects.equals(toString(), formCaptcha.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return FormCaptchaSerDes.toJSON(this);
	}

}