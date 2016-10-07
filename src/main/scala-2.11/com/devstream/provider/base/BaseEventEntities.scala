package com.devstream.provider.base

trait ProviderBasedEvent

case class DevStreamEvent(provider: String, `type`: String,
                          event: ProviderBasedEvent, createdAt: Long)