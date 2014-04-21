package jpa;

import java.util.List;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints;
import play.db.jpa.JPA;

@Entity
//@Table( name = "testEntry" )
public class TestEntry {
	
	@Id
	public Long id;
	
	@Constraints.Required
	public String title;
	
	@Override
	public String toString() {
		return this.id + ": " + this.title;
	}
	
	public static Stream<TestEntry> all() {
		
		@SuppressWarnings("unchecked")
		List<TestEntry> testEntries = JPA.em().createQuery("from TestEntry order by id").getResultList();
		
		return testEntries.stream();
	}
	
}
