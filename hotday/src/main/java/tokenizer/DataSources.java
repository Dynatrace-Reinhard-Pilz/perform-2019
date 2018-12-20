package tokenizer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

@Component
public class DataSources {

	public static class DataSource1 extends HikariDataSource {
	}

	public static class DataSource2 extends HikariDataSource {
	}
	
	public static class DataSource3 extends HikariDataSource {
	}
	
	private static final Class<?>[] DATA_SOURCE_CLASSES = new Class<?>[] {
		DataSources.DataSource1.class
		,
		DataSources.DataSource2.class,
		DataSources.DataSource3.class
	};
	
	private final Collection<Connection> connections = new ArrayList<>();
	
	private static final Random RAND = new Random(System.currentTimeMillis());
	
	private final BeanFactory ctx;
	
	public DataSources(BeanFactory ctx) {
		Objects.requireNonNull(ctx);
		this.ctx = ctx;
	}
	
	public void run() throws SQLException {
		if (Stability.get()) {
			HikariDataSource datasource = (HikariDataSource) ctx.getBean(DATA_SOURCE_CLASSES[RAND.nextInt(DATA_SOURCE_CLASSES.length)]);
			try (Connection connection = datasource.getConnection()) {
				ResultSet rs = connection.createStatement().executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
				rs.next();
			}
		} else {
			Connection connection = null;
			for (int i = 0; i < 10; i++) {
				HikariDataSource datasource = (HikariDataSource) ctx.getBean(DATA_SOURCE_CLASSES[RAND.nextInt(DATA_SOURCE_CLASSES.length)]);
				connection = datasource.getConnection();
				synchronized (connections) {
					connections.add(connection);
				}
			}
			if (connection != null) {
				ResultSet rs = connection.createStatement().executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
				rs.next();
			}
		}
	}
	
	public void free() {
		synchronized (connections) {
			for (Connection connection : connections) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}
 	
}
