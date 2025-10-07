package it.dedagroup.invoices.info;

import com.liferay.info.localized.InfoLocalizedValue;
import it.dedagroup.invoices.model.Invoice;

import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldSet;
import com.liferay.info.field.type.NumberInfoFieldType;
import com.liferay.info.field.type.TextInfoFieldType;
import com.liferay.info.form.InfoForm;
import com.liferay.info.item.provider.InfoItemFormProvider;
import org.osgi.service.component.annotations.Component;

@Component(
        service = InfoItemFormProvider.class,
        property = "item.class.name=it.dedagroup.invoices.model.Invoice"
)
public class InvoiceInfoItemFormProvider implements InfoItemFormProvider<Invoice> {

    private static final InfoField<NumberInfoFieldType> INVOICE_ID = InfoField.builder()
            .infoFieldType(NumberInfoFieldType.INSTANCE)
            .namespace("invoice")
            .name("invoiceId")
            .build();

    private static final InfoField<NumberInfoFieldType> USER_ID = InfoField.builder()
            .infoFieldType(NumberInfoFieldType.INSTANCE)
            .namespace("invoice")
            .name("userId")
            .build();

    private static final InfoField<TextInfoFieldType> INVOICE_VALUE = InfoField.builder()
            .infoFieldType(TextInfoFieldType.INSTANCE)
            .namespace("invoice")
            .name("value")
            .build();

    private static final InfoFieldSet FIELD_SET = InfoFieldSet.builder()
            .name("invoiceFields")
            .labelInfoLocalizedValue(InfoLocalizedValue.singleValue("Invoice Fields"))
            .infoFieldSetEntry(INVOICE_ID)
            .infoFieldSetEntry(USER_ID)
            .infoFieldSetEntry(INVOICE_VALUE)
            .build();

    @Override
    public InfoForm getInfoForm() {
        return InfoForm.builder()
                .name("invoiceForm")
                .infoFieldSetEntry(FIELD_SET)
                .build();
    }

    @Override
    public InfoForm getInfoForm(Invoice invoice) {
        return getInfoForm();
    }
}
