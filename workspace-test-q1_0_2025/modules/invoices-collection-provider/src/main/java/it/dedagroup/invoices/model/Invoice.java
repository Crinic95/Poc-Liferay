package it.dedagroup.invoices.model;

import java.math.BigDecimal;

public class Invoice {

    private final long invoiceId;
    private final long userId;
    private final BigDecimal value;

    public Invoice(long invoiceId, long userId, BigDecimal importo) {
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.value = importo;
    }

    public long getInvoiceId() {
        return invoiceId;
    }

    public long getUserId() {
        return userId;
    }

    public BigDecimal getValue() {
        return value;
    }
}
