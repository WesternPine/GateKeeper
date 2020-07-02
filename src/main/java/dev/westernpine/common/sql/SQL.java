package dev.westernpine.common.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.Getter;
import lombok.Setter;

/*
 * MySQL Driver
 *
 		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>8.0.16</version>
		</dependency>
 */

public class SQL {
    
    @Getter
    private SQLBuilder connection;
    
    @Getter
    @Setter
    private boolean debugging = false;
    
    public static SQLBuilder getBuilder() {
        return new SQLBuilder();
    }
    
    SQL(SQLBuilder connection) {
        this.connection = connection;
    }
    
    @SuppressWarnings("unchecked")
	public Set<String> getTables() {
    	Optional<Object> returned = query(rs -> {
    		Set<String> tables = new HashSet<>();
			try {
				while(rs.next()) {
					tables.add(rs.getString("tables_in_" + getConnection().getDatabase()));
				}
			} catch (Exception e) {}
			return tables;
		}, "SHOW TABLES;");
    	return returned.isPresent() ? (HashSet<String>) returned.get() : new HashSet<>();
    }
    
	public void getTablesAsync(Consumer<Optional<Object>> returnedResult) {
    	queryAsync(rs -> {
    		Set<String> tables = new HashSet<>();
			try {
				while(rs.next()) {
					tables.add(rs.getString("tables_in_" + getConnection().getDatabase()));
				}
			} catch (Exception e) {}
			return tables;
		}, returnedResult, "SHOW TABLES;");
    }
    
