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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.Preprocessor;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

@Component(service = {Filter.class, Preprocessor.class},
        property = {
                "path=/",
                "osgi.http.whiteboard.filter.regex=/.*",
                "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=*)",
                "service.ranking:Integer=1",
                "service.description:String=CORS Private Network filter",
                "sling.filter.request.pattern=/universal-editor/.*",
                "sling.filter.scope=REQUEST",
        })
@Designate(ocd = UniversalEditorServiceProxyFilter.Config.class)
public class UniversalEditorServiceProxyFilter implements Filter, Preprocessor {

    private final static String ROOT = "/universal-editor";
    private String endpoint;

    @Reference
    private HttpClientBuilderFactory httpClientBuilderFactory;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if(!StringUtils.equals(httpRequest.getMethod(), "POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!StringUtils.startsWith(httpRequest.getPathInfo(), ROOT)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpClientBuilder builder = httpClientBuilderFactory.newBuilder();
        HttpClient httpClient = null;
        try {
            httpClient = builder.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }

        String destination = endpoint + httpRequest.getPathInfo().replaceAll(ROOT, "");
        HttpPost ueRequest = new HttpPost(destination);
        httpRequest.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            httpRequest.getHeaders(headerName).asIterator().forEachRemaining(headerValue -> {
                if (!StringUtils.equalsAnyIgnoreCase(headerName, "content-length")) {
                    ueRequest.addHeader(headerName, headerValue);
                }
            });
        });

        ueRequest.setEntity(new InputStreamEntity(request.getInputStream()));
        HttpResponse ueResponse = httpClient.execute(ueRequest);

        Arrays.stream(ueResponse.getAllHeaders()).iterator().forEachRemaining(header -> {
            httpResponse.setHeader(header.getName(), header.getValue());
        });

        httpResponse.setContentType("application/json");
        ueResponse.getEntity().writeTo(httpResponse.getOutputStream());
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    @Activate
    private void activate(final BundleContext bundleContext,
                          final UniversalEditorServiceProxyFilter.Config config,
                          final Map<String, Object> props) throws IOException, InvalidSyntaxException {
        this.endpoint = config.endpoint();
    }


    @ObjectClassDefinition(name = "Universal Editor Service Proxy - USE ONLY ON SDKS",
            description = "Proxy for the Universal Editor Service")
    public @interface Config {
        @AttributeDefinition(name = "Universal Editor Service Endpoint", description = "Universal Editor Service the Filter shall proxy to")
        String endpoint() default "http://localhost:8080";
    }
}