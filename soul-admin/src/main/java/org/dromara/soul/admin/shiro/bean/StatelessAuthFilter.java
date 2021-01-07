/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.soul.admin.shiro.bean;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * custom Stateless AccessControlFilter.
 *
 * @author YuI
 **/
@Slf4j
public class StatelessAuthFilter extends AccessControlFilter {

    private static final String HEAD_TOKEN = "X-Access-Token";

    @Override
    protected boolean isAccessAllowed(final ServletRequest servletRequest, final ServletResponse servletResponse,
                                      final Object o) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest servletRequest, final ServletResponse servletResponse)
            throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String tokenValue = httpServletRequest.getHeader(HEAD_TOKEN);

        if (StringUtils.isEmpty(tokenValue)) {
            onLoginFail(servletResponse);
            return false;
        }

        StatelessToken token = new StatelessToken();
        token.setToken(tokenValue);

        Subject subject = getSubject(servletRequest, servletResponse);
        try {
            subject.login(token);
        } catch (AuthenticationException e) {
            log.warn("token is warning, token : {}", token, e);
            onLoginFail(servletResponse);
            return false;
        }

        return true;
    }

    /**
     * union response same result form exception.
     * todo: will change to global exception intercept.
     *
     * @param response {@link ServletResponse}
     */
    private void onLoginFail(final ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Content-type", "text/html;charset=UTF-8");
        httpResponse.setCharacterEncoding("utf-8");
        wrapCorsResponse(httpResponse);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write("token失效");
    }

    /**
     * add cors.
     *
     * @param response {@link ServletResponse}
     */
    private void wrapCorsResponse(final HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.addHeader("Access-Control-Max-Age", "1800");
    }
}
