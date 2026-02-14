package com.brios.miempresa.catalog.domain

enum class CatalogAccessError(
    val routeValue: String,
) {
    NO_INTERNET_FIRST_VISIT("no_internet_first_visit"),
    CATALOG_NOT_FOUND("catalog_not_found"),
    CATALOG_NOT_AVAILABLE("catalog_not_available"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun fromRouteValue(value: String?): CatalogAccessError =
            entries.firstOrNull { it.routeValue == value } ?: UNKNOWN
    }
}

class CatalogSyncException(
    val error: CatalogAccessError,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
