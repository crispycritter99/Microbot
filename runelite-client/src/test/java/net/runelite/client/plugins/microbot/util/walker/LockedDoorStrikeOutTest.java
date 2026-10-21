package net.runelite.client.plugins.microbot.util.walker;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.walker.door.DoorAttemptLedger;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LockedDoorStrikeOutTest
{
	private static final WorldPoint DOOR = new WorldPoint(1633, 4837, 0);
	private static final WorldPoint NEAR_SIDE = new WorldPoint(1633, 4837, 0);
	private static final WorldPoint FAR_SIDE = new WorldPoint(1632, 4837, 0);
	private static final WorldPoint ATTEMPT_START = new WorldPoint(1634, 4839, 0);
	private static final WorldPoint OPEN_STOP = new WorldPoint(1633, 4838, 0);
	private static final long LOOP_PERIOD_MS = 12_000L;

	@Test
	public void approachingTheDoorColumnIsNotATraversal()
	{
		assertFalse("stepping onto the door's own column on the near side has not crossed the edge",
			Rs2WalkerDoors.didTraverseInteractedDoor(ATTEMPT_START, OPEN_STOP, DOOR, NEAR_SIDE, FAR_SIDE));
	}

	@Test
	public void reachingTheNearSideTileIsNotATraversal()
	{
		assertFalse(Rs2WalkerDoors.didTraverseInteractedDoor(
			new WorldPoint(1633, 4839, 0), NEAR_SIDE, DOOR, NEAR_SIDE, FAR_SIDE));
	}

	@Test
	public void crossingTheEdgeIsStillATraversal()
	{
		assertTrue(Rs2WalkerDoors.didTraverseInteractedDoor(NEAR_SIDE, FAR_SIDE, DOOR, NEAR_SIDE, FAR_SIDE));
		assertTrue(Rs2WalkerDoors.didTraverseInteractedDoor(
			new WorldPoint(1633, 4836, 0), FAR_SIDE, DOOR, NEAR_SIDE, FAR_SIDE));
	}

	@Test
	public void diagonalEdgesKeepTheObjectSideReading()
	{
		WorldPoint from = new WorldPoint(1886, 5244, 0);
		WorldPoint to = new WorldPoint(1887, 5243, 0);
		assertTrue(Rs2WalkerDoors.didTraverseInteractedDoor(
			new WorldPoint(1886, 5245, 0), new WorldPoint(1887, 5244, 0), new WorldPoint(1887, 5244, 0), from, to));
	}

	@Test
	public void stoppingOnTheNearSideAfterACrossNudgeIsAConclusiveRefusal()
	{
		assertTrue("the nudge walked the player onto the near-side tile and the game refused the crossing",
			Rs2WalkerDoors.isConclusiveCrossNudgeSample(OPEN_STOP, NEAR_SIDE, NEAR_SIDE, false, false));
		assertTrue(Rs2WalkerDoors.isConclusiveCrossNudgeSample(NEAR_SIDE, NEAR_SIDE, NEAR_SIDE, false, false));
		assertTrue(Rs2WalkerDoors.isConclusiveCrossNudgeSample(OPEN_STOP, OPEN_STOP, NEAR_SIDE, false, false));
	}

	@Test
	public void inFlightOrCancelledOrOffDoorNudgeSamplesProveNothing()
	{
		assertFalse(Rs2WalkerDoors.isConclusiveCrossNudgeSample(OPEN_STOP, NEAR_SIDE, NEAR_SIDE, true, false));
		assertFalse(Rs2WalkerDoors.isConclusiveCrossNudgeSample(OPEN_STOP, NEAR_SIDE, NEAR_SIDE, false, true));
		assertFalse(Rs2WalkerDoors.isConclusiveCrossNudgeSample(
			OPEN_STOP, new WorldPoint(1634, 4838, 0), NEAR_SIDE, false, false));
		assertFalse(Rs2WalkerDoors.isConclusiveCrossNudgeSample(null, NEAR_SIDE, NEAR_SIDE, false, false));
	}

	@Test
	public void lockedDoorLoopStrikesOutWithinTheStrikeLimit()
	{
		DoorAttemptLedger ledger = new DoorAttemptLedger();
		long now = 1_000_000L;
		int strikeOutCycle = -1;
		for (int cycle = 1; cycle <= 20 && strikeOutCycle < 0; cycle++, now += LOOP_PERIOD_MS)
		{
			boolean traversed = Rs2WalkerDoors.didTraverseInteractedDoor(
				ATTEMPT_START, OPEN_STOP, DOOR, NEAR_SIDE, FAR_SIDE);
			DoorAttemptLedger.Strike strike;
			if (traversed)
			{
				ledger.clearCrossFailures(NEAR_SIDE, FAR_SIDE);
			}
			else
			{
				strike = ledger.registerCrossFailure(NEAR_SIDE, FAR_SIDE, false, now,
					Rs2Walker.DOOR_CROSS_FAILURE_DECAY_MS, Rs2Walker.DOOR_CROSS_FAILURE_STRIKE_LIMIT);
				if (strike == DoorAttemptLedger.Strike.STRIKE_OUT)
				{
					strikeOutCycle = cycle;
					break;
				}
			}
			strike = ledger.registerCrossFailure(NEAR_SIDE, FAR_SIDE,
				Rs2WalkerDoors.isConclusiveCrossNudgeSample(OPEN_STOP, NEAR_SIDE, NEAR_SIDE, false, false),
				now + 3_000L, Rs2Walker.DOOR_CROSS_FAILURE_DECAY_MS, Rs2Walker.DOOR_CROSS_FAILURE_STRIKE_LIMIT);
			if (strike == DoorAttemptLedger.Strike.STRIKE_OUT)
			{
				strikeOutCycle = cycle;
			}
		}
		assertTrue("a locked door that never opens must strike out instead of looping; cycle=" + strikeOutCycle,
			strikeOutCycle > 0 && strikeOutCycle <= Rs2Walker.DOOR_CROSS_FAILURE_STRIKE_LIMIT);
	}

	@Test
	public void nestedRecoveryWalkKeepsTheOuterWalksStrikeOutBlocks()
	{
		DoorAttemptLedger ledger = Rs2WalkerDoors.doorAttemptLedgerForTesting();
		ledger.drainWalkScopedBlocks();
		ledger.recordWalkScopedBlock(NEAR_SIDE, FAR_SIDE);
		ledger.recordWalkScopedBlock(FAR_SIDE, NEAR_SIDE);

		Rs2Walker.withdrawPreviousWalkScopedBlocks(2);

		List<WorldPoint[]> kept = ledger.drainWalkScopedBlocks();
		assertEquals("a can't-reach recovery walk nested in the outer walk must not lift its blocks",
			2, kept.size());

		ledger.recordWalkScopedBlock(NEAR_SIDE, FAR_SIDE);
		Rs2Walker.withdrawPreviousWalkScopedBlocks(1);
		assertTrue("a new outermost walk still withdraws the previous walk's blocks",
			ledger.drainWalkScopedBlocks().isEmpty());
	}
}
