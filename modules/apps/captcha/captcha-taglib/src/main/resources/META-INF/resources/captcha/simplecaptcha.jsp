<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/captcha/init.jsp" %>

<%
String url = (String)request.getAttribute("liferay-captcha:captcha:url");
String portletId = (String)request.getAttribute("liferay-captcha:captcha:portletId");
boolean headlessAPI = true;

if (!url.isEmpty()) {
	url = HtmlUtil.escapeAttribute(HttpComponentsUtil.addParameter(url, "t", String.valueOf(System.currentTimeMillis())));
	headlessAPI = false;
}
%>

<c:if test="<%= captchaEnabled %>">
	<div class="my-3 taglib-captcha">
		<img alt="<liferay-ui:message escapeAttribute="<%= true %>" key="text-to-identify" />" class="captcha d-inline-block mb-2" id="<portlet:namespace />captcha" src="<%= url %>" />

		<liferay-ui:icon
			cssClass="align-top d-inline-block refresh"
			icon="reload"
			id="refreshCaptcha"
			label="<%= false %>"
			localizeMessage="<%= true %>"
			markupView="lexicon"
			message="refresh-captcha"
			url="javascript:void(0);"
		/>

		<aui:input ignoreRequestValue="<%= true %>" label="text-verification" name="captchaText" required="<%= true %>" size="10" type="text" value="" />
	</div>

	<aui:script>
		var hasEventAttached = false;
		var refreshCaptcha = document.getElementById('<portlet:namespace />refreshCaptcha');
		var captcha = document.getElementById('<portlet:namespace />captcha');
		var simpleCaptchaURL = '/o/captcha/v1.0/simple?portletId=<%= portletId %>';

		function attachEvent() {
			if (refreshCaptcha && !hasEventAttached) {
				hasEventAttached = true;
				refreshCaptcha.addEventListener('click', handleCaptchaRefresh);
			}
		}

		function handleCaptchaRefresh() {
			if (<%= headlessAPI %>) {
				return getCaptchaSourceImage(simpleCaptchaURL);
			}

			let url = Liferay.Util.addParams('t=' + Date.now(), '<%= HtmlUtil.escapeJS(url) %>');
			captcha.setAttribute('src', url);
		}

		function getCaptchaSourceImage(url) {
			Liferay.Util.fetch(url, {
				method: 'GET',
			})
			.then((response) => {
				if (!response.ok) {
					throw new Error();
				}

				return response.json();
			})
			.then((response) => {
				let outputImg = document.createElement('img');
				outputImg.src = response.image;

				if (captcha) {
					captcha.setAttribute('src', outputImg.src);
				}
			})
			.catch((error) => {
				console.error('An error occurred:', error);
			});
		}

		attachEvent();

		if (<%= url.isEmpty() %>) {
			getCaptchaSourceImage(simpleCaptchaURL);
		}

		Liferay.on('<portlet:namespace />simplecaptcha_attachEvent', attachEvent);
	</aui:script>
</c:if>