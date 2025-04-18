/*
 *  Change log:
 *  1.0 - Initial version
 *  1.0.1 - Added latency of querydb:5ms, insertdb:15ms, truncatetable:25ms to fit Jennifer's dashboard
 */

package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@SpringBootApplication
@RestController
public class DemoApplication extends SpringBootServletInitializer{

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	// Endpoint to select all records from testing_db
	@GetMapping("/querydb")
	public ResponseEntity<?> queryDatabase(HttpServletRequest request) {
		try {
			Thread.sleep(5); // Pause 5 milliseconds
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

	// Endpoint to insert JSON array request body into testing_db
	@PutMapping("/insertdb")
	public ResponseEntity<String> insertRecords(@RequestBody List<Map<String, String>> records, HttpServletRequest request) {
		try {
			Thread.sleep(15); // Pause 15 milliseconds
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
			Thread.sleep(25); // Pause 5 milliseconds
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
