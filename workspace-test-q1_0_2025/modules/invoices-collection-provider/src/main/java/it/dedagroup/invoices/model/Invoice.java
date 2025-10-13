package it.dedagroup.invoices.model;

import java.math.BigDecimal;

public class Invoice {

    private final long invoiceId;
    private final BigDecimal invoiceValue;
    private final long userId;
    private final String type;
    private final String status;

    public Invoice(long invoiceId, long userId, BigDecimal invoiceValue, String type, String status) {
        this.invoiceId = invoiceId;
        this.invoiceValue = invoiceValue;
        this.userId = userId;
        this.type = type;
        this.status = status;
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

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }
}
