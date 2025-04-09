/*
 *  Copyright 2025 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.adobe.aem.sites.universaleditor;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.Preprocessor;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = {Filter.class, Preprocessor.class},
        property = {
                "path=/",
                "osgi.http.whiteboard.filter.regex=/.*",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=*)",
                "service.ranking:Integer=1",
                "service.description:String=CORS Private Network filter",
                "sling.filter.pattern=/.*",
                "sling.filter.scope=REQUEST",
        })
public class CORSPrivateNetworkFilter implements Filter, Preprocessor {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {
        if(response instanceof HttpServletResponse) {
            final HttpServletResponse slingResponse = (HttpServletResponse) response;
            slingResponse.setHeader("Access-Control-Allow-Private-Network", "true");
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}