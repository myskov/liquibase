package liquibase.preconditions;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.JDBCException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.log.LogFactory;
import liquibase.util.StringUtils;

public class ViewExistsPrecondition implements Precondition {
    static final protected Logger log = LogFactory.getLogger();

    private String schemaName;
    private String viewName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        // Use DatabaseMetaData to query db's data dictionary
        DatabaseConnection conn = database.getConnection();
        ResultSet views = null;
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            views = dbm.getTables(
                    database.convertRequestedSchemaToCatalog(getSchemaName()),
                    database.convertRequestedSchemaToSchema(getSchemaName()),
                    getViewName(),
                    new String[]{"VIEW"}
            );
            if (!views.next()) {
                throw new PreconditionFailedException("View "+database.escapeStringForDatabase(getViewName())+" does not exist", changeLog, this);
            }
        } catch (JDBCException je) {
            throw new PreconditionErrorException(je, changeLog, this);
        } catch (SQLException se) {
            throw new PreconditionErrorException(se, changeLog, this);
        } finally {
            if (views != null) {
                try {
                    views.close();
                } catch (SQLException e) {
                    log.warning("Error closing result set: " + e.getMessage());
                }
            }
        }
    }

    public String getTagName() {
        return "viewExists";
    }
}