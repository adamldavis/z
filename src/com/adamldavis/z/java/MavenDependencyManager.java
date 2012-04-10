package com.adamldavis.z.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.api.DependencyManager;

public class MavenDependencyManager implements DependencyManager {

	public static final String DEPENDENCY = "dependency";

	public static final String ARTIFACT_ID = "artifactId";

	public static final String GROUP_ID = "groupId";

	@Override
	public String getStandardFileName() {
		return "pom.xml";
	}

	static interface DoInXmlFile<T> {
		T doInXml(Document doc, long lastModified, final T inParameter)
				throws ParserConfigurationException, SAXException, IOException,
				FileNotFoundException;
	}

	@Override
	public List<ZNode> getDependencies(File dependencyFile) {
		List<ZNode> list = new ArrayList<ZNode>(10);

		return doInXmlFile(list, dependencyFile,
				new DoInXmlFile<List<ZNode>>() {

					@Override
					public List<ZNode> doInXml(Document doc, long lastModified,
							List<ZNode> list)
							throws ParserConfigurationException, SAXException,
							IOException, FileNotFoundException {
						return getDependencies(doc, lastModified, list);
					}
				});
	}

	public <T> T doInXmlFile(T list, File file, DoInXmlFile<T> func) {
		long lastModified = file.lastModified();

		try {
			final DocumentBuilder docBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new FileInputStream(file));

			list = func.doInXml(doc, lastModified, list);

		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File not found:" + file);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return list;
	}

	private List<ZNode> getDependencies(Document doc, long lastModified,
			final List<ZNode> list) throws ParserConfigurationException,
			SAXException, IOException, FileNotFoundException {

		NodeList depsList = doc.getDocumentElement().getElementsByTagName(
				"dependencies");

		if (depsList.getLength() == 0) {
			return list;
		}
		Node deps = depsList.item(0);
		NodeList dependencyNodes = deps.getChildNodes();

		for (int i = 0; i < dependencyNodes.getLength(); i++) {
			Node dep = dependencyNodes.item(i);
			if (DEPENDENCY.equals(dep.getNodeName())) {
				final String groupId = getNodeContent(GROUP_ID, dep);
				final String artifaceId = getNodeContent(ARTIFACT_ID, dep);
				final String version = getNodeContent("version", dep);

				list.add(new ZNode(ZNodeType.MODULE, artifaceId, groupId + " "
						+ artifaceId + " " + version, "", lastModified));
			}
		}
		return list;
	}

	private String getNodeContent(String name, Node depNode) {
		for (Node child = depNode.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (name.equals(child.getNodeName())) {
				return child.getTextContent();
			}
		}
		return null;
	}

	@Override
	public File getSourceFolder(final File dependencyFile) {
		File file = new File(dependencyFile.getParentFile(), "src");

		return doInXmlFile(file, dependencyFile, new DoInXmlFile<File>() {

			@Override
			public File doInXml(Document doc, long lastModified, File file)
					throws ParserConfigurationException, SAXException,
					IOException, FileNotFoundException {
				NodeList nodes = doc.getElementsByTagName("sourceDirectory");

				for (int i = 0; i < nodes.getLength();) {
					return new File(dependencyFile.getParentFile(), nodes.item(
							i).getTextContent());
				}
				return file;
			}
		});
	}

	@Override
	public String getProjectName(File dependencyFile) {
		return doInXmlFile("z", dependencyFile, new DoInXmlFile<String>() {

			@Override
			public String doInXml(Document doc, long lastModified, String name)
					throws ParserConfigurationException, SAXException,
					IOException, FileNotFoundException {
				NodeList nodes = doc.getDocumentElement().getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					if ("name".equals(nodes.item(i).getNodeName())) {
						return nodes.item(i).getTextContent();
					}
				}
				return name;
			}
		});
	}

}
