package it.dedagroup.invoices.info;

import it.dedagroup.invoices.model.Invoice;
import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.field.type.NumberInfoFieldType;
import com.liferay.info.field.type.TextInfoFieldType;
import com.liferay.info.item.InfoItemFieldValues;
import com.liferay.info.item.provider.InfoItemFieldValuesProvider;
import org.osgi.service.component.annotations.Component;

@Component(
        service = InfoItemFieldValuesProvider.class,
        property = "item.class.name=it.dedagroup.invoices.model.Invoice"
)
public class InvoiceInfoItemFieldValuesProvider
        implements InfoItemFieldValuesProvider<Invoice> {

    private static final InfoField<NumberInfoFieldType> INVOICE_ID = InfoField
            .builder()
            .infoFieldType(NumberInfoFieldType.INSTANCE)
            .namespace("invoice")
            .name("invoiceId")
            .build();

    private static final InfoField<NumberInfoFieldType> USER_ID = InfoField
            .builder()
            .infoFieldType(NumberInfoFieldType.INSTANCE)
            .namespace("invoice")
            .name("userId")
            .build();

    private static final InfoField<TextInfoFieldType> INVOICE_VALUE = InfoField
            .builder()
            .infoFieldType(TextInfoFieldType.INSTANCE)
            .namespace("invoice")
            .name("value")
            .build();

    @Override
    public InfoItemFieldValues getInfoItemFieldValues(Invoice invoice) {
        return InfoItemFieldValues.builder()
                .infoFieldValue(new InfoFieldValue<>(INVOICE_ID, invoice.getInvoiceId()))
                .infoFieldValue(new InfoFieldValue<>(USER_ID, invoice.getUserId()))
                .infoFieldValue(new InfoFieldValue<>(INVOICE_VALUE, invoice.getValue().toPlainString()))
                .build();
    }
}
