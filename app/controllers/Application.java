package controllers;

import static java.util.stream.Collectors.joining;

import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import jpa.TestEntry;
import play.db.DB;
import play.db.jpa.Transactional;
import play.libs.EventSource;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.WebSocket;
import query.JdbcQueryService;
import query.ResultSetJsonAdapter;

public class Application extends Controller {

    public static Result index() {
        return ok(views.html.index.render("Spike 1"));
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
    	
    	DataSource ds =  DB.getDataSource();
		 
		String existing = TestEntry.all().map(TestEntry::toString).collect(joining("\n"));
    	
    	return ok(existing);
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

//    public static WebSocket<String> echo() {
//    	
//    	final Request req = request();
//    	
//        return new WebSocket<String>() {
//            public void onReady(final In<String> in, final Out<String> out) {
//            	System.out.println(req.path());
//                in.onMessage(out::write);
//            }
//        };
//    }
    
    public static WebSocket<String> echo() {
    	
    	final Request req = request();
    	DataSource ds =  DB.getDataSource();
    	
        return new WebSocket<String>() {
            public void onReady(final In<String> in, final Out<String> out) {
//            	System.out.println(req.path());
//                in.onMessage(out::write);
            	String sql = "SELECT * FROM TESTENTRY";
        		
        		ResultSet rs = new JdbcQueryService().runQuery(ds, sql);
        		ResultSetJsonAdapter result = ResultSetJsonAdapter.initialiseFrom(rs);
        		
        		result.rowsAsStream().forEach(out::write);
            }
        };
    }
    
    /*
     * String sql = "SELECT * FROM TESTENTRY";
		
		ResultSet rs = new JdbcQueryService().runQuery(DS, sql);
		ResultSetJsonAdapter result = ResultSetJsonAdapter.initialiseFrom(rs);
		
		System.out.println(result.getMetadata());
		
		result.rowsAsStream().forEach(System.out::println);
     */

}
