package com.svet.config

interface Configuration<T> {
    fun load(): T?
    fun save()
}
