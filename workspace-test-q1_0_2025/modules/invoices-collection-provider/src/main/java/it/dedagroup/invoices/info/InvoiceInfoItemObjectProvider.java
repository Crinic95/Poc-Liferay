package it.dedagroup.invoices.info;

import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.provider.InfoItemObjectProvider;
import it.dedagroup.invoices.model.Invoice;
import it.dedagroup.invoices.service.InvoiceRepository;
import org.osgi.service.component.annotations.Component;

@Component(
        service = InfoItemObjectProvider.class,
        property = "item.class.name=it.dedagroup.invoices.model.Invoice"
)
public class InvoiceInfoItemObjectProvider implements InfoItemObjectProvider<Invoice> {

    @Override
    public Invoice getInfoItem(InfoItemIdentifier infoItemIdentifier) {

        long invoiceId = ((ClassPKInfoItemIdentifier) infoItemIdentifier).getClassPK();
        Invoice invoice = null;

        try {
            invoice = InvoiceRepository.getById(invoiceId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return invoice;
    }
}
