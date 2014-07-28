package importer;

import importer.ReportedTestResultEntry.FailureInfo;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
Junit report schema here ...

<?xml version="1.0" encoding="UTF-8" ?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xs:element name="failure">
        <xs:complexType mixed="true">
            <xs:attribute name="type" type="xs:string" use="optional"/>
            <xs:attribute name="message" type="xs:string" use="optional"/>
        </xs:complexType>
    </xs:element>

    <xs:element name="error">
        <xs:complexType mixed="true">
            <xs:attribute name="type" type="xs:string" use="optional"/>
            <xs:attribute name="message" type="xs:string" use="optional"/>
        </xs:complexType>
    </xs:element>

    <xs:element name="properties">
        <xs:complexType>
            <xs:sequence>
                <xs:element ref="property" maxOccurs="unbounded"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:element name="property">
        <xs:complexType>
            <xs:attribute name="name" type="xs:string" use="required"/>
            <xs:attribute name="value" type="xs:string" use="required"/>
        </xs:complexType>
    </xs:element>

    <xs:element name="skipped" type="xs:string"/>
    <xs:element name="system-err" type="xs:string"/>
    <xs:element name="system-out" type="xs:string"/>

    <xs:element name="testcase">
        <xs:complexType>
            <xs:sequence>
                <xs:element ref="skipped" minOccurs="0" maxOccurs="1"/>
                <xs:element ref="error" minOccurs="0" maxOccurs="unbounded"/>
                <xs:element ref="failure" minOccurs="0" maxOccurs="unbounded"/>
                <xs:element ref="system-out" minOccurs="0" maxOccurs="unbounded"/>
                <xs:element ref="system-err" minOccurs="0" maxOccurs="unbounded"/>
            </xs:sequence>
            <xs:attribute name="name" type="xs:string" use="required"/>
            <xs:attribute name="assertions" type="xs:string" use="optional"/>
            <xs:attribute name="time" type="xs:string" use="optional"/>
            <xs:attribute name="classname" type="xs:string" use="optional"/>
            <xs:attribute name="status" type="xs:string" use="optional"/>
        </xs:complexType>
    </xs:element>

    <xs:element name="testsuite">
        <xs:complexType>
            <xs:sequence>
                <xs:element ref="properties" minOccurs="0" maxOccurs="1"/>
                <xs:element ref="testcase" minOccurs="0" maxOccurs="unbounded"/>
                <xs:element ref="system-out" minOccurs="0" maxOccurs="1"/>
                <xs:element ref="system-err" minOccurs="0" maxOccurs="1"/>
            </xs:sequence>
            <xs:attribute name="name" type="xs:string" use="required"/>
            <xs:attribute name="tests" type="xs:string" use="required"/>
            <xs:attribute name="failures" type="xs:string" use="optional"/>
            <xs:attribute name="errors" type="xs:string" use="optional"/>
            <xs:attribute name="time" type="xs:string" use="optional"/>
            <xs:attribute name="disabled" type="xs:string" use="optional"/>
            <xs:attribute name="skipped" type="xs:string" use="optional"/>
            <xs:attribute name="timestamp" type="xs:string" use="optional"/>
            <xs:attribute name="hostname" type="xs:string" use="optional"/>
            <xs:attribute name="id" type="xs:string" use="optional"/>
            <xs:attribute name="package" type="xs:string" use="optional"/>
        </xs:complexType>
    </xs:element>

    <xs:element name="testsuites">
        <xs:complexType>
            <xs:sequence>
                <xs:element ref="testsuite" minOccurs="0" maxOccurs="unbounded"/>
            </xs:sequence>
            <xs:attribute name="name" type="xs:string" use="optional"/>
            <xs:attribute name="time" type="xs:string" use="optional"/>
            <xs:attribute name="tests" type="xs:string" use="optional"/>
            <xs:attribute name="failures" type="xs:string" use="optional"/>
            <xs:attribute name="disabled" type="xs:string" use="optional"/>
            <xs:attribute name="errors" type="xs:string" use="optional"/>
        </xs:complexType>
    </xs:element>
</xs:schema>
 */

/**
 * Transforms a Junit XML Report to Java objects.
 */
public class ReportParser {
	private static final String testsuiteEl = "testsuite";
	private static final String testsAttr = "tests";
	private static final String testcaseEl = "testcase";
	private static final String classnameAttr = "classname";
	private static final String nameAttr = "name";
	private static final String timeAttr = "time";
	private static final String errorsAttr = "errors";
	private static final String skippedAttr = "skipped";
	private static final String failuresAttr = "failures";
	private static final String errorEl = "error";
	private static final String failureEl = "failure";
	private static final String skippedEl = "skipped";
	private static final String message = "message";
	private static final String type = "type";
	private static final String timestampAttr = "timestamp";
	
