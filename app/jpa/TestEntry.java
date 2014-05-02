package jpa;

import java.util.List;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.data.validation.Constraints;
import play.db.jpa.JPA;

@Entity(name = "TestEntry")
/*
 * TODO: Commented out below. See http://stackoverflow.com/questions/20734540/nosuchmethoderror-in-javax-persistence-table-indexesljavax-persistence-index
 */
public class TestEntry {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE) 
	public Long id;
	
	@Constraints.Required
	public String uuid;
	
	@Constraints.Required
	public String className;
	
	@Constraints.Required
	public String methodName;
	
	@Constraints.Required
	public String time;
	
	@Override
	public String toString() {
		return this.id + " | " + this.uuid + " | " + this.className + " | " + this.methodName + " | " + this.time;
	}
	
	public static Stream<TestEntry> all() {
		
		@SuppressWarnings("unchecked")
		List<TestEntry> testEntries = JPA.em().createQuery("from TestEntry order by id").getResultList();
		
		return testEntries.stream();
	}
	
}
