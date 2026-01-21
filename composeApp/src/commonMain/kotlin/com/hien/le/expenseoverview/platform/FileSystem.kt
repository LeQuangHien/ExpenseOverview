package com.hien.le.expenseoverview.platform

interface FileSystem {
    /** Returns a platform file path where exports should be written. */
    fun exportsDirPath(): String

    /** Write bytes to absolute path. Create parent dirs if needed. */
    suspend fun writeBytes(path: String, bytes: ByteArray)

    /** Write text (UTF-8). */
    suspend fun writeText(path: String, text: String)
}