	private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	
	public static class ImportResult {
		
		public ImportResult(Integer importedEntryCount, Long timeTakenSeconds, Stream<ReportedTestElement> importedElements) {
			this.importedEntryCount = importedEntryCount;
			this.timeTakenSeconds = timeTakenSeconds;
			this.importedElements = importedElements;
		}
		public Integer importedEntryCount;
		public Long timeTakenSeconds;
		public Stream<ReportedTestElement> importedElements;
	}
	
	public Stream<ReportedTestElement> parse(Path fileLocation) {
		Preconditions.checkNotNull(fileLocation, "Argument fileLocation must not be null");
		
		final List<ReportedTestElement> testElements = Lists.newArrayList();
		
		try (BufferedReader bs = Files.newBufferedReader(fileLocation, StandardCharsets.UTF_8);
				AutoCloseEventReader eventReader = newEventReader(bs)) {

			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					String elemName = startElement.getName().getLocalPart();
					
					if(testsuiteEl.equals(elemName)) {
						ReportedTestSuiteEntry suiteEntry = parseTestSuiteEntry(startElement);
						setFileAndFolderFromSubmittedFileLocation(fileLocation, suiteEntry);
						suiteEntry.validateState();
						testElements.add(suiteEntry);
					}
					
					if(testcaseEl.equals(elemName)) {
						ReportedTestResultEntry testCaseEntry = parseTestCaseEntry(startElement, eventReader);
						testElements.add(testCaseEntry);
					}
				}
			}
		}
		catch(Exception ex) {
			Throwables.propagate(ex);
		}
		return testElements.stream();
	}

	private static void setFileAndFolderFromSubmittedFileLocation(Path fileLocation, ReportedTestSuiteEntry suiteEntry) {
		
		File reportFile = fileLocation.toFile();
		if (!reportFile.isFile()) {
			throw new IllegalArgumentException("File Location refer to a valid File: " + fileLocation + ".");
		}
		
		String fileName = reportFile.getName();
		
		suiteEntry.setContainingFile(fileName);
		suiteEntry.setContainingFolder(reportFile.getParentFile().toPath());
	}

	private static ReportedTestResultEntry parseTestCaseEntry(final StartElement testCaseElement, final XMLEventReader eventReader) 
			throws XMLStreamException {
		
		final ReportedTestResultEntry testCaseEntry = new ReportedTestResultEntry();
		//TODO: make this optional. PK is sequnec from DB.
		testCaseEntry.setStorageId(UUID.randomUUID());
		
		@SuppressWarnings("unchecked")
		Iterator<Attribute> attributes = testCaseElement.getAttributes();
		while(attributes.hasNext()) {
			Attribute attribute = attributes.next();
			if (attribute.getName().toString().equals(classnameAttr)) {
		        testCaseEntry.setQualifiedName(attribute.getValue());
		    }
			if (attribute.getName().toString().equals(nameAttr)) {
		        testCaseEntry.setMethodName(attribute.getValue());
		    }
			if (attribute.getName().toString().equals(timeAttr)) {
		        testCaseEntry.setTime(attribute.getValue());
		    }
		}
		
		boolean isTestCaseParsed = false;
		while (eventReader.hasNext() && !isTestCaseParsed) {
			XMLEvent testCaseElementEvent = eventReader.nextEvent();
			if (testCaseElementEvent.isStartElement()) {
				StartElement startElement = testCaseElementEvent.asStartElement();
				String elemName = startElement.getName().getLocalPart();
				
				if(failureEl.equals(elemName) || errorEl.equals(elemName)) {
					
					FailureInfo.Type failType = 
						failureEl.equals(elemName) ? FailureInfo.Type.failure : FailureInfo.Type.error; 
					
					FailureInfo failInfo = parseFailureInfo(eventReader, startElement, failType);
					testCaseEntry.setFailInfo(failInfo);
				}
				
				if (skippedEl.equals(elemName)) {
					testCaseEntry.setSkipped(true);
				}
			}
			
			if (testCaseElementEvent.isEndElement()) {
				EndElement endElement = testCaseElementEvent.asEndElement();
				String tcEndElemName = endElement.getName().getLocalPart();
				if(testcaseEl.equals(tcEndElemName)) {
					isTestCaseParsed = true;
				}
			}
		}
		
		testCaseEntry.validateState();
		return testCaseEntry;
	}

	private static FailureInfo parseFailureInfo(final XMLEventReader eventReader, StartElement startElement, FailureInfo.Type failType)
			throws XMLStreamException {
		
		String messageAttr = null;
		String typeAttr = null;
		String detailsString = null;
		
		@SuppressWarnings("unchecked")
		Iterator<Attribute> failAttributes = startElement.getAttributes();
		while(failAttributes.hasNext()) {
			Attribute attribute = failAttributes.next();
			if (attribute.getName().toString().equals(message)) {
				messageAttr = attribute.getValue();
		    }
			if (attribute.getName().toString().equals(type)) {
				typeAttr = attribute.getValue();
		    }
		}
		
		StringBuilder detailsBuilder = new StringBuilder();
		boolean isReadingChars = true;
		while (eventReader.hasNext() && isReadingChars) {
			XMLEvent nextEvent = eventReader.nextEvent();
			if (nextEvent.isCharacters()) {
				detailsBuilder.append(nextEvent.asCharacters().getData());
			}
			else {
				isReadingChars = false;
			}
		}
		detailsString = detailsBuilder.toString();		
		
		FailureInfo failInfo = new FailureInfo(messageAttr, typeAttr, detailsString, failType);
		return failInfo;
	}

	private static ReportedTestSuiteEntry parseTestSuiteEntry(StartElement startElement) {
		ReportedTestSuiteEntry testSuiteEntry = new ReportedTestSuiteEntry();
		testSuiteEntry.setStorageId(UUID.randomUUID());
		
		@SuppressWarnings("unchecked")
		Iterator<Attribute> attributes = startElement.getAttributes();
		
		while(attributes.hasNext()) {
			Attribute attribute = attributes.next();
			if (attribute.getName().toString().equals(nameAttr)) {
				testSuiteEntry.setQualifiedName(attribute.getValue());
		    }
			if (attribute.getName().toString().equals(timeAttr)) {
				testSuiteEntry.setTime(attribute.getValue());
		    }
			if (attribute.getName().toString().equals(testsAttr)) {
				testSuiteEntry.setTestsRun(Long.parseLong(attribute.getValue()));
		    }
			if (attribute.getName().toString().equals(timestampAttr)) {
				testSuiteEntry.setTimestamp(DateTime.parse(attribute.getValue()));
		    }
			if (attribute.getName().toString().equals(errorsAttr)) {
				testSuiteEntry.setTotalErrors(Long.parseLong(attribute.getValue()));
		    }
			if (attribute.getName().toString().equals(failuresAttr)) {
				testSuiteEntry.setTotalFailures(Long.parseLong(attribute.getValue()));
		    }
			if (attribute.getName().toString().equals(skippedAttr)) {
				testSuiteEntry.setTotalSkipped(Long.parseLong(attribute.getValue()));
		    }
		}
		return testSuiteEntry;
	}
	
	private static AutoCloseableEventReader newEventReader(BufferedReader br){
		XMLEventReader er = null;
		try {
			er = inputFactory.createXMLEventReader(br);
			return new AutoCloseableEventReader(er);
		} catch (XMLStreamException e) {
			Throwables.propagate(e);
		}
		return null;
	}
	
	/**
	 * Convenience interface to bundle up {@link XMLReader} and {@link AutoCloseable} interfaces. 
	 * This gives us a cleaner flow in the main parsing logic.
	 */
	private static interface AutoCloseEventReader extends XMLEventReader, AutoCloseable {}
	
	/**
	 * Implements {@link AutoCloseEventReader} by wrapping an {@link XMLEventReader}.
	 * The {@link AutoCloseable#close()} will be dispatched to {@link XMLEventReader#close()}. 
	 */
	private static class AutoCloseableEventReader implements AutoCloseEventReader {
		
		final XMLEventReader wrapped;
		
		AutoCloseableEventReader(XMLEventReader wrapped) { this.wrapped = wrapped; }

		@Override public Object next() { return wrapped.next(); }

		@Override public void close() throws XMLStreamException { wrapped.close(); }

		@Override public String getElementText() throws XMLStreamException { return wrapped.getElementText(); }

		@Override public Object getProperty(String arg0) throws IllegalArgumentException {return wrapped.getProperty(arg0); }

		@Override public boolean hasNext() {return wrapped.hasNext(); }

		@Override public XMLEvent nextEvent() throws XMLStreamException { return wrapped.nextEvent(); }

		@Override public XMLEvent nextTag() throws XMLStreamException { return wrapped.nextTag(); }

		@Override public XMLEvent peek() throws XMLStreamException { return wrapped.peek(); }
		
	}
	
}
