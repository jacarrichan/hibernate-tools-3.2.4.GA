/*******************************************************************************
  * Copyright (c) 2007-2008 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
package org.hibernate.tool.hbm2x.hbm2hbmxml;

import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Any;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.tool.NonReflectiveTestCase;
import org.hibernate.tool.hbm2x.Exporter;
import org.hibernate.tool.hbm2x.HibernateMappingExporter;

/**
 * @author Dmitry Geraskov
 *
 */
public class MapAndAnyTest extends NonReflectiveTestCase {

	private String mappingFile = "Properties.hbm.xml";

	private Exporter hbmexporter;

	/**
	 * @param name
	 */
	public MapAndAnyTest(String name) {
		super(name, "cfg2hbmoutput");
	}


	protected String[] getMappings() {
		return new String[] {
				mappingFile
		};
	}

	protected void setUp() throws Exception {
		super.setUp();

		hbmexporter = new HibernateMappingExporter(getCfg(), getOutputDir() );
		hbmexporter.start();
	}

	public void testAllFilesExistence() {
		assertFileAndExists(new File(getOutputDir().getAbsolutePath(),  getBaseForMappings() + "ComplexPropertyValue.hbm.xml") );
		assertFileAndExists(new File(getOutputDir().getAbsolutePath(),  getBaseForMappings() + "IntegerPropertyValue.hbm.xml") );
		assertFileAndExists(new File(getOutputDir().getAbsolutePath(),  getBaseForMappings() + "StringPropertyValue.hbm.xml") );
		assertFileAndExists(new File(getOutputDir().getAbsolutePath(),  getBaseForMappings() + "PropertySet.hbm.xml") );
	}

	public void testReadable() {
        Configuration cfg = new Configuration();

        cfg.addFile(new File(getOutputDir(), getBaseForMappings() + "ComplexPropertyValue.hbm.xml"));
        cfg.addFile(new File(getOutputDir(), getBaseForMappings() + "IntegerPropertyValue.hbm.xml"));
        cfg.addFile(new File(getOutputDir(), getBaseForMappings() + "StringPropertyValue.hbm.xml"));
        cfg.addFile(new File(getOutputDir(), getBaseForMappings() + "PropertySet.hbm.xml"));

        cfg.buildMappings();
    }

	public void testAnyNode() throws DocumentException {
		File outputXml = new File(getOutputDir(),  getBaseForMappings() + "PropertySet.hbm.xml");
		assertFileAndExists(outputXml);

		SAXReader xmlReader =  getSAXReader();

		Document document = xmlReader.read(outputXml);

		XPath xpath = DocumentHelper.createXPath("//hibernate-mapping/class/any");
		List list = xpath.selectNodes(document);
		assertEquals("Expected to get one any element", 1, list.size());
		Element node = (Element) list.get(0);
		assertEquals(node.attribute( "name" ).getText(),"someSpecificProperty");
		assertEquals(node.attribute( "id-type" ).getText(),"long");
		assertEquals(node.attribute( "meta-type" ).getText(),"string");
		assertEquals(node.attribute( "cascade" ).getText(), "all");

		list = node.elements("column");
		assertEquals("Expected to get two column elements", 2, list.size());

		list = node.elements("meta-value");
		assertEquals("Expected to get three meta-value elements", 3, list.size());
		node = (Element) list.get(0);
		String className = node.attribute( "class" ).getText();
		assertNotNull("Expected class attribute in meta-value", className);
		if (className.indexOf("IntegerPropertyValue") > 0){
			assertEquals(node.attribute( "value" ).getText(),"I");
		} else if (className.indexOf("StringPropertyValue") > 0){
			assertEquals(node.attribute( "value" ).getText(),"S");
		} else {
			assertTrue(className.indexOf("ComplexPropertyValue") > 0);
			assertEquals(node.attribute( "value" ).getText(),"C");
		}
	}

	public void testMetaValueRead() throws Exception{
		String oldMappingFile = mappingFile;
		mappingFile = "Person2.hbm.xml";

		super.setUp();//rebuld cfg

		Configuration config = getCfg();
		PersistentClass pc = config.getClassMapping("org.hibernate.tool.hbm2x.hbm2hbmxml.Person2");
		assertNotNull(pc);
		Property prop = pc.getProperty("data");
		assertNotNull(prop);
		assertTrue(prop.getValue() instanceof Any);
		Any any = (Any) prop.getValue();
		assertTrue("Expected to get one meta-value element", any.getMetaValues() != null);
		assertEquals("Expected to get one meta-value element", 1, any.getMetaValues().size());
		mappingFile = oldMappingFile;
	}

	public void testMapManyToAny() throws DocumentException {
		File outputXml = new File(getOutputDir(),  getBaseForMappings() + "PropertySet.hbm.xml");
		assertFileAndExists(outputXml);

		SAXReader xmlReader =  getSAXReader();

		Document document = xmlReader.read(outputXml);

		XPath xpath = DocumentHelper.createXPath("//hibernate-mapping/class/map");
		List list = xpath.selectNodes(document);
		assertEquals("Expected to get one any element", 1, list.size());
		Element node = (Element) list.get(0);
		assertEquals(node.attribute( "name" ).getText(),"generalProperties");
		assertEquals(node.attribute( "table" ).getText(),"T_GEN_PROPS");
		assertEquals(node.attribute( "lazy" ).getText(),"true");
		assertEquals(node.attribute( "cascade" ).getText(), "all");


		list = node.elements("key");
		assertEquals("Expected to get one key element", 1, list.size());

		list = node.elements("map-key");
		assertEquals("Expected to get one map-key element", 1, list.size());
		node = (Element) list.get(0);
		assertEquals(node.attribute( "type" ).getText(),"string");
		list = node.elements("column");
		assertEquals("Expected to get one column element", 1, list.size());
		node = node.getParent();//map

		list = node.elements("many-to-any");
		assertEquals("Expected to get one many-to-any element", 1, list.size());
		node = (Element) list.get(0);

		list = node.elements("column");
		assertEquals("Expected to get two column elements", 2, list.size());

		list = node.elements("meta-value");
		assertEquals("Expected to get two meta-value elements", 2, list.size());
		node = (Element) list.get(0);
		String className = node.attribute( "class" ).getText();
		assertNotNull("Expected class attribute in meta-value", className);
		if (className.indexOf("IntegerPropertyValue") > 0){
			assertEquals(node.attribute( "value" ).getText(),"I");
		} else {
			assertTrue(className.indexOf("StringPropertyValue") > 0);
			assertEquals(node.attribute( "value" ).getText(),"S");
		}
	}

	protected String getBaseForMappings() {
		return "org/hibernate/tool/hbm2x/hbm2hbmxml/";
	}

	public static Test suite() {
		return new TestSuite(MapAndAnyTest.class);
	}

}
