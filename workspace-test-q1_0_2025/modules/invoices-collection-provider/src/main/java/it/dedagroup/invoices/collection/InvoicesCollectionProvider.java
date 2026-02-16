package it.dedagroup.invoices.collection;

import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.filter.InfoFilterProvider;
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
import it.dedagroup.invoices.filter.InvoiceStatusFilter;
import it.dedagroup.invoices.filter.InvoiceTypeFilter;
import it.dedagroup.invoices.model.Invoice;
import it.dedagroup.invoices.service.InvoiceRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
    private volatile int _lastCount = 0;

    @Override
    public InfoPage<Invoice> getCollectionInfoPage(CollectionQuery collectionQuery) {

        long userId = PrincipalThreadLocal.getUserId();
        if (userId <= 0 && PermissionThreadLocal.getPermissionChecker() != null) {
            userId = PermissionThreadLocal.getPermissionChecker().getUserId();
        }

        ServiceContext serviceContext = ServiceContextThreadLocal.getServiceContext();
        HttpServletRequest original = (serviceContext != null) ? PortalUtil.getOriginalServletRequest(serviceContext.getRequest()) : null;

        String type = null;
        String status = null;

        if (original != null) {
            Map<String, String[]> values = new HashMap<>();
            String typeRaw = ParamUtil.getString(original, "invoiceType", null);
            String statusRaw = ParamUtil.getString(original, "invoiceStatus", null);

            if (Validator.isNotNull(typeRaw)) {
                values.put("invoiceType",   new String[]{typeRaw});
            }
            if (Validator.isNotNull(statusRaw)) {
                values.put("invoiceStatus", new String[]{statusRaw});
            }

            if (invoiceTypeFilterProvider != null) {
                it.dedagroup.invoices.filter.InvoiceTypeFilter typeFilter =
                        invoiceTypeFilterProvider.create(values);
                if (typeFilter != null && Validator.isNotNull(typeFilter.getType())) {
                    type = typeFilter.getType();
                }
            }
            if (invoiceStatusFilterProvider != null) {
                it.dedagroup.invoices.filter.InvoiceStatusFilter statusFilter =
                        invoiceStatusFilterProvider.create(values);
                if (statusFilter != null && Validator.isNotNull(statusFilter.getStatus())) {
                    status = statusFilter.getStatus();
                }
            }
        }

        _lastCount = getCount(userId, type, status);

        List<Invoice> invoices;
        try {
            invoices = InvoiceRepository.findByUserTypeStatus(userId, type, status);
        } catch (Exception e) {
            _log.error("Error loading invoices", e);
            invoices = java.util.Collections.emptyList();
        }

        return InfoPage.of(invoices);
    }

    public int getCount(long userId, String type, String status) {
        try {
            return InvoiceRepository.countByUserTypeStatus(userId, type, status);
        } catch (Exception e) {
            _log.error("Error counting invoices", e);
            return 0;
        }
    }

    public int getActiveCount() {
        return _lastCount;
    }

    @Override
    public String getLabel(Locale locale) {
        return "Invoices (Collection Provider)";
    }

    @Reference(target = "(&(item.class.name=it.dedagroup.invoices.model.Invoice)(key=invoiceStatus))")
    private InfoFilterProvider<InvoiceStatusFilter> invoiceStatusFilterProvider;

    @Reference(target = "(&(item.class.name=it.dedagroup.invoices.model.Invoice)(key=invoiceType))")
    private InfoFilterProvider<InvoiceTypeFilter> invoiceTypeFilterProvider;

}