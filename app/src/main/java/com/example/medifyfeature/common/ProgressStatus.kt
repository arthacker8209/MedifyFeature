package com.example.medifyfeature

sealed class ProgressStatus<T> {
    class Loading<T> : ProgressStatus<T>()

    data class Success<T>(val data: T) : ProgressStatus<T>()

    data class Error<T>(val errorMessage: String) : ProgressStatus<T>()
}

sealed class DataResult<T> {
    data class DataSuccess<T>(val data: T) : DataResult<T>()
    data class DataError<T>(val errorMessage: String) : DataResult<T>()
}
