package hotday;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.BeanFactory;

import com.zaxxer.hikari.HikariDataSource;

public class ConnectionThread extends Thread {
	
	private static final Class<?>[] DATA_SOURCE_CLASSES = new Class<?>[] {
		DataSources.DataSource1.class,
		DataSources.DataSource2.class,
		DataSources.DataSource3.class
	};
	
	private static final Random RAND = new Random(System.currentTimeMillis());
	private static final AtomicInteger CNT = new AtomicInteger(0);
	
	private final BeanFactory ctx;
	
	public ConnectionThread(BeanFactory ctx) {
		Objects.requireNonNull(ctx);
		this.ctx = ctx;
		setName(ConnectionThread.class.getSimpleName() + "-" + CNT.incrementAndGet());
		setDaemon(true);
	}
	
	@Override
	public void run() {
		HikariDataSource datasource = (HikariDataSource) ctx.getBean(DATA_SOURCE_CLASSES[RAND.nextInt(DATA_SOURCE_CLASSES.length)]);
		try (Connection connection = datasource.getConnection()) {
			ResultSet rs = connection.createStatement().executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
			rs.next();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
		try {
			Thread.sleep(1000 + RAND.nextInt(150));
		} catch (InterruptedException e) {
			return;
		}
	}
 
}