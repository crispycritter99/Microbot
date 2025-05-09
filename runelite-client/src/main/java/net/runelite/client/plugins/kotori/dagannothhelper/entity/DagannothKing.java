/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2022, Kotori <https://github.com/OreoCupcakes/>
 * Copyright (c) 2020, dutta64 <https://github.com/dutta64>
 * Copyright (c) 2019, Ganom <https://github.com/Ganom>
 * Copyright (c) 2019, Lucas <https://github.com/lucwousin>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.kotori.dagannothhelper.entity;

import net.runelite.client.plugins.kotori.dagannothhelper.DagannothHelperPlugin;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Prayer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Comparator;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DagannothKing implements Comparable<DagannothKing>
{
	@EqualsAndHashCode.Include
	private final NPC npc;

	@Getter
	private final int npcId;

	@Getter
	private int ticksUntilNextAnimation;

	private final int animationId;

	private final int animationTickSpeed;

	@Getter
	private final AttackStyle attackStyle;

	private final int priority;

	@Getter
	private final Color color;

	public DagannothKing(final NPC npc, boolean echo)
	{
		this.npc = npc;
		this.npcId = npc.getId();
		this.ticksUntilNextAnimation = 0;

		final Boss boss = Boss.of(npcId, echo);
		this.animationId = boss.animationId;
		this.animationTickSpeed = boss.attackSpeed;
		this.attackStyle = boss.attackStyle;
		this.priority = boss.attackStyle.getPriority();
		this.color = attackStyle.color;
	}

	public void updateTicksUntilNextAnimation()
	{
		if (ticksUntilNextAnimation > 0)
		{
			ticksUntilNextAnimation--;
		}

		if (npc.getAnimation() == animationId && ticksUntilNextAnimation == 0)
		{
			ticksUntilNextAnimation = animationTickSpeed;
		}
	}

	public Actor getInteractingActor()
	{
		return npc.getInteracting();
	}

	@Override
	public int compareTo(@NotNull DagannothKing o)
	{
		return Comparator.comparing(DagannothKing::getTicksUntilNextAnimation).thenComparing(DagannothKing::getPriority).compare(this, o);
	}

	@RequiredArgsConstructor
	public enum Boss
	{
		DAGANNOTH_PRIME(NpcID.DAGANNOTH_PRIME, DagannothHelperPlugin.DAG_PRIME_ATTACK, 4, AttackStyle.MAGE),
		DAGANNOTH_REX(NpcID.DAGANNOTH_REX, DagannothHelperPlugin.DAG_REX_ATTACK, 4, AttackStyle.MELEE),
		DAGANNOTH_SUPREME(NpcID.DAGANNOTH_SUPREME, DagannothHelperPlugin.DAG_SUPREME_ATTACK, 4, AttackStyle.RANGE),
		ECHO_PRIME(NpcID.DAGANNOTH_PRIME, DagannothHelperPlugin.DAG_PRIME_ATTACK, 5, AttackStyle.MAGE),
		ECHO_REX(NpcID.DAGANNOTH_REX, DagannothHelperPlugin.DAG_REX_ATTACK, 5, AttackStyle.MELEE),
		ECHO_SUPREME(NpcID.DAGANNOTH_SUPREME, DagannothHelperPlugin.DAG_SUPREME_ATTACK, 5, AttackStyle.RANGE);

		private final int npcId;
		private final int animationId;
		private final int attackSpeed;
		private final AttackStyle attackStyle;

		public static Boss of(final int npcId, boolean echo)
		{
			for (final Boss boss : Boss.values())
			{
				if (echo)
				{
					if (boss.attackSpeed == 5 && boss.npcId == npcId)
					{
						return boss;
					}
				}
				else if (boss.npcId == npcId)
				{
					return boss;
				}
			}

			throw new IllegalArgumentException("Unsupported Boss npcId");
		}
	}

	@Getter
	@RequiredArgsConstructor
	public enum AttackStyle
	{
		MAGE(Prayer.PROTECT_FROM_MAGIC, 3, Color.CYAN),
		RANGE(Prayer.PROTECT_FROM_MISSILES, 2, Color.GREEN),
		MELEE(Prayer.PROTECT_FROM_MELEE, 1, Color.RED);

		private final Prayer prayer;
		private final int priority;
		private final Color color;
	}
}
