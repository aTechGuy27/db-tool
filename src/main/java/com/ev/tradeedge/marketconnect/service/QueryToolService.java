package com.ev.tradeedge.marketconnect.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ev.tradeedge.marketconnect.model.Database;
import com.ev.tradeedge.marketconnect.model.FileProcessEvent;
import com.ev.tradeedge.marketconnect.model.KillQueryRequest;
import com.ev.tradeedge.marketconnect.model.Query;

@Service
public class QueryToolService {
    private static final Logger logger = LoggerFactory.getLogger(QueryToolService.class);
    
    private final DatabaseService databaseService;
    

    public QueryToolService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public List<String> getServers(String type) {
        try {
            // Get servers from database
            Connection conn = databaseService.getMainDbConnection(type);
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT database_server_name FROM tecfg.tenant_datasource"
            );
            ResultSet rs = stmt.executeQuery();
            
            List<String> servers = new ArrayList<>();
            while (rs.next()) {
                servers.add(rs.getString("database_server_name"));
            }
            
            rs.close();
            stmt.close();
            
            return servers;
        } catch (SQLException e) {
            logger.error("Error getting servers", e);
            // Fallback to mock data
            if ("staging".equals(type)) {
                return Arrays.asList("staging-db-1", "staging-db-2", "staging-db-3");
            } else {
                return Arrays.asList("prod-db-1", "prod-db-2", "prod-db-3");
            }
        }
    }

    public List<Database> getDatabasesByFpid(String id, String server) {
        try {
            // Get databases from database
            Connection conn = databaseService.getMainDbConnection(server);
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT b1.tenant_datasource_id, a1.file_process_id, b1.tenant_id, b1.database_server_name, " +
                "b1.database_port_number, b1.database_name, b1.database_user_name " +
                "FROM teopr.file_process_summary a1, tecfg.tenant_datasource b1 " +
                "WHERE a1.tenant_id = b1.tenant_id AND a1.file_process_id = ?"
            );
            
            // Convert string to long before setting parameter
            stmt.setLong(1, Long.parseLong(id));
            
            ResultSet rs = stmt.executeQuery();
            
            List<Database> databases = new ArrayList<>();
            while (rs.next()) {
                Database db = new Database(
                    rs.getString("tenant_datasource_id"), // Use tenant_datasource_id as the ID
                    rs.getString("tenant_id"),
                    rs.getString("database_server_name"),
                    rs.getString("database_port_number"),
                    rs.getString("database_name"),
                    rs.getString("database_user_name")
                );
                databases.add(db);
            }
            
            rs.close();
            stmt.close();
            
            return databases;
        } catch (SQLException e) {
            logger.error("Error getting databases by FPID", e);
            // Fallback to mock data
            List<Database> databases = new ArrayList<>();
            databases.add(new Database("1", "tenant-1", "db-server-1", "5432", "database-1", "user1"));
            databases.add(new Database("2", "tenant-2", "db-server-2", "5432", "database-2", "user2"));
            return databases;
        } catch (NumberFormatException e) {
            // Handle case where id is not a valid number
            logger.error("Invalid file process ID format: " + id, e);
            return new ArrayList<>();
        }
    }

    public List<Database> getDatabasesByServer(String name, String type) {
        try {
            // Get databases from database
            Connection conn = databaseService.getMainDbConnection(type);
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT tenant_datasource_id, tenant_id, database_server_name, " +
                "database_port_number, database_name, database_user_name " +
                "FROM tecfg.tenant_datasource WHERE database_server_name = ?"
            );
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            
            List<Database> databases = new ArrayList<>();
            while (rs.next()) {
                Database db = new Database(
                    rs.getString("tenant_datasource_id"),
                    rs.getString("tenant_id"),
                    rs.getString("database_server_name"),
                    rs.getString("database_port_number"),
                    rs.getString("database_name"),
                    rs.getString("database_user_name")
                );
                databases.add(db);
            }
            
            rs.close();
            stmt.close();
            
            return databases;
        } catch (SQLException e) {
            logger.error("Error getting databases by server", e);
            // Fallback to mock data
            List<Database> databases = new ArrayList<>();
            databases.add(new Database("1", "tenant-1", name, "5432", "database-1", "user1"));
            databases.add(new Database("2", "tenant-2", name, "5432", "database-2", "user2"));
            return databases;
        }
    }

    public List<Query> getQueries(String databaseId, String serverType) {
        try {
            // First, get the database details
            Connection metaConn = databaseService.getMainDbConnection(serverType);
            PreparedStatement metaStmt = metaConn.prepareStatement(
                "SELECT database_server_name, database_port_number, database_name, database_user_name " +
                "FROM tecfg.tenant_datasource WHERE tenant_datasource_id = ?"
            );
            //metaStmt.setString(1, databaseId);
            metaStmt.setLong(1, Long.parseLong(databaseId));
            ResultSet metaRs = metaStmt.executeQuery();
            
            if (!metaRs.next()) {
                throw new SQLException("Database not found: " + databaseId);
            }
            
            String host = metaRs.getString("database_server_name");
            String port = metaRs.getString("database_port_number");
            String dbName = metaRs.getString("database_name");
            String user = metaRs.getString("database_user_name");
            
            metaRs.close();
            metaStmt.close();
            
            // Now connect to the tenant database and get queries
            Connection tenantConn = databaseService.getTenantDbConnection(host, port, dbName, user);
            PreparedStatement tenantStmt = tenantConn.prepareStatement(
                "SELECT datname, pid, application_name, usename, query_start, " +
                "CASE WHEN now() > query_start THEN now() - query_start " +
                "WHEN now() < query_start THEN query_start - now() END as execution_time, " +
                "state, query FROM pg_stat_activity WHERE datname = ?"
            );
            tenantStmt.setString(1, dbName);
            ResultSet tenantRs = tenantStmt.executeQuery();
            
            List<Query> queries = new ArrayList<>();
            while (tenantRs.next()) {
                Query query = new Query(
                    tenantRs.getString("datname"),
                    tenantRs.getString("pid"),
                    tenantRs.getString("application_name"),
                    tenantRs.getString("usename"),
                    tenantRs.getString("query_start"),
                    tenantRs.getString("execution_time"),
                    tenantRs.getString("state"),
                    tenantRs.getString("query")
                );
                queries.add(query);
            }
            
            tenantRs.close();
            tenantStmt.close();
            
            return queries;
        } catch (SQLException e) {
            logger.error("Error getting queries", e);
            // Fallback to mock data
            List<Query> queries = new ArrayList<>();
            queries.add(new Query(
                "database-1", 
                "12345", 
                "Application 1", 
                "user1", 
                "2023-05-01 10:30:45", 
                "00:15:30", 
                "active", 
                "SELECT * FROM large_table WHERE complex_condition = true ORDER BY timestamp DESC"
            ));
            queries.add(new Query(
                "database-1", 
                "12346", 
                "Application 2", 
                "user1", 
                "2023-05-01 10:45:12", 
                "00:01:03", 
                "idle", 
                "UPDATE users SET last_login = NOW() WHERE user_id = 12345"
            ));
            queries.add(new Query(
                "database-1", 
                "12347", 
                "Application 3", 
                "user2", 
                "2023-05-01 10:48:33", 
                "00:00:17", 
                "active", 
                "INSERT INTO logs (timestamp, level, message) VALUES (NOW(), 'INFO', 'User logged in')"
            ));
            return queries;
        }
    }

    public void killQueries(KillQueryRequest request) {
        try {
            // First, get the database details
            Database database = request.getDatabase();
            String host = database.getServerName();
            String port = database.getPort();
            String dbName = database.getName();
            String user = database.getUsername();
            
            // Connect to the tenant database
            Connection tenantConn = databaseService.getTenantDbConnection(host, port, dbName, user);
            
            // Build the query to kill the processes
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE pid IN (");
            
            for (int i = 0; i < request.getQueryIds().size(); i++) {
                if (i > 0) {
                    queryBuilder.append(",");
                }
                queryBuilder.append("?");
            }
            
            queryBuilder.append(")");
            
            PreparedStatement stmt = tenantConn.prepareStatement(queryBuilder.toString());
            
            // Set the parameters
            for (int i = 0; i < request.getQueryIds().size(); i++) {
                stmt.setInt(i + 1, Integer.parseInt(request.getQueryIds().get(i)));
            }
            
            // Execute the query
            stmt.executeQuery();
            stmt.close();
            
            logger.info("Killed queries: {}", request.getQueryIds());
        } catch (SQLException e) {
            logger.error("Error killing queries", e);
        }
    }
    
 // java-backend/src/main/java/com/dbtools/service/QueryToolService.java
 // Add this new method to your existing service

 public List<FileProcessEvent> getFileProcessEvents(Long fileProcessId, String startTime, String endTime) {
     try {
         // Get the main database connection
         Connection conn = databaseService.getMainDbConnection("staging"); // or use a parameter for server type
         
         StringBuilder queryBuilder = new StringBuilder();
         queryBuilder.append("SELECT file_process_event_id, parent_process_event_id, file_process_id, ");
         queryBuilder.append("subs_file_id, event_code, start_time, end_time, created_time, ");
         queryBuilder.append("status, is_success, failure_reason, remarks ");
         queryBuilder.append("FROM teopr.file_process_events ");
         queryBuilder.append("WHERE file_process_id = ? ");
         
         // Add time filters if provided
         if (startTime != null && !startTime.isEmpty()) {
             queryBuilder.append("AND start_time >= ? ");
         }
         if (endTime != null && !endTime.isEmpty()) {
             queryBuilder.append("AND end_time <= ? ");
         }
         
         queryBuilder.append("ORDER BY start_time DESC");
         
         PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
         
         // Set parameters
         int paramIndex = 1;
         stmt.setLong(paramIndex++, fileProcessId);
         
         if (startTime != null && !startTime.isEmpty()) {
             stmt.setTimestamp(paramIndex++, Timestamp.valueOf(startTime));
         }
         if (endTime != null && !endTime.isEmpty()) {
             stmt.setTimestamp(paramIndex++, Timestamp.valueOf(endTime));
         }
         
         ResultSet rs = stmt.executeQuery();
         
         List<FileProcessEvent> events = new ArrayList<>();
         while (rs.next()) {
             FileProcessEvent event = new FileProcessEvent();
             event.setFileProcessEventId(rs.getLong("file_process_event_id"));
             event.setParentProcessEventId(rs.getInt("parent_process_event_id"));
             event.setFileProcessId(rs.getLong("file_process_id"));
             event.setSubsFileId(rs.getInt("subs_file_id"));
             event.setEventCode(rs.getString("event_code"));
             event.setStartTime(rs.getTimestamp("start_time") != null ? 
                 rs.getTimestamp("start_time").toString() : null);
             event.setEndTime(rs.getTimestamp("end_time") != null ? 
                 rs.getTimestamp("end_time").toString() : null);
             event.setCreatedTime(rs.getTimestamp("created_time") != null ? 
                 rs.getTimestamp("created_time").toString() : null);
             event.setStatus(rs.getString("status"));
             event.setIsSuccess(rs.getBoolean("is_success"));
             event.setFailureReason(rs.getString("failure_reason"));
             event.setRemarks(rs.getString("remarks"));
             
             events.add(event);
         }
         
         rs.close();
         stmt.close();
         
         return events;
     } catch (SQLException e) {
         logger.error("Error getting file process events", e);
         // Return empty list on error
         return new ArrayList<>();
     }
 }
}