package com.devstream.provider.base

trait ProviderBasedEvent

case class DevStreamEvent(provider: String, eventType: String,
                          event: ProviderBasedEvent, createdAt: Long)