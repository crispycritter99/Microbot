package net.runelite.client.plugins.kotori.hallowedhelper;

import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.LocalPoint;

@RequiredArgsConstructor
public enum FloorParts
{
	//Right to Left movement, Last point is top right, First point bottom left statues
	FLOOR4_1(new LocalPoint(4992, 8704, -1), new LocalPoint(7296, 9344, -1)),
	FLOOR4_2(new LocalPoint(3968,5120, -1), new LocalPoint(5248,5760, -1)),
	FLOOR4_3(new LocalPoint(3328,2944, -1), new LocalPoint(3968,3968, -1)),
	FLOOR5_1(new LocalPoint(4096, 9088, -1), new LocalPoint(5632, 9728, -1)),
	FLOOR5_2(new LocalPoint(3840, 9088, -1), new LocalPoint(5376, 9728, -1)),
	FLOOR5_3(new LocalPoint(7040, 9088, -1), new LocalPoint(8576, 9728, -1)),
	FLOOR5_4(new LocalPoint(7936, 8448, -1), new LocalPoint(8960, 9088, -1)),
	FLOOR5_5(new LocalPoint(3456, 8448, -1), new LocalPoint(5888, 9088, -1));

	private final LocalPoint left_top;
	private final LocalPoint bottom_right;

	public boolean isinarea(LocalPoint pointtocheck)
	{
		return (pointtocheck.getX() >= left_top.getX() && pointtocheck.getX() <= bottom_right.getX() && pointtocheck.getY() >= left_top.getY() && pointtocheck.getY() <= bottom_right.getY());
	}
}