package com.chromesearcher.donefun

data class Task(var status: String, val template: TaskTemplate, var id: String, val dateCreated: Map<String, String>)