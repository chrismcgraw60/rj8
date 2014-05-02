package importer;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Throwables;

/**
 * Encapsulates a Test Report data source. An ImportSource object is provided as
 * input to a {@link ReportParser}.
 * 
 * This class currently only supports the provision of Junit reports from the
 * file system in the form of a folder structure. It is pretty basic in that it
 * will recursively walk the folder structure adding any files that begin with
 * "TEST-" to the output.
 * 
 * TODO: This can get more sophisticated in the types of input that can be handled (ie
 * web URLs). The output could also probably be made more generic. Currently however YAGNI.
 */
public class ImportSource {

	final String submittedPath;
	final File file;
	
	/**
	 * @param path Absolute filesystem path to the folder where Junit Test Reports are
	 * located.
	 */
	public ImportSource(String path) {
		this.submittedPath = path;
		this.file = new File(this.submittedPath);
	}
	
	/**
	 * @return A {@link Stream} of all absolute file paths for candidate junit reports
	 * that were discovered via walking the folder structure.  
	 */
	public Stream<String> computePaths() {
		List<String> fileList = null;
		try {
			fileList = Files.walk(file.toPath())
					.map(p -> p.toFile())
					.filter(f -> f.getName().startsWith("TEST-"))
					.map(File::getAbsolutePath)
					.collect(Collectors.toList());
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		return fileList.parallelStream();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(computePaths().collect(joining("\n")));		
		return sb.toString();
	}
}
