/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.git;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNodeLink;
import com.adamldavis.z.ZNodeLink.LinkType;
import com.adamldavis.z.api.ProgressListener;

/**
 * Assumes git is installed.
 * 
 * @author Adam L. Davis
 * 
 */
public class GitLogDiffsMap implements Serializable {

	private static final long serialVersionUID = 321L;

	static final Logger log = LoggerFactory.getLogger(GitLogDiffsMap.class);

	private final GitLogList gitLogList = new GitLogList();

	private final Map<GitLog, List<GitDiff>> gitDiffs = new HashMap<GitLog, List<GitDiff>>();

	private final File currentDirectory;

	// TODO:find git path from PATH whether windows or other
	private String git = "C:\\Program Files (x86)\\Git\\cmd\\git.cmd";

	// TODO: set this to zero and allow use to set it
	/** limits the number of logs to get. */
	private int limit = 77;

	public GitLogDiffsMap(File currentDirectory) {
		super();
		// TODO Fix the following hack to make sure in base git dir:
		if (currentDirectory.isFile()
				|| !new File(currentDirectory, ".git").isDirectory()) {
			this.currentDirectory = currentDirectory.getParentFile();
		} else
			this.currentDirectory = currentDirectory;
	}

	/** Does a git log and git diff for every log for current directory. */
	public void runDiff(ProgressListener listener) {
		try {
			File temp = File.createTempFile("git_log", ".out");
			Process process = Runtime.getRuntime().exec(
					git + " log" + (limit == 0 ? "" : " -" + limit) + " .",
					null, currentDirectory);
			FileUtils.copyInputStreamToFile(process.getInputStream(), temp);
			log.info("git exited with {}", process.exitValue());
			LineIterator iter = FileUtils.lineIterator(temp);
			gitLogList.addAll(iter);
			iter.close();
			Collections.reverse(gitLogList); // reverse it
			if (listener != null)
				listener.update(10);

			for (int i = 0; i < gitLogList.size(); i++) {
				GitLog gitlog = gitLogList.get(i);
				GitLog gitlog2 = (i == gitLogList.size() - 1) ? null
						: gitLogList.get(i + 1);
				gitDiffs.put(gitlog, getGitDiffs(gitlog, gitlog2));
				if (listener != null)
					listener.update(10 + i * 89 / gitLogList.size());
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private List<GitDiff> getGitDiffs(GitLog gitLog, GitLog gitLog2)
			throws IOException {
		final List<GitDiff> diffs = new LinkedList<GitDiff>();
		File temp = File.createTempFile("git_diff", ".out");
		final String command = git + " diff " + gitLog.getId() + ".."
				+ (gitLog2 == null ? "" : gitLog2.getId());
		Process process = Runtime.getRuntime().exec(command, null,
				currentDirectory);
		FileUtils.copyInputStreamToFile(process.getInputStream(), temp);
		log.info("git exited with {}", process.exitValue());
		LineIterator iter = FileUtils.lineIterator(temp);

		for (String line = ""; iter.hasNext();) {
			line = iter.next();
			if (GitDiff.isMatching(line)) {
				diffs.add(GitDiff.newGitDiff(line, currentDirectory));
			}
		}
		iter.close();
		return diffs;
	}

	public Collection<GitUser> getGitUsers() {
		return gitLogList.getUsers();
	}

	public GitLogList getGitLogList() {
		return gitLogList;
	}

	public Map<GitLog, List<GitDiff>> getGitDiffs() {
		return gitDiffs;
	}

	/** Number of logs (maximum logNumber). */
	public int getLogSize() {
		return gitLogList.size();
	}

	/**
	 * Gets a list of links from Author to nodes changed.
	 * 
	 * @param logNumber
	 *            Starting at 0 being the most recent.
	 * @param nodes
	 *            ZNode list to add nodes to.
	 * @return every node1 is the author, node2 is a node.
	 */
	public Collection<ZNodeLink> getNodeLinks(int logNumber,
			Collection<ZNode> nodes) {
		Collection<ZNodeLink> links = new LinkedList<ZNodeLink>();
		if (logNumber < 0 || logNumber >= getLogSize()) {
			return links;
		}
		GitLog gitLog = gitLogList.get(logNumber);
		// TODO: in the future the ZNode might be separate from the GitUser
		author = gitLog.getAuthor();
		float x = 0, y = 0;
		final List<GitDiff> diffs = new LinkedList<GitDiff>(
				gitDiffs.get(gitLog));

		for (GitDiff diff : diffs) {
			ZNode node = findZNode(diff.getFileA(), this.zNodes);
			if (node == null) {
				log.warn("node {} not found", diff.getFileA().getAbsolutePath());
			} else {
				links.add(new ZNodeLink(author, node, LinkType.HAS_A));
				// TODO: make a smooth animation
				x += (node.getLocation().x - 64) / diffs.size();
				y += node.getLocation().y / diffs.size();
			}
		}
		if (x != 0 && diffs.size() > 0)
			authorLocation.setLocation(x, y);
		log.info("user={}", author);
		log.info("x,y={},{}", author.getLocation().x, author.getLocation().y);

		for (ZNodeLink link : links) {
			nodes.add(link.getNode2());
		}
		return links;
	}

	public final Point2D.Float authorLocation = new Point2D.Float(0, 0);;

	public GitUser author;

	private ZNode findZNode(File file, Collection<ZNode> nodes) {
		String name = file.getName();
		int i = file.getName().indexOf('.');
		if (i > 0) {
			name = name.substring(0, i);
		}
		for (ZNode node : nodes) {
			// ensure name and package are equal
			if (node.getName().equals(name)
					&& file.getParentFile().getAbsolutePath()
							.equals(node.getParentFile().getAbsolutePath())) {
				return node;
			}
		}
		return null;
	}

	public void setGit(String git) {
		this.git = git;
	}

	public static void main(String[] args) {
		log.info(System.getenv("PATH"));
		final GitLogDiffsMap map = new GitLogDiffsMap(new File("."));
		map.runDiff(new ProgressListener() {
			@Override
			public void update(int progress) {
				log.info("progress={}", progress);
			}
		});
		log.info("users={}", map.getGitUsers());
		GitUser user = map.getGitUsers().iterator().next();
		log.info("email={}", user.getEmail());
	}

	/** Removes nodes that are related to diffs. */
	public void removeDiffNodesFrom(List<ZNode> nodes) {
		this.zNodes.addAll(nodes);
		// TODO // remove all nodes associated with diffs:
		final Set<ZNode> toRm = new HashSet<ZNode>();

		for (GitLog gitLog : gitLogList) {
			final List<GitDiff> diffs = new LinkedList<GitDiff>(
					gitDiffs.get(gitLog));

			for (GitDiff diff : diffs) {
				ZNode node = findZNode(diff.getFileA(), nodes);
				if (node != null)
					toRm.add(node);
			}
		}
		nodes.removeAll(toRm);
	}

	private final List<ZNode> zNodes = new ArrayList<ZNode>();

}
