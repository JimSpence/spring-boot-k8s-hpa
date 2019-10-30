package com.learnk8s.app.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static org.apache.camel.LoggingLevel.INFO;

@Component
public class MyRouter extends RouteBuilder {

//	@Autowired
//	private Environment env;

	@Override
	public void configure() throws Exception {

		// this can also be configured in application.properties
//		restConfiguration()
//				.component("servlet")
//				.bindingMode(RestBindingMode.auto)
//				.dataFormatProperty("prettyPrint", "true")
//				.enableCORS(true)
//				.port("8081")
//				.contextPath(contextPath.substring(0, contextPath.length() - 2))
//				// turn on swagger api-doc
//				.apiContextPath("/api-doc")
//				.apiProperty("api.title", "User API")
//				.apiProperty("api.version", "1.0.0");

//		from("timer://timer1?period=1000")
//		.setBody(simple("select * from Brand"))
//		.to("jdbc:dataSource")
//		.split().simple("${body}")
//		.log("process row ${body}")
//		.process(new Processor(){
//
//			public void process(Exchange xchg) throws Exception {
//
//				Map<String, Object> row = xchg.getIn().getBody(Map.class);
//				System.out.println("Processing....."+row);
//				Employee emp = new Employee();
//
//				emp.setId(row.get("ID").toString());
//				emp.setName(row.get("NAME").toString());
//				emp.setDob(row.get("DOB").toString());
//				emp.setSalary((Integer)row.get("SALARY"));
//
//				System.out.println("Employee: "+ emp);
//			}
//
//		})
//		.to("mock:result");

		from("timer://simpleTimer?period=5000")
				.log(INFO, "*** TIMER FIRED ***")
				.process("consumerProcessor")
		;

	}

}
