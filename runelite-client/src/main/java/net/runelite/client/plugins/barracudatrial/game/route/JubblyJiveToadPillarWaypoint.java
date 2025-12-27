package net.runelite.client.plugins.barracudatrial.route;

import lombok.Getter;

import static net.runelite.client.plugins.barracudatrial.route.RouteWaypoint.WaypointType.TOAD_PILLAR;

@Getter
public class JubblyJiveToadPillarWaypoint extends RouteWaypoint
{
	JubblyJiveToadPillar pillar;

	public JubblyJiveToadPillarWaypoint(JubblyJiveToadPillar pillar)
	{
        super(TOAD_PILLAR, pillar.getLocation());
		this.pillar = pillar;
	}

	public JubblyJiveToadPillarWaypoint(int lap, JubblyJiveToadPillar pillar)
	{
		super(lap, TOAD_PILLAR, pillar.getLocation());
		this.pillar = pillar;
	}
}
