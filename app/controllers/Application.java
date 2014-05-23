package controllers;

import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import play.db.DB;
import play.db.jpa.Transactional;
import play.libs.EventSource;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import query.JdbcQueryService;
import query.JsonResulSet;


public class Application extends Controller {

    public static Result index() {
        //return ok(views.html.index.render("Spike 1"));
    	return ok(views.html.index.render());
    }

    @Transactional(readOnly=true)
    public static Result syncFoo() {
//    	String f = "/media/d2/downloads/dev/3rdparty/unitth/tests/tests1/TEST-aa1.xml";
//		String f = "/media/d2/skunk/activator/activator-1.1.0_projects/rj8/test/ant-test-results/TEST-testdata.AllTests.xml";
//		ReportParser dbImport = new ReportParser(f);
//		Stream<ReportedTestElement> testCaseEntries = dbImport.parse();
//		
//		BatchJdbcImporter importer = new BatchJdbcImporter();
//		importer.doImport(testCaseEntries, DB.getConnection(), 1000);
		 
//		String existing = TestEntry.all().map(TestEntry::toString).collect(joining("\n"));
    	
    	return ok("");
    }

    public static F.Promise<Result> asyncFoo() {
        return F.Promise.promise(() -> ok("async foo [CHRIS_+++]"));
    }

    public static F.Promise<Result> asyncNonBlockingFoo() {
        return F.Promise.delayed(() -> ok("async non-blocking foo [CHRIS]"), 5, TimeUnit.SECONDS);
    }

    public static F.Promise<Result> reactiveRequest() {
        F.Promise<WS.Response> typesafePromise = WS.url("http://www.typesafe.com").get();
        return typesafePromise.map(response -> ok(response.getBody()));
    }

    public static F.Promise<Result> reactiveComposition() {
        final F.Promise<WS.Response> twitterPromise = WS.url("http://www.twitter.com").get();
        final F.Promise<WS.Response> typesafePromise = WS.url("http://www.typesafe.com").get();

        return twitterPromise.flatMap((twitter) ->
                typesafePromise.map((typesafe) ->
                        ok(twitter.getBody() + typesafe.getBody())));
    }

    public static Result events() {
        EventSource eventSource = new EventSource() {
            public void onConnected() {
                sendData("hello");
            }
        };
        return ok(eventSource);
    }
    
    public static WebSocket<String> echo() {
    	
        return new WebSocket<String>() {
            public void onReady(final In<String> in, final Out<String> out) {
            	in.onMessage(sql -> {
            		
	            	DataSource ds =  DB.getDataSource();
	        		ResultSet rs = new JdbcQueryService(ds).runQuery(sql);
	        		JsonResulSet jsonRs = JsonResulSet.initialiseFrom(rs);
	        		
	        		out.write(jsonRs.getMetadata());
	        		jsonRs.rowsAsStream().forEach(out::write);
            	});
            }
        };
    }
    
    public static WebSocket<String> query() {
    	
        return new WebSocket<String>() {
            public void onReady(final In<String> in, final Out<String> out) {
            	in.onMessage(sql -> {
            		
	            	DataSource ds =  DB.getDataSource();
	        		ResultSet rs = new JdbcQueryService(ds).runQuery(sql);
	        		JsonResulSet jsonRs = JsonResulSet.initialiseFrom(rs);
	        		
	        		out.write(jsonRs.getMetadata());
	        		jsonRs.rowsAsStream().forEach(out::write);
	        		
	        		/*
	        		 * Useful for seeing graph building up from streamed results.
	        		 */
//	        		jsonRs.rowsAsStream().forEach( s -> {
//	        				try { Thread.sleep(1); } catch (Exception e) { }
//	        				out.write(s);
//	        			});
	        		out.close();
            	});
            }
        };
    }
}
