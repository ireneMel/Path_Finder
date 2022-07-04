package com.example.pathfinder.core.serialization.write

const val FILE_TYPE = "text/plain"

enum class FileState {
	OPENED, ERROR, CLOSED, IDLE
}