var config;
function init(filterConfig) {
    filterConfig.addInitParameter("init-parameter", "init-value");
    config = filterConfig;
}
function doFilter(request, response, chain) {
    response.getOutputStream().print("text1");
}
function destroy() {
    config.addInitParameter("destroy-parameter", "destroy-value");
}