package it.dedagroup.invoices.model;

import java.math.BigDecimal;

public class Invoice {

    private final long invoiceId;
    private final BigDecimal invoiceValue;
    private final long userId;

    public Invoice(long invoiceId, long userId, BigDecimal invoiceValue) {
        this.invoiceId = invoiceId;
        this.invoiceValue = invoiceValue;
        this.userId = userId;
    }

    public long getInvoiceId() {
        return invoiceId;
    }

    public BigDecimal getInvoiceValue() {
        return invoiceValue;
    }

    public long getUserId() {
        return userId;
    }
}
