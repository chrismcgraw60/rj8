package importer;

import importer.ReportedTestResultEntry.FailureInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
//	private static final String skipped = "skipped";
	private static final String error = "error";
	private static final String failure = "failure";
	
	private static final String message = "message";
	private static final String type = "type";
	private static final String timestampAttr = "timestamp";
	
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
	
	public static Stream<ReportedTestElement> parse(String fileLocation) {
		Preconditions.checkNotNull(fileLocation, "Argument fileLocation must not be null");
		
		final XMLEventReader eventReader = initialiseEventReader(fileLocation);
		List<ReportedTestElement> testElements = Lists.newArrayList();
		
		try {
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

	private static void setFileAndFolderFromSubmittedFileLocation(String fileLocation, ReportedTestSuiteEntry suiteEntry) {
		
		File reportFile = new File(fileLocation);
		if (!reportFile.isFile()) {
			throw new IllegalArgumentException("File Location refer to a valid File: " + fileLocation + ".");
		}
		
		String fileName = reportFile.getName();
		String folderName = reportFile.getParentFile().getName();
		
		suiteEntry.setContainingFile(fileName);
		suiteEntry.setContainingFolder(folderName);
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
				
				if(failure.equals(elemName) || error.equals(elemName)) {
					
					FailureInfo.Type failType = 
						failure.equals(elemName) ? FailureInfo.Type.failure : FailureInfo.Type.error; 
					
					FailureInfo failInfo = parseFailureInfo(eventReader, startElement, failType);
					testCaseEntry.setFailInfo(failInfo);
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
		}
		return testSuiteEntry;
	}

	/*
	 *  Setup a new EventReader.
	 */
	private static XMLEventReader initialiseEventReader(String fileLocation) {
		
		XMLEventReader eventReader = null;
		try {
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			InputStream in = new FileInputStream(fileLocation);
			eventReader = inputFactory.createXMLEventReader(in);
		}
		catch(Exception ex) {
			Throwables.propagate(ex);
		}
		return eventReader;
	}
	
}
