package it.dedagroup.invoices.service;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.util.Validator;
import it.dedagroup.invoices.model.Invoice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {

    private static final String SELECT_BY_ID =
            "SELECT invoiceId, userId, invoiceValue, type_, status_ " +
                    "FROM test_q1_0_2025_db.invoice WHERE invoiceId = ?";

    private static final String BASE_SELECT =
            "SELECT invoiceId, userId, invoiceValue, type_, status_ " +
                    "FROM test_q1_0_2025_db.invoice WHERE userId = ? ";

    private static final String ORDER_BY = " ORDER BY invoiceId DESC";

    public static List<Invoice> findByUserId(long userId) throws Exception {
        return findByUserIdAndType(userId, null);
    }

    public static List<Invoice> findByUserIdAndType(long userId, String type) throws Exception {
        List<Invoice> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(BASE_SELECT);
        boolean hasType = (type != null && !type.isBlank());
        if (hasType) {
            sql.append(" AND type_ = ? ");
        }
        sql.append(ORDER_BY);

        try (Connection con = DataAccess.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int i = 1;
            ps.setLong(i++, userId);
            if (hasType) {
                ps.setString(i++, type.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Invoice(
                            rs.getLong("invoiceId"),
                            rs.getLong("userId"),
                            rs.getBigDecimal("invoiceValue"),
                            rs.getString("type_"),
                            rs.getString("status_")
                    ));
                }
            }
        }

        return list;
    }

    public static List<Invoice> findByUserTypeStatus(long userId, String type, String status) throws Exception {
        StringBuilder sql = new StringBuilder(
                "SELECT invoiceId, userId, invoiceValue, type_, status_ " +
                        "FROM test_q1_0_2025_db.invoice WHERE userId = ?"
        );

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (Validator.isNotNull(type)) {
            sql.append(" AND type_ = ?");
            params.add(type);
        }
        if (Validator.isNotNull(status)) {
            sql.append(" AND status_ = ?");
            params.add(status);
        }

        sql.append(" ORDER BY invoiceId DESC");

        List<Invoice> list = new ArrayList<>();
        try (Connection con = DataAccess.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Invoice(
                            rs.getLong("invoiceId"),
                            rs.getLong("userId"),
                            rs.getBigDecimal("invoiceValue"),
                            rs.getString("type_"),
                            rs.getString("status_")
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
                            rs.getBigDecimal("invoiceValue"),
                            rs.getString("type_"),
                            rs.getString("status_")
                    );
                }
            }
        }
        return null;
    }

}
