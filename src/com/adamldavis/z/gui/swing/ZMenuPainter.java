/** Copyright 2012, Adam L. Davis. */
package com.adamldavis.z.gui.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.adamldavis.z.gui.Painter;
import com.adamldavis.z.gui.ZMenu;

/**
 * Paints the menu.
 * 
 * @author Adam L. Davis
 */
public class ZMenuPainter extends Graphics2DPainter implements Painter {

	Font font = new Font(Font.SANS_SERIF, Font.BOLD, ZMenu.FONT_SIZE);

	public ZMenuPainter(Graphics2D graphics2d) {
		super(graphics2d);
	}

	@Override
	public void paint(Object object) {
		if (object instanceof ZMenu) {
			ZMenu zMenu = (ZMenu) object;

			if (zMenu.isVisible()) {
				graphics2d.setColor(Color.BLACK);
				graphics2d.fillRect(zMenu.getLocation().x,
						zMenu.getLocation().y, zMenu.getWidth(),
						zMenu.getHeight());
				graphics2d.setColor(Color.LIGHT_GRAY);
				graphics2d.drawRect(zMenu.getLocation().x,
						zMenu.getLocation().y, zMenu.getWidth(),
						zMenu.getHeight());
				graphics2d.setFont(font);
				ZMenu.Menu hoveredMenu = zMenu.getHoveredMenu();

				for (ZMenu.Menu menu : zMenu.getBar().getMenus()) {
					if (hoveredMenu != null) {
						graphics2d.setColor(Color.WHITE);
					} else
						graphics2d.setColor(Color.LIGHT_GRAY);

					graphics2d.drawString(menu.getName(), menu.getLocation().x,
							menu.getLocation().y);
				}
				if (hoveredMenu != null) {
					graphics2d.setColor(Color.BLACK);
					int x = hoveredMenu.getLocation().x + zMenu.getWidth();
					int y = hoveredMenu.getLocation().y - ZMenu.FONT_SIZE;
					graphics2d.fillRect(x, y, zMenu.getWidth(), ZMenu.FONT_SIZE
							* hoveredMenu.getItems().size());
					graphics2d.setColor(Color.WHITE);
					graphics2d.drawRect(x, y, zMenu.getWidth(), ZMenu.FONT_SIZE
							* hoveredMenu.getItems().size());
					for (ZMenu.MenuItem item : hoveredMenu.getItems()) {
						graphics2d.drawString(item.getName(),
								item.getLocation().x, item.getLocation().y);
					}
				}
			}
		}
	}
}