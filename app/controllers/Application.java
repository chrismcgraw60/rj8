package controllers;

import java.sql.ResultSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import play.db.DB;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import query.JdbcQueryService;
import query.JsonResulSet;
import folderManager.Folder;
import folderManager.FolderManager;

/**
 * Web Entry point for appplication.
 * All REST endpoints are defined here.
 */
@Singleton
public class Application extends Controller {
	
	@Inject
	private FolderManager folderManager;

    public Result index() {
    	return ok(views.html.index.render());
    }
    
    /**
     * @return A Stream of Folder objects that represent the output of {@link FolderManager#folderEventStream()}.
     */
    public WebSocket<String> folders() {
    	
    	WebSocket<String> ws = new WebSocket<String>() {
            public void onReady(final In<String> in, final Out<String> out) {
            	in.onMessage(msg -> {
            		folderManager.folderEventStream()
            			.map(Folder::toJSON)
            			.subscribe(out::write);
            	});
            }
        };
        
        return ws;
    }
    
    public WebSocket<String> query() {
    	
        return new WebSocket<String>() {
            public void onReady(final In<String> in, final Out<String> out) {
            	in.onMessage(sql -> {
            		
	            	JsonResulSet jsonRs = runQuery(sql);
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
    
    private JsonResulSet runQuery(String sql) {
		DataSource ds =  DB.getDataSource();
		ResultSet rs = new JdbcQueryService(ds).runQuery(sql);
		JsonResulSet jsonRs = JsonResulSet.initialiseFrom(rs);
		return jsonRs;
	}
}
