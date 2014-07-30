package controllers;

import java.sql.ResultSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import play.db.DB;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import query.JdbcQueryService;
import query.JsonResulSet;
import folderManager.Folder;
import folderManager.FolderManager;

import static java.util.stream.Collectors.toList;

@Singleton
public class Application extends Controller {
	
	@Inject
	private FolderManager folderManager;

    public Result index() {
    	return ok(views.html.index.render());
    }
    
    public F.Promise<Result> folders() {
    	List<Folder> folders = folderManager.folders().collect(toList());
        return F.Promise.promise(() -> ok(folders.toString()));
    }
    
    public WebSocket<String> query() {
    	
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
