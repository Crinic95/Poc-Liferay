package it.dedagroup.invoices.filter;

import com.liferay.info.item.renderer.InfoItemRenderer;
import com.liferay.info.list.renderer.InfoListRenderer;
import com.liferay.petra.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.template.*;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import it.dedagroup.invoices.collection.InvoicesCollectionProvider;
import it.dedagroup.invoices.model.Invoice;
import org.osgi.service.component.annotations.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component(
        service = InfoListRenderer.class,
        property = {
                "item.class.name=it.dedagroup.invoices.model.Invoice",
                "info.list.renderer.key=forniture-slider",
                "info.list.renderer.label=Slider (Forniture)"
        }
)
public class InvoicesSliderInfoListRenderer implements InfoListRenderer<Invoice> {

    private static final Log _log = LogFactoryUtil.getLog(InvoicesSliderInfoListRenderer.class);

    @Override
    public void render(
            List<Invoice> items,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            PrintWriter out = response.getWriter();

            String ftl = readResource("/META-INF/resources/templates/slider.ftl");

            TemplateManager tm = TemplateManagerUtil.getTemplateManager(TemplateConstants.LANG_TYPE_FTL);
            TemplateResource tr = new StringTemplateResource("invoices-slider.ftl", ftl);
            Template template = tm.getTemplate(tr, false);
            template.put("items", items);

            UnsyncStringWriter sw = new UnsyncStringWriter();
            template.processTemplate(sw);
            out.write(sw.toString());

        } catch (Exception e) {
            _log.error("Errore durante il rendering del template delle forniture", e);
            throw new RuntimeException("Errore nel render del template slider", e);
        }
    }

    @Override
    public List<InfoItemRenderer<?>> getAvailableInfoItemRenderers() {
        return Collections.emptyList();
    }

    private static String readResource(String path) throws IOException {
        try (var is = InvoicesSliderInfoListRenderer.class.getResourceAsStream(path)) {
            if (is == null) throw new IOException("Template not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}