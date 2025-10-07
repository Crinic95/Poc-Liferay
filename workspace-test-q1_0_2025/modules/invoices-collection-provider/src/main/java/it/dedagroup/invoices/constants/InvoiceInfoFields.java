package it.dedagroup.invoices.constants;

import com.liferay.info.field.InfoField;
import com.liferay.info.field.type.TextInfoFieldType;
import com.liferay.info.localized.InfoLocalizedValue;

public class InvoiceInfoFields {

    public static final InfoField<TextInfoFieldType> INVOICE_ID = InfoField.builder()
            .infoFieldType(TextInfoFieldType.INSTANCE)
            .namespace("invoice")
            .name("invoiceId")
            .labelInfoLocalizedValue(InfoLocalizedValue.singleValue("Invoice ID"))
            .build();

    public static final InfoField<TextInfoFieldType> INVOICE_VALUE = InfoField.builder()
            .infoFieldType(TextInfoFieldType.INSTANCE)
            .namespace("invoice")
            .name("invoiceValue")
            .labelInfoLocalizedValue(InfoLocalizedValue.singleValue("Invoice Value"))
            .build();

    public static final InfoField<TextInfoFieldType> USER_ID = InfoField.builder()
            .infoFieldType(TextInfoFieldType.INSTANCE)
            .namespace("invoice")
            .name("userId")
            .labelInfoLocalizedValue(InfoLocalizedValue.singleValue("User ID"))
            .build();
}
