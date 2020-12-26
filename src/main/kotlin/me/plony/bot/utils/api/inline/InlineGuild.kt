package me.plony.bot.utils.api.inline

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.supplier.EntitySupplier

class InlineGuild(override val id: Snowflake,
                  override val kord: Kord,
                  override val supplier: EntitySupplier = kord.defaultSupplier
) : GuildBehavior