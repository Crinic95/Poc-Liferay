package it.dedagroup.invoices.service;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import it.dedagroup.invoices.model.Invoice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {

    private static final String SELECT_BY_USER =
            "SELECT invoiceId, userId, invoiceValue " +
                    "FROM test_q1_0_2025_db.invoice WHERE userId = ? ORDER BY invoiceId DESC";

    private static final String SELECT_BY_ID =
            "SELECT invoiceId, userId, invoiceValue " +
                    "FROM test_q1_0_2025_db.invoice WHERE invoiceId = ?";

    public static List<Invoice> findByUserId(long userId) throws Exception {
        List<Invoice> list = new ArrayList<>();
        try (Connection con = DataAccess.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_USER)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Invoice(
                            rs.getLong("invoiceId"),
                            rs.getLong("userId"),
                            rs.getBigDecimal("invoiceValue")
                    ));
                }
            }
        }
        return list;
    }

    public static Invoice getById(long invoiceId) throws Exception {
        try (Connection con = DataAccess.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Invoice(
                            rs.getLong("invoiceId"),
                            rs.getLong("userId"),
                            rs.getBigDecimal("invoiceValue")
                    );
                }
            }
        }
        return null;
    }

}
