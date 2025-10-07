package it.dedagroup.invoices.info;

import it.dedagroup.invoices.model.Invoice;

import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemClassDetails;
import com.liferay.info.item.InfoItemDetails;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.item.provider.InfoItemDetailsProvider;
import org.osgi.service.component.annotations.Component;

@Component(
        service = InfoItemDetailsProvider.class,
        property = "item.class.name=it.dedagroup.invoices.model.Invoice"
)
public class InvoiceInfoItemDetailsProvider
        implements InfoItemDetailsProvider<Invoice> {

    @Override
    public InfoItemClassDetails getInfoItemClassDetails() {
        return new InfoItemClassDetails(Invoice.class.getName());
    }

    @Override
    public InfoItemDetails getInfoItemDetails(Invoice invoice) {
        InfoItemReference reference = new InfoItemReference(
                Invoice.class.getName(),
                new ClassPKInfoItemIdentifier(invoice.getInvoiceId())
        );

        return new InfoItemDetails(getInfoItemClassDetails(), reference);
    }
}