    public Optional<Object> update(Function<Integer, Object> affected, String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        Object toReturn = null;
        try {
            PreparedStatement sql = connection.open().prepareStatement(statement);
            for (int i = 0; i < values.length;) {
                sql.setObject(i + 1, values[i]);
                i++;
            }
            
            int a = sql.executeUpdate();

            if(affected != null)
                toReturn = affected.apply(a);
            
            sql.close();
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
        if(toReturn == null)
            return Optional.empty();
        return Optional.of(toReturn);
    }
    
    public void updateAsync(Function<Integer, Object> affected, Consumer<Optional<Object>> returnedResult, String statement, Object...values) {
        new Thread(() -> { 
            returnedResult.accept(update(affected, statement, values));
        });
    }
    
    public void update(Consumer<Integer> affected, String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        try {
            PreparedStatement sql = connection.open().prepareStatement(statement);
            for (int i = 0; i < values.length;) {
                sql.setObject(i + 1, values[i]);
                i++;
            }
            
            int a = sql.executeUpdate();

            if(affected != null)
                affected.accept(a);
            
            sql.close();
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
    }
    
    public void updateAsync(Consumer<Integer> affected, String statement, Object...values) {
        new Thread(() -> { 
            update(affected, statement, values);
        });
    }
    
    public void update(String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        try {
            PreparedStatement sql = connection.open().prepareStatement(statement);
            for (int i = 0; i < values.length;) {
                sql.setObject(i + 1, values[i]);
                i++;
            }
            
            sql.executeUpdate();
            
            sql.close();
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
    }
    
    public void updateAsync(String statement, Object...values) {
        new Thread(() -> { 
            update(statement, values);
        });
    }
    
    public Optional<Object> query(Function<ResultSet, Object> resultSet, String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        Object toReturn = null;
        try {
            PreparedStatement sql = connection.open().prepareStatement(statement);
            for (int i = 0; i < values.length;) {
                sql.setObject(i + 1, values[i]);
                i++;
            }
            
            ResultSet rs = sql.executeQuery();
            
            if(resultSet != null)
                toReturn = resultSet.apply(rs);
            
            rs.close();
            sql.close();
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
        if(toReturn == null)
            return Optional.empty();
        return Optional.of(toReturn);
    }
    
    public void queryAsync(Function<ResultSet, Object> resultSet, Consumer<Optional<Object>> returnedResult, String statement, Object...values) {
        new Thread(() -> { 
            returnedResult.accept(query(resultSet, statement, values));
        });
    }
    
    public void query(Consumer<ResultSet> resultSet, String statement, Object...values) {
        boolean wasOpen = connection.isOpen();
        try {
            PreparedStatement sql = connection.open().prepareStatement(statement);
            for (int i = 0; i < values.length;) {
                sql.setObject(i + 1, values[i]);
                i++;
            }
            
            ResultSet rs = sql.executeQuery();
            
            if(resultSet != null)
                resultSet.accept(rs);
            
            rs.close();
            sql.close();
        } catch(Exception e) {
            if(debugging)
                e.printStackTrace();
        }
        if(!wasOpen)
            connection.close();
    }
    
    public void queryAsync(Consumer<ResultSet> resultSet, String statement, Object...values) {
        new Thread(() -> { 
            query(resultSet, statement, values);
        });
    }
    
    
    public static class SQLBuilder {
        
        @Getter
        private DatabaseType type;
        @Getter
        private String ip;
        @Getter
        private String port;
        @Getter
        private String database;
        @Getter
        private String username;
        @Getter
        private String password;
        @Getter
        private boolean useSSLSuffix = true;
        @Getter
        private boolean useSSL = false;
        
        @Getter
        private Connection connection;
        
        private SQLBuilder() {
            this.type = DatabaseType.MYSQL;
            this.ip = "localhost";
            this.port = "3306";
            this.database = "database";
            this.username = "root";
            this.password = "admin";
        }
        
        public SQL build() {
            return new SQL(this);
        }
        
        public boolean isOpen() {
            try {
                return (connection != null && !connection.isClosed());
            } catch(Exception e) {}
            return false;
        }
        
        public Connection open() {
            
            try {
                if(isOpen())
                    return connection;
            } catch(Exception e) {}
            
            try {
                connection = DriverManager.getConnection(toString(), username, password);
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            return connection;
        }
        
        public void close() {
            try {
                if(isOpen())
                    connection.close();
            } catch (Exception e) {} finally {
                try {
                    connection.close();
                } catch (Exception e) {}
            }
            connection = null;
        }
        
        @Override
        public String toString() {
            return type.toString() + "://" + ip + ":" + port + "/" + database + (useSSLSuffix ? (useSSL ? "?useSSL=true" : "?useSSL=false") : "");
        }
        
        public String toUnprotectedString() {
            return type.toString() + ":" + username + "/" + password + "@//" + ip + ":" + port + "/" + database + (useSSLSuffix ? (useSSL ? "?useSSL=true" : "?useSSL=false") : "");
        }
        
        public SQLBuilder setDatabaseType(DatabaseType type) {
            this.type = type;
            return this;
        }
        
        public SQLBuilder setIp(String ip) {
            this.ip = ip;
            return this;
        }
        
        public SQLBuilder setPort(String port) {
            this.port = port;
            return this;
        }
        
        public SQLBuilder setDatabase(String database) {
            this.database = database;
            return this;
        }
        
        public SQLBuilder setUsername(String username) {
            this.username = username;
            return this;
        }
        
        public SQLBuilder setPassword(String password) {
            this.password = password;
            return this;
        }
        
        public SQLBuilder setUseSSLSuffix(boolean useSSLSuffix) {
            this.useSSLSuffix = useSSLSuffix;
            return this;
        }
        
        public SQLBuilder setUseSSL(boolean useSSL) {
            this.useSSL = useSSL;
            return this;
        }

    }
    
    public enum DatabaseType {
        
        MYSQL("jdbc:mysql"),
        REDIS("jdbc:redis"),
        POSTGRE("jdbc:postgresql"),
        SQL("jdbc:postgresql"),
        MARIADB("jdbc:mariadb"),
        DB2EXPRESSC("jdbc:db2"),
        SAPHANA("jdbc:sap"),
        INFORMIX("jdbc:informix-sqli"),
        ;
        
        private String connectionPrefix;
        
        DatabaseType(String connectionPrefix) {
            this.connectionPrefix = connectionPrefix;
        }
        
        @Override
        public String toString() {
            return this.connectionPrefix;
        }
        
    }

}