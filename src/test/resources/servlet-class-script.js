var config;
function init(servletConfig) {
    servletConfig.addInitParameter("init-parameter", "init-value");
    config = servletConfig;
}
function service(request, response) {
    response.getOutputStream().print("text1");
}
function destroy() {
    config.addInitParameter("destroy-parameter", "destroy-value");
}