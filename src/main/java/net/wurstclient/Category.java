/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient;

import java.awt.Color;

public enum Category
{
	RENDER("Render", new Color(0x37FF00)),
	COMBAT("Combat", new Color(0xFF1313)),
	MOVEMENT("Movement", new Color(0x0059FF)),
	BLOCKS("Blocks", new Color(0xFF7600)),
	CHAT("Chat", new Color(0xFFF20B)),
	ITEMS("Items", new Color(0x00FF9C)),
	FUN("Fun", new Color(0xAFFF0A)),
	OTHER("Other", new Color(0xFF0DFF));

	private final String name;
	private final Color color;

	private Category(String name, Color color)
	{
		this.name = name;
		this.color = color;
	}

	public String getName()
	{
		return name;
	}
	public Color getColor() { return color; }
}
