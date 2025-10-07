package it.dedagroup.invoices.collection;

import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.pagination.InfoPage;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import it.dedagroup.invoices.model.Invoice;
import org.osgi.service.component.annotations.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author NCLCST95H
 */
@Component(
        service = InfoCollectionProvider.class,
        property = {
                "item.class.name=it.dedagroup.invoices.model.Invoice",
                "collection.key=invoices-by-current-user"
        }
)
public class InvoicesCollectionProvider implements InfoCollectionProvider<Invoice> {

    @Override
    public InfoPage<Invoice> getCollectionInfoPage(CollectionQuery collectionQuery) {

        long userId = PrincipalThreadLocal.getUserId();
        if (userId <= 0 && PermissionThreadLocal.getPermissionChecker() != null) {
            userId = PermissionThreadLocal.getPermissionChecker().getUserId();
        }

        List<Invoice> invoices = new ArrayList<>();

        String sql = "SELECT invoiceId, userId, invoiceValue FROM test_q1_0_2025_db.invoice WHERE userId = ? ORDER BY invoiceId DESC";

        try (Connection con = DataAccess.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    invoices.add(new Invoice(
                            rs.getLong("invoiceId"),
                            rs.getLong("userId"),
                            rs.getBigDecimal("invoiceValue")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return InfoPage.of(invoices);
    }

    @Override
    public String getLabel(Locale locale) {
        return "Invoices (Collection Provider)";
    }
}