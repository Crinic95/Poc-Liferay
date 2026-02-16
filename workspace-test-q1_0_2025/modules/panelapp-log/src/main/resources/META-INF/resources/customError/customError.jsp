<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme"%>
<%@ page contentType="text/html"%>
<%@ page import="com.liferay.portal.kernel.theme.ThemeDisplay"%>
<%@ page import="com.liferay.portal.kernel.util.WebKeys"%>
<%
ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
%>
<!DOCTYPE html>
<html lang="it">
<head>
<meta charset="UTF-8">
<title>Accesso Negato</title>
</head>

<body>

	<div class="container-fluid">
		<div class="alert alert-warning" role="alert">
			Non possibile accedere a Log Console Liferay poichnon hai i
			permessi necessari <a href="<%=themeDisplay.getURLHome()%>"
				class="btn btn-warning ml-3">Torna alla Home</a>
		</div>
	</div>

</body>
</html>