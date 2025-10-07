package it.dedagroup.invoices.info;

import com.liferay.info.field.InfoFieldSet;
import com.liferay.info.form.InfoForm;
import com.liferay.info.item.provider.InfoItemFormProvider;
import com.liferay.info.localized.InfoLocalizedValue;
import it.dedagroup.invoices.model.Invoice;
import org.osgi.service.component.annotations.Component;

import static it.dedagroup.invoices.constants.InvoiceInfoFields.*;

@Component(
        service = InfoItemFormProvider.class,
        property = "item.class.name=it.dedagroup.invoices.model.Invoice"
)
public class InvoiceInfoItemFormProvider implements InfoItemFormProvider<Invoice> {

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
    public InfoForm getInfoForm(Invoice invoice) { return getInfoForm(); }
}
