package com.adamldavis.z.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.api.DependencyManager;

/**
 * Handles loading and saving of pom files.
 * 
 * @author Adam Davis
 * 
 */
public class MavenDependencyManager implements DependencyManager {

	static interface DoInXmlCallback<T> {
		T doInXml(Document doc, long lastModified, final T inParameter)
				throws ParserConfigurationException, SAXException, IOException,
				FileNotFoundException;
	}

	public static final String ARTIFACT_ID = "artifactId";

	private static final String DEPENDENCIES = "dependencies";

	public static final String DEPENDENCY = "dependency";

	public static final String GROUP_ID = "groupId";

	public static final String PROJECT = "project";

	public <T> T doInXmlFile(T list, File file, DoInXmlCallback<T> func) {
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

	String flatten(Node dep) {
		return flatten(dep, new LinkedList<String>()).toString();
	}

	CharSequence flatten(final Node dep, final List<String> nest) {
		final StringBuilder builder = new StringBuilder();

		for (Node child = dep.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child instanceof Element) {
				if (DEPENDENCIES.equals(child.getNodeName())) {
					// skip
				} else if (child.getChildNodes().getLength() == 1
						&& child.getFirstChild() instanceof Text) {
					StringBuilder name = new StringBuilder();

					for (String nestName : nest) {
						name.append(nestName).append('.');
					}
					name.append(child.getNodeName());
					builder.append(name).append('=')
							.append(child.getTextContent()).append('\n');
				} else {
					List<String> list = new LinkedList<String>(nest);
					list.add(child.getNodeName());
					builder.append(flatten(child, list));
				}
			}
		}
		return builder;
	}

	private List<ZNode> getDependencies(Document doc, File file,
			final List<ZNode> list) throws ParserConfigurationException,
			SAXException, IOException, FileNotFoundException {

		NodeList depsList = doc.getDocumentElement().getElementsByTagName(
				DEPENDENCIES);

		if (depsList.getLength() == 0) {
			return list;
		}
		Node deps = depsList.item(0);
		NodeList dependencyNodes = deps.getChildNodes();

		for (int i = 0; i < dependencyNodes.getLength(); i++) {
			Node dep = dependencyNodes.item(i);
			if (DEPENDENCY.equals(dep.getNodeName())) {
				final String artifaceId = getNodeContent(ARTIFACT_ID, dep);

				list.add(new ZNode(ZNodeType.DEPENDENCY, artifaceId,
						flatten(dep), "xml", file));
			}
		}
		return list;
	}

	@Override
	public List<ZNode> getDependencies(final File dependencyFile) {
		List<ZNode> list = new ArrayList<ZNode>(10);

		return doInXmlFile(list, dependencyFile,
				new DoInXmlCallback<List<ZNode>>() {

					@Override
					public List<ZNode> doInXml(Document doc, long lastModified,
							List<ZNode> list)
							throws ParserConfigurationException, SAXException,
							IOException, FileNotFoundException {
						final List<ZNode> dependencies = getDependencies(doc,
								dependencyFile, list);

						for (ZNode node : dependencies) {
							node.parentFile = dependencyFile.getParentFile();
						}
						return dependencies;
					}
				});
	}

	private Node getNode(String name, Node depNode) {
		for (Node child = depNode.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (name.equals(child.getNodeName())) {
				return child;
			}
		}
		return null;
	}

	private String getNodeContent(String name, Node depNode) {
		Node child = getNode(name, depNode);
		return child == null ? null : child.getTextContent();
	}

	@Override
	public String getProjectName(File dependencyFile) {
		return doInXmlFile("z", dependencyFile, new DoInXmlCallback<String>() {

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

	@Override
	public File getSourceFolder(final File dependencyFile) {
		File file = new File(dependencyFile.getParentFile(), "src/main/java/");

		return doInXmlFile(file, dependencyFile, new DoInXmlCallback<File>() {

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
	public String getStandardFileName() {
		return "pom.xml";
	}

	@Override
	public String loadCode(File dependencyFile) {
		return doInXmlFile("", dependencyFile, new DoInXmlCallback<String>() {

			@Override
			public String doInXml(Document doc, long lastModified,
					String inParameter) throws ParserConfigurationException,
					SAXException, IOException, FileNotFoundException {
				return flatten(doc.getDocumentElement());
			}
		});
	}

	@Override
	public void save(final ZNode zNode) {
		final File file = new File(zNode.parentFile, getStandardFileName());

		try {
			final DocumentBuilder docBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			final Document doc;
			FileInputStream fis = null;

			if (file.isFile()) {
				doc = docBuilder.parse(fis = new FileInputStream(file));
			} else {
				doc = docBuilder.newDocument();
				final Element project = doc.createElement(PROJECT);
				project.appendChild(doc.createElement(DEPENDENCIES));
				doc.appendChild(project);
			}
			final NodeList depsList = doc.getDocumentElement()
					.getElementsByTagName(DEPENDENCIES);

			if (depsList.getLength() == 0) {
				return;
			}
			final Node deps = depsList.item(0);

			if (zNode.zNodeType == ZNodeType.DEPENDENCY) {
				saveDependencyNode(zNode, doc, deps);
			} else {
				unflatten(doc, zNode.getCodeLines(), doc.getDocumentElement());
			}
			if (fis != null) {
				fis.close();
			}
			saveXML(file, doc);

		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File not found:" + file);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	protected void saveDependencyNode(final ZNode zNode, final Document doc,
			final Node deps) throws DOMException {
		final NodeList dependencyNodes = deps.getChildNodes();
		Node dependency = null;

		for (int i = 0; i < dependencyNodes.getLength(); i++) {
			final Node item = dependencyNodes.item(i);

			if (DEPENDENCY.equals(item.getNodeName())) {
				if (zNode.name.equals(getNodeContent(ARTIFACT_ID, item))) {
					dependency = item;
					break;
				}
			}
		}
		if (dependency == null) {
			deps.appendChild(unflatten(doc, zNode.getCodeLines()));
		} else {
			deps.replaceChild(unflatten(doc, zNode.getCodeLines()), dependency);
		}
	}

	@SuppressWarnings("deprecation")
	private void saveXML(final File file, Document doc) throws IOException {
		doc.setStrictErrorChecking(false);
		OutputFormat format = new OutputFormat(doc);
		format.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(format);
		final FileOutputStream fos = FileUtils.openOutputStream(file);
		serializer.setOutputByteStream(fos);
		serializer.serialize(doc);
		fos.flush();
		fos.close();
	}

	Element unflatten(Document doc, Collection<String> lines) {
		return unflatten(doc, lines, doc.createElement(DEPENDENCY));
	}

	Element unflatten(Document doc, Collection<String> lines, final Element node) {
		for (String line : lines) {
			unflatten(doc, node, line);
		}
		return node;
	}

	protected void unflatten(Document doc, final Element node, String line)
			throws DOMException {
		if (line.indexOf('=') < 0) {
			return;
		}
		String key = line.substring(0, line.indexOf('='));

		if (key.contains(".")) {
			Node parent = node;

			for (String name : key.split("\\.")) {
				if (getNode(name, parent) == null) {
					parent = parent.appendChild(doc.createElement(name));
				} else
					parent = getNode(name, parent);
			}
			parent.setTextContent(line.substring(line.indexOf('=') + 1));
		} else {
			final Node child = getNode(key, node) == null ? doc
					.createElement(key) : getNode(key, node);

			child.setTextContent(line.substring(line.indexOf('=') + 1));
			node.appendChild(child);
		}
	}

}
