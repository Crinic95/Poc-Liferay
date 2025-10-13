package it.dedagroup.invoices.info;

import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.item.InfoItemFieldValues;
import com.liferay.info.item.provider.InfoItemFieldValuesProvider;
import it.dedagroup.invoices.model.Invoice;
import org.osgi.service.component.annotations.Component;

import static it.dedagroup.invoices.constants.InvoiceInfoFields.*;

@Component(
        service = InfoItemFieldValuesProvider.class,
        property = "item.class.name=it.dedagroup.invoices.model.Invoice"
)
public class InvoiceInfoItemFieldValuesProvider
        implements InfoItemFieldValuesProvider<Invoice> {

    @Override
    public InfoItemFieldValues getInfoItemFieldValues(Invoice invoice) {
        return InfoItemFieldValues.builder()
                .infoFieldValue(new InfoFieldValue<>(INVOICE_ID, String.valueOf(invoice.getInvoiceId())))
                .infoFieldValue(new InfoFieldValue<>(USER_ID, String.valueOf(invoice.getUserId())))
                .infoFieldValue(new InfoFieldValue<>(INVOICE_VALUE, invoice.getInvoiceValue().toPlainString()))
                .infoFieldValue(new InfoFieldValue<>(TYPE, invoice.getType()))
                .infoFieldValue(new InfoFieldValue<>(STATUS, invoice.getStatus()))
                .build();
    }
}
