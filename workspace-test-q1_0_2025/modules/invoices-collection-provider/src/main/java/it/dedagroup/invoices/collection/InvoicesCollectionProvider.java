package it.dedagroup.invoices.collection;

import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.pagination.InfoPage;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import it.dedagroup.invoices.filter.InvoiceTypeFilter;
import it.dedagroup.invoices.model.Invoice;
import it.dedagroup.invoices.service.InvoiceRepository;
import org.osgi.service.component.annotations.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author NCLCST95H
 */
@Component(
        service = InfoCollectionProvider.class,
        property = {
                "item.class.name=it.dedagroup.invoices.model.Invoice",
                "collection.key=invoices-by-current-user"
        }
)
public class InvoicesCollectionProvider implements InfoCollectionProvider<Invoice> {

    private static final Log _log = LogFactoryUtil.getLog(InvoicesCollectionProvider.class);

    @Override
    public InfoPage<Invoice> getCollectionInfoPage(CollectionQuery collectionQuery) {

        long userId = PrincipalThreadLocal.getUserId();
        if (userId <= 0 && PermissionThreadLocal.getPermissionChecker() != null) {
            userId = PermissionThreadLocal.getPermissionChecker().getUserId();
        }

        ServiceContext sc = ServiceContextThreadLocal.getServiceContext();
        HttpServletRequest original = (sc != null) ? PortalUtil.getOriginalServletRequest(sc.getRequest()) : null;

        String type = null;
        String status = null;

        if (original != null) {
            type   = ParamUtil.getString(original, "invoiceType");
            status = ParamUtil.getString(original, "invoiceStatus");
        }

        if (Validator.isNotNull(type)) {
            if ("Pagate".equalsIgnoreCase(type)) type = "Pagata";
            if ("Scadute".equalsIgnoreCase(type)) type = "Scaduta";
        }
        if (Validator.isNotNull(status)) {
            System.out.println("status: " + status);
            if ("Aperte".equalsIgnoreCase(status)) status = "Aperta";
            if ("Chiuse".equalsIgnoreCase(status)) status = "Chiusa";
        }

        if (Validator.isNotNull(type) && !("Pagata".equals(type) || "Scaduta".equals(type))) {
            type = null;
        }
        if (Validator.isNotNull(status) && !( "Aperta".equals(status) || "Chiusa".equals(status))) {
            status = null;
        }

        List<Invoice> list;
        try {
            list = InvoiceRepository.findByUserTypeStatus(userId, type, status);
        } catch (Exception e) {
            _log.error("Error loading invoices", e);
            list = Collections.emptyList();
        }

        return InfoPage.of(list);
    }

    @Override
    public String getLabel(Locale locale) {
        return "Invoices (Collection Provider)";
    }
}