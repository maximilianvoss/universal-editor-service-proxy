# Universal Editor Service Proxy

This bundle provides a proxy to the Universal Editor Service (UES) to speed up the local development setup.
It proxies requests from the local AEM instance to a local running Universal Editor Service. 
By this, users won't need to provide Authentication tokens or similar as requests will be fed by AEM's `login-token` cookie.
This works with AEMCS as well as AEM 6.5 local development instances.

**!!!DO ONLY USE THIS ON YOUR LOCAL AEM SDK OR AEM 6.5 INSTANCE!!!**  
**!!!DON'T PUT THIS INTO ANY PRODUCTION SETUP!!!**

## Building this Package
```shell
mvn clean install
```

## Install
* Build the bundle yourself

or

* Download the latest released bundle from https://github.com/maximilianvoss/universal-editor-service-proxy/releases/latest

And then

* Install the `core/target/universal-editor-service-proxy.core-*.jar` file directly into the Felix console http://localhost:4502/system/console/bundles. 

## Setting up the whole development environment

### AEM SDK
1. Ensure your AEM SDK is running on HTTPS. Either via AEM SSL configuration or a local https proxy.
2. Basic OSGi setup 
    * Remove `X-FRAME-Options=SAMEORIGIN` header from the OSGi config for org.apache.sling.engine.impl.SlingMainServlet 
    * Set `token.samesite.cookie.attr`=`Partitioned` for the OSGi config for com.day.crx.security.token.impl.impl.TokenAuthenticationHandler
    * See: https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/implementing/developing/universal-editor/developer-overview#samesite-cookies for more details.

### Universal Editor Service
1. Download the latest Universal Editor Service from [Software Distribution](https://experience.adobe.com/#/downloads/content/software-distribution/en/aemcloud.html)
2. Unzip the downloaded file
3. Create a .env file or set following environment variables
    * `UES_TLS_REJECT_UNAUTHORIZED=false`
    * `UES_CORS_PRIVATE_NETWORK=true`
    * `UES_DISABLE_IMS_VALIDATION=true`
4. Run `node universal-editor-service.cjs`

More details can be found at [Running Your Own Universal Editor Service](https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/implementing/developing/universal-editor/local-dev)

### Universal Editor Service Proxy
1. Download the JAR File from Releases
2. Install the bundle into your local AEM instance
3. Configure the `Universal Editor Service Proxy - USE ONLY ON SDKS` service if you run the Universal Editor Service elsewhere at http://localhost:4502/system/console/configMgr/ 

### Instrument your page
1. Do your normal required instrumentation on the page.
2. Change the protocol to use `aem65` instead of `aem` as it will then also use the local AEM pickers
    * Change this `<meta name="urn:adobe:aue:system:aemconnection" content="aem:https://localhost:8443">` to `<meta name="urn:adobe:aue:system:aemconnection" content="aem65:https://localhost:8443">`
3. Add a new service endpoint (**must be removed or adapted before pushing to production or any AEM Cloud instance)
   * By adding following meta tag: `<meta name="urn:adobe:aue:config:service" content="https://localhost:8443/universal-editor">`

### Extensions
1. Go the [Extension Manager](https://experience.adobe.com/#/aem/extension-manager/universal-editor)
2. Search and enable the Extension: `AEM Universal Editor Dev Login Extension`

## Run it
1. Open the Universal Editor
2. Paste your AEM URL or your external rendered URL into the Universal Editor
3. Click on `Developer Login` in the header menu
4. Put your AEM credentials into the login form 
5. Refresh the Page

Now you should see all requests going to AEM having the `login-token` cookie set.