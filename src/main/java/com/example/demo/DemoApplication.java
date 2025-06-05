/*
 *  Change log:
 *  1.0 - Initial version
 *  1.0.1 - Added latency of querydb:5ms, insertdb:15ms, truncatetable:25ms to fit Jennifer's dashboard
 *  1.0.2 - Modified latency to be querydb:2500ms(delay1), insertdb:7500ms(delay2), truncatetable:12500ms(delay3)
 * 			Latency is configurable in application.properties as delay1,delay2,delay3
 *  1.0.3 - Added querytable to read table name from header. This is used to test Dynamic Instrumentation
 *  1.0.4 - Modified latency settings
 */

package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@SpringBootApplication
@RestController
public class DemoApplication extends SpringBootServletInitializer{

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${healthdelay}")
	private int healthDelay;

	@Value("${querydbdelay}")
	private int querydbDelay;

	@Value("${querytabledelay}")
	private int querytableDelay;

	@Value("${insertdbdelay}")
	private int insertdbDelay;

	@Value("${truncatetabledelay}")
	private int truncatetableDelay;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		logger.info("App Version: 1.0.4");
	}

	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
		try {
			logger.info("/health called with a delay of " + healthDelay + "ms");
			Thread.sleep(healthDelay); // Use delay1 from properties
			logger.info("Health check endpoint called");
			Map<String, String> response = new HashMap<>();
			response.put("status", "ok");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("e");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
    }

	// Endpoint to select all records from testing_db
	@GetMapping("/querydb")
	public ResponseEntity<?> queryDatabase(HttpServletRequest request) {
		try {
			logger.info("/querydb called with a delay of " + querydbDelay + "ms");
			Thread.sleep(querydbDelay); // Use delay1 from properties
			String sql = "SELECT * FROM sample_table";
			List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
			logger.info("Database queried successfully");
			return ResponseEntity.ok(results);
		} catch (Exception e) {
			logger.error("Error querying database: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Failed to query database: " + e.getMessage());
		} 
	}

	// Endpoint for testing dynamic instrumentation by retrieving 'tableName' as spantag
	@GetMapping("/querytable")
	public ResponseEntity<?> queryTable(HttpServletRequest request) {
		String tableName = request.getHeader("querytable");
		if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
			logger.error("Invalid or missing table name in header");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or missing table name in header");
		}
		try {
			logger.info("/querytable called with a delay of " + querytableDelay + "ms");
			Thread.sleep(querytableDelay); // Use delay1 from properties
			logger.info("/querytable called for table: " + tableName);
			String sql = "SELECT * FROM " + tableName;
			List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
			logger.info("Table '" + tableName + "' queried successfully");
			return ResponseEntity.ok(results);
		} catch (Exception e) {
			logger.error("Error querying table '" + tableName + "': " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Failed to query table '" + tableName + "': " + e.getMessage());
		}
	}

	// Endpoint to insert JSON array request body into testing_db
	@PutMapping("/insertdb")
	public ResponseEntity<String> insertRecords(@RequestBody List<Map<String, String>> records, HttpServletRequest request) {
		try {
			logger.info("/insertdb called with a delay of " + insertdbDelay + "ms");
			Thread.sleep(insertdbDelay); // Use delay2 from properties
			String sql = "INSERT INTO sample_table (name, value) VALUES (?, ?)";
			for (Map<String, String> record : records) {
				jdbcTemplate.update(sql, record.get("name"), record.get("value"));
			}
			logger.info(records.size() + " record(s) inserted successfully");
			return new ResponseEntity<>(records.size() + " record(s) inserted successfully", HttpStatus.CREATED);
		} catch (Exception e) {
			logger.error("Error inserting records: " + e.getMessage());
			return new ResponseEntity<>("Failed to insert records: " + e.getMessage(), 
				HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/truncatetable")
	public ResponseEntity<String> truncateTable(HttpServletRequest request) {
		try {
			logger.info("/truncatedb called with a delay of " + truncatetableDelay + "ms");
			Thread.sleep(truncatetableDelay); // Use delay3 from properties
			String sql = "TRUNCATE TABLE sample_table";
			jdbcTemplate.execute(sql);
			logger.info("Table truncated successfully");
			return new ResponseEntity<>("Table truncated successfully", HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error truncating table: " + e.getMessage());
			return new ResponseEntity<>("Failed to truncate table: " + e.getMessage(), 
				HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DemoApplication.class);
	}

}
