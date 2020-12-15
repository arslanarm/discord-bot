package me.plony.processor

class ServiceManager {
    private val services = mutableListOf<Service>()
    val serviceList: List<Service> = services

    fun add(service: Service) = services.add(service)
    fun addAll(vararg services: Service) = services.forEach(::add)
    fun addAll(services: List<Service>) = services.forEach(::add)
    fun executeAll() = services.forEach{ it.start() }
    fun cancelAll() = services.forEach { it.cancel() }
    fun restartAll() {
        cancelAll()
        executeAll()
    }
}