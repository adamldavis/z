/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.gui.swing;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.git.GitUser;
import com.adamldavis.z.git.GravatarUtil;
import com.adamldavis.z.gui.Painter;

/**
 * @author Adam L. Davis
 * 
 */
public class UserPainter extends Graphics2DPainter implements Painter {

	private static final Logger log = LoggerFactory
			.getLogger(UserPainter.class);

	/* TODO: find a better way to do this. */
	private static final Map<GitUser, Image> images = new HashMap<GitUser, Image>();

	public UserPainter(Graphics2D graphics2d) {
		super(graphics2d);
	}

	// for now, gets a GitUser and paints its image.
	@Override
	public void paint(Object object) {
		if (object instanceof GitUser) {
			GitUser user = (GitUser) object;
			Image image = images.get(user);
			int size = 32; // size of image
			if (image == null) {
				try {
					image = Toolkit.getDefaultToolkit().createImage(
							GravatarUtil.getGravatar(user.getEmail(), size));
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			images.put(user, image);
			final int x = (int) user.getLocation().x - size / 2;
			final int y = (int) user.getLocation().y - size / 2;
			if (image != null)
				graphics2d.drawImage(image, x, y, x + size, y + size, 0, 0,
						size, size, null);
			graphics2d.setFont(graphics2d.getFont().deriveFont(10f));
			graphics2d.drawString(user.getName(), x, y - 2);
		}
	}

}
