/*
 * Copyright (c) 2016-2021.
 *
 * This file is part of Imposter.
 *
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as
 * defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights
 * under the License will not include, and the License does not grant to
 * you, the right to Sell the Software.
 *
 * For purposes of the foregoing, "Sell" means practicing any or all of
 * the rights granted to you under the License to provide to third parties,
 * for a fee or other consideration (including without limitation fees for
 * hosting or consulting/support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from
 * the functionality of the Software. Any license notice or attribution
 * required by the License must also include this Commons Clause License
 * Condition notice.
 *
 * Software: Imposter
 *
 * License: GNU Lesser General Public License version 3
 *
 * Licensor: Peter Cornish
 *
 * Imposter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Imposter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Imposter.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.gatehill.imposter.lifecycle

import com.google.common.collect.Lists
import org.apache.logging.log4j.LogManager
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * @author Pete Cornish
 */
abstract class LifecycleHooks<L> {
    private val listeners: MutableList<L> = Lists.newArrayList()

    fun registerListener(listener: L) {
        LOGGER.trace("Registered listener: {}", listener!!::class.java.canonicalName)
        listeners.add(listener)
    }

    fun forEach(listenerConsumer: Consumer<L>?) {
        if (listeners.isEmpty()) {
            return
        }
        listeners.forEach(listenerConsumer)
    }

    fun allMatch(listenerConsumer: Predicate<L>?): Boolean {
        return if (listeners.isEmpty()) {
            true
        } else listeners.stream().allMatch(listenerConsumer)
    }

    val isEmpty: Boolean
        get() = listeners.isEmpty()

    companion object {
        private val LOGGER = LogManager.getLogger(
            LifecycleHooks::class.java
        )
    }
}