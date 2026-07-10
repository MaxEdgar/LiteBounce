/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.utils.io

import io.netty.bootstrap.AbstractBootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import net.minecraft.server.network.EventLoopGroupHolder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Shortcut for Netty client [io.netty.bootstrap.Bootstrap],
 * using shared [io.netty.channel.EventLoopGroup] from [EventLoopGroupHolder]
 */
internal fun <B : AbstractBootstrap<B, Channel>> AbstractBootstrap<B, Channel>.clientChannelAndGroup(
    useEpoll: Boolean = true
): B {
    val networkingBackend = EventLoopGroupHolder.remote(useEpoll)
    return channel(networkingBackend.channelCls())
            .group(networkingBackend.eventLoopGroup())
}

/**
 * Returns a pair of (bossGroup, workerGroup) for server bootstraps.
 */
@JvmOverloads
internal fun ServerBootstrap.setup(useNativeTransport: Boolean = true): Pair<EventLoopGroup, EventLoopGroup> {
    val bossGroup = if (useNativeTransport) {
        EpollEventLoopGroup(1)
    } else {
        NioEventLoopGroup(1)
    }

    val workerGroup = if (useNativeTransport) {
        EpollEventLoopGroup()
    } else {
        NioEventLoopGroup()
    }

    val channelClass = when {
        useNativeTransport -> EpollServerSocketChannel::class.java
        else -> NioServerSocketChannel::class.java
    }

    group(bossGroup, workerGroup)
    channel(channelClass)

    return Pair(bossGroup, workerGroup)
}

/**
 * Awaits a [ChannelFuture] as a suspend function.
 */
internal suspend fun ChannelFuture.awaitSuspend(): ChannelFuture = suspendCoroutine { cont ->
    this.addListener(GenericFutureListener { future: Future<in Void> ->
        if (future.isSuccess) {
            @Suppress("UNCHECKED_CAST")
            cont.resume(future as ChannelFuture)
        } else if (future.cause() != null) {
            cont.resumeWithException(future.cause()!!)
        } else {
            @Suppress("UNCHECKED_CAST")
            cont.resume(future as ChannelFuture)
        }
    })
}

/**
 * Awaits a [ChannelFuture] as a suspend function (same as awaitSuspend).
 */
internal suspend fun ChannelFuture.syncSuspend(): ChannelFuture = suspendCoroutine { cont ->
    this.addListener(GenericFutureListener { future: Future<in Void> ->
        if (future.isSuccess) {
            @Suppress("UNCHECKED_CAST")
            cont.resume(future as ChannelFuture)
        } else if (future.cause() != null) {
            cont.resumeWithException(future.cause()!!)
        } else {
            @Suppress("UNCHECKED_CAST")
            cont.resume(future as ChannelFuture)
        }
    })
}
