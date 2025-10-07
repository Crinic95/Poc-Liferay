package it.dedagroup.invoices.collection;

import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.pagination.InfoPage;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import it.dedagroup.invoices.model.Invoice;
import it.dedagroup.invoices.service.InvoiceRepository;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
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

        List<Invoice> invoices = new ArrayList<>();
        try {
            invoices = InvoiceRepository.findByUserId(userId);

            if(_log.isDebugEnabled()) {
                _log.debug("Loaded " + invoices.size() + " invoices for userId=" + userId);
            }
        } catch (Exception e) {
            _log.error("Error loading invoices for userId=" + userId, e);
        }

        return InfoPage.of(invoices);
    }

    @Override
    public String getLabel(Locale locale) {
        return "Invoices (Collection Provider)";
    }
